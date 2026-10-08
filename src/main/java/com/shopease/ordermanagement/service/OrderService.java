package com.shopease.ordermanagement.service;

import com.shopease.usermanagement.entity.Customer;
import com.shopease.usermanagement.repository.CustomerRepository;
import com.shopease.productmanagement.entity.Product;
import com.shopease.productmanagement.repository.ProductRepository;
import com.shopease.inventorymanagement.entity.Inventory;
import com.shopease.inventorymanagement.service.InventoryService;
import static com.shopease.ordermanagement.dto.OrderDtos.*;
import com.shopease.ordermanagement.entity.*;
import com.shopease.ordermanagement.repository.OrderItemRepository;
import com.shopease.ordermanagement.repository.OrderRepository;
import com.shopease.ordermanagement.repository.PaymentRepository;
import com.shopease.ordermanagement.observer.OrderEventSubject;
import com.shopease.ordermanagement.observer.OrderEventType;
import com.shopease.ordermanagement.strategy.PaymentContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PaymentContext paymentContext;
    private final OrderEventSubject orderEventSubject;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        InventoryService inventoryService,
                        PaymentContext paymentContext,
                        OrderEventSubject orderEventSubject) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.paymentContext = paymentContext;
        this.orderEventSubject = orderEventSubject;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getCustomerOrders(Integer customerId) {
        return orderRepository.findByCustomer_CustomerIdAndDeletedByCustomerFalseOrderByCreatedAtDesc(customerId);
    }

    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        // 1. Pre-check real-time inventory for all items
        for (OrderItemRequest itemReq : request.getItems()) {
            Inventory inv = inventoryService.getInventoryByProductId(itemReq.getProductId());
            if (inv.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalStateException("Product '" + inv.getProduct().getName() + "' is out of stock or insufficient quantity (Available: " + inv.getStockQuantity() + ", Requested: " + itemReq.getQuantity() + ")");
            }
        }

        // 2. Initialize Order
        Order order = new Order(
                customer,
                BigDecimal.ZERO, // will calculate total below
                OrderStatus.CONFIRMED,
                request.getShippingAddress()
        );
        order = orderRepository.save(order);

        // 3. Process items, calculate total amount, and deduct stock
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));

            OrderItem item = new OrderItem(
                    order,
                    product,
                    itemReq.getQuantity(),
                    product.getPrice(),
                    product.getDiscountPercentage()
            );
            item = orderItemRepository.save(item);
            order.getOrderItems().add(item);
            total = total.add(item.getSubtotal());

            // Automated transactional deduction from stock upon order confirmation
            inventoryService.deductStockForOrder(
                    product.getProductId(),
                    itemReq.getQuantity(),
                    "ORDER-#" + order.getOrderId(),
                    customer.getCustomerId()
            );
        }

        order.setTotalAmount(total);
        order = orderRepository.save(order);

        // Record Payment using Strategy Pattern
        Payment payment = paymentContext.executePayment(request.getPaymentMethod(), order, total);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        // Notify registered Observers of order confirmation event
        String confirmMsg = "ORDER CONFIRMED: Your order #" + order.getOrderId() + " for LKR " + total +
                " has been placed successfully. Payment Method: " + request.getPaymentMethod() + " (" + payment.getPaymentStatus() + ").";
        orderEventSubject.notifyObservers(order, OrderEventType.ORDER_CONFIRMED, confirmMsg);

        return order;
    }

    @Transactional
    public Order cancelOrder(Integer orderId, Integer customerId) {
        Order order = getOrderById(orderId);

        if (customerId != null && !order.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this order");
        }

        // Cancellation: Customers can cancel orders only if the status is PENDING or CONFIRMED
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled because it is already " + order.getOrderStatus() +
                    ". Orders can only be cancelled while in PENDING or CONFIRMED status.");
        }

        // Restoring inventory levels
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.restoreStockForOrder(
                    item.getProduct().getProductId(),
                    item.getQuantity(),
                    "ORDER-#" + order.getOrderId(),
                    customerId
            );
        }

        // Update payment status if card payment was completed
        paymentRepository.findByOrder_OrderId(orderId).ifPresent(p -> {
            if (p.getPaymentStatus() == PaymentStatus.COMPLETED) {
                p.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(p);
            }
        });

        order.setOrderStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Notify Observers of order cancellation
        orderEventSubject.notifyObservers(
                order,
                OrderEventType.ORDER_CANCELLED,
                "ORDER CANCELLED: Order #" + orderId + " has been cancelled. Reserved warehouse stock has been restored to inventory."
        );

        return order;
    }

    @Transactional
    public Order updateOrderStatus(Integer orderId, OrderStatus status) {
        if (status == OrderStatus.CANCELLED) {
            return cancelOrder(orderId, null);
        }
        Order order = getOrderById(orderId);
        order.setOrderStatus(status);
        order = orderRepository.save(order);

        // Notify Observers of order status change
        orderEventSubject.notifyObservers(
                order,
                OrderEventType.ORDER_STATUS_CHANGED,
                "ORDER STATUS UPDATE: Your order #" + orderId + " status is now " + status + "."
        );
        return order;
    }

    @Transactional
    public Payment updatePaymentStatus(Integer orderId, PaymentStatus paymentStatus) {
        Payment payment = paymentRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for Order #" + orderId));
        payment.setPaymentStatus(paymentStatus);
        if (paymentStatus == PaymentStatus.COMPLETED) {
            payment.setPaymentDate(java.time.LocalDateTime.now());
        }
        payment = paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            orderEventSubject.notifyObservers(
                    order,
                    OrderEventType.PAYMENT_STATUS_CHANGED,
                    "PAYMENT STATUS UPDATE: Payment for order #" + orderId + " is now " + paymentStatus + "."
            );
        }
        return payment;
    }

    @Transactional
    public Order updateOrderAndPaymentStatus(Integer orderId, OrderStatus orderStatus, PaymentStatus paymentStatus) {
        Order order = getOrderById(orderId);
        if (orderStatus != null) {
            if (orderStatus == OrderStatus.CANCELLED && order.getOrderStatus() != OrderStatus.CANCELLED) {
                order = cancelOrder(orderId, null);
            } else {
                order.setOrderStatus(orderStatus);
                order = orderRepository.save(order);
                orderEventSubject.notifyObservers(
                        order,
                        OrderEventType.ORDER_STATUS_CHANGED,
                        "ORDER STATUS UPDATE: Your order #" + orderId + " status is now " + orderStatus + "."
                );
            }
        }
        if (paymentStatus != null) {
            updatePaymentStatus(orderId, paymentStatus);
        }
        return getOrderById(orderId);
    }

    @Transactional
    public void deleteCustomerOrder(Integer orderId, Integer customerId) {
        Order order = getOrderById(orderId);

        if (customerId != null && !order.getCustomer().getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("You are not authorized to remove this order from history");
        }

        // Only cancelled or completed (delivered) orders can be removed from history
        if (order.getOrderStatus() != OrderStatus.CANCELLED && order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only cancelled or completed (delivered) orders can be removed from your order history. Current status: " + order.getOrderStatus());
        }

        order.setDeletedByCustomer(true);
        orderRepository.save(order);
    }
}
