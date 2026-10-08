package com.shopease.deliverymanagement.service;

import com.shopease.usermanagement.entity.User;
import com.shopease.usermanagement.repository.UserRepository;
import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.entity.OrderStatus;
import com.shopease.ordermanagement.repository.OrderRepository;
import com.shopease.deliverymanagement.dto.DeliveryDtos.*;
import com.shopease.deliverymanagement.entity.AttemptStatus;
import com.shopease.deliverymanagement.entity.Delivery;
import com.shopease.deliverymanagement.entity.DeliveryAttempt;
import com.shopease.deliverymanagement.entity.ShipmentStatus;
import com.shopease.deliverymanagement.repository.DeliveryAttemptRepository;
import com.shopease.deliverymanagement.repository.DeliveryRepository;
import com.shopease.reviewmanagement.entity.MessageType;
import com.shopease.reviewmanagement.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryRepository deliveryRepository;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           DeliveryAttemptRepository deliveryAttemptRepository,
                           UserRepository userRepository,
                           OrderRepository orderRepository,
                           NotificationService notificationService) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryAttemptRepository = deliveryAttemptRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    private void dispatchCustomerAlert(Delivery delivery, String alertMessage) {
        if (delivery == null || delivery.getOrder() == null || delivery.getOrder().getCustomer() == null) return;
        try {
            Integer customerUserId = delivery.getOrder().getCustomer().getCustomerId();
            notificationService.sendNotification(customerUserId, MessageType.DELIVERY, alertMessage);
            log.info("[DELIVERY DISPATCH] Sent to Customer #{} ({}): {}", 
                    customerUserId, delivery.getOrder().getCustomer().getPhoneNumber(), alertMessage);
        } catch (Exception ex) {
            log.warn("Could not dispatch delivery notification: {}", ex.getMessage());
        }
    }

    @Transactional
    public Delivery initializeDeliveryForOrder(Order order) {
        String trackingNumber = "SEL-LK-" + (100000 + new Random().nextInt(900000));
        Delivery delivery = new Delivery(order, trackingNumber, "Order received. Preparing package for dispatch.");
        delivery = deliveryRepository.save(delivery);

        dispatchCustomerAlert(delivery, "ORDER CONFIRMED: Delivery initialized for Order #" + order.getOrderId() + 
                                        ". Tracking Number: " + trackingNumber);
        return delivery;
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAllByOrderByUpdatedAtDesc();
    }

    public List<Delivery> getCustomerDeliveries(Integer customerId) {
        return deliveryRepository.findByOrder_Customer_CustomerIdOrderByUpdatedAtDesc(customerId);
    }

    public Delivery getDeliveryByTrackingNumber(String trackingNumber) {
        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("No delivery found with tracking number: " + trackingNumber));
        delivery.setAttempts(deliveryAttemptRepository.findByDelivery_DeliveryIdOrderByAttemptTimeDesc(delivery.getDeliveryId()));
        return delivery;
    }

    public Delivery getDeliveryByOrderId(Integer orderId) {
        Delivery delivery = deliveryRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No delivery found for order ID: " + orderId));
        delivery.setAttempts(deliveryAttemptRepository.findByDelivery_DeliveryIdOrderByAttemptTimeDesc(delivery.getDeliveryId()));
        return delivery;
    }

    @Transactional
    public Delivery assignDeliveryPerson(Integer deliveryId, Integer riderId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));
        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Delivery person not found: " + riderId));

        delivery.setDeliveryPerson(rider);
        delivery.setNotes("Assigned to delivery agent: " + rider.getUsername());
        delivery.setUpdatedAt(LocalDateTime.now());
        delivery = deliveryRepository.save(delivery);

        // Notify customer
        dispatchCustomerAlert(delivery, "SHIPMENT UPDATE: Delivery agent " + rider.getUsername() + 
                                        " has been assigned to deliver package " + delivery.getTrackingNumber() + ".");

        // Notify rider
        try {
            notificationService.sendNotification(rider.getUserId(), MessageType.DELIVERY, 
                    "NEW ASSIGNMENT: Package " + delivery.getTrackingNumber() + " (Order #" + 
                    delivery.getOrder().getOrderId() + ") assigned to you. Destination: " + delivery.getOrder().getShippingAddress());
        } catch (Exception ignored) {}

        return delivery;
    }

    /**
     * Enforces business-rule logistics lifecycle status progression:
     * PROCESSING -> DISPATCHED -> IN_TRANSIT -> OUT_FOR_DELIVERY -> DELIVERED
     */
    private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {
        if (current == null || next == null || current == next) {
            return;
        }

        if (current == ShipmentStatus.DELIVERED) {
            throw new IllegalArgumentException("Invalid status transition: Delivery is already DELIVERED and cannot be modified.");
        }

        boolean isValid = switch (current) {
            case PROCESSING -> (next == ShipmentStatus.DISPATCHED);
            case DISPATCHED -> (next == ShipmentStatus.IN_TRANSIT || next == ShipmentStatus.OUT_FOR_DELIVERY);
            case IN_TRANSIT -> (next == ShipmentStatus.OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> (next == ShipmentStatus.DELIVERED || next == ShipmentStatus.IN_TRANSIT);
            case DELIVERED -> false;
        };

        if (!isValid) {
            throw new IllegalArgumentException("Invalid status transition: Cannot transition delivery from " 
                    + current + " to " + next + ". Sequential stages cannot be skipped.");
        }
    }

    @Transactional
    public Delivery updateShipmentStatus(Integer deliveryId, UpdateShipmentRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        ShipmentStatus oldStatus = delivery.getShipmentStatus();

        // Business Rule: Without assigning a driver, shipment status cannot be changed from initial status (PROCESSING)
        if (oldStatus == ShipmentStatus.PROCESSING 
                && request.getShipmentStatus() != ShipmentStatus.PROCESSING 
                && delivery.getDeliveryPerson() == null) {
            throw new IllegalArgumentException("Cannot update shipment status: A delivery driver must be assigned before changing from initial status (PROCESSING).");
        }

        validateStatusTransition(oldStatus, request.getShipmentStatus());

        delivery.setShipmentStatus(request.getShipmentStatus());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            delivery.setNotes(request.getNotes());
        }
        delivery.setUpdatedAt(LocalDateTime.now());
        delivery = deliveryRepository.save(delivery);

        // When delivery is marked DELIVERED, synchronize the parent Order status
        if (request.getShipmentStatus() == ShipmentStatus.DELIVERED) {
            Order order = delivery.getOrder();
            if (order != null) {
                order.setOrderStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
            }
        } else if (request.getShipmentStatus() == ShipmentStatus.DISPATCHED) {
            Order order = delivery.getOrder();
            if (order != null) {
                order.setOrderStatus(OrderStatus.SHIPPED);
                orderRepository.save(order);
            }
        }

        // Dispatch customer milestone notification if status changed
        if (oldStatus != request.getShipmentStatus()) {
            String trackingNum = delivery.getTrackingNumber();
            String msg = switch (request.getShipmentStatus()) {
                case DISPATCHED -> "PACKAGE DISPATCHED: Shipment " + trackingNum + " has left the Colombo fulfillment center.";
                case IN_TRANSIT -> "IN TRANSIT: Shipment " + trackingNum + " is currently in transit to your local distribution hub.";
                case OUT_FOR_DELIVERY -> "OUT FOR DELIVERY: Shipment " + trackingNum + " is out for delivery today! Please keep your phone reachable.";
                case DELIVERED -> "PACKAGE DELIVERED: Shipment " + trackingNum + " has been successfully delivered. Thank you for shopping with ShopEase Lanka!";
                case PROCESSING -> "SHIPMENT UPDATE: Shipment " + trackingNum + " is being processed at the warehouse.";
            };
            dispatchCustomerAlert(delivery, msg);
        }

        return delivery;
    }

    @Transactional
    public DeliveryAttempt recordAttempt(Integer deliveryId, RecordAttemptRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        DeliveryAttempt attempt = new DeliveryAttempt(
                delivery,
                request.getAttemptStatus(),
                request.getFailureReason()
        );
        attempt = deliveryAttemptRepository.save(attempt);

        if (request.getAttemptStatus() == AttemptStatus.FAILED) {
            String reason = request.getFailureReason() != null ? request.getFailureReason() : "Customer unavailable";
            dispatchCustomerAlert(delivery, "DELIVERY ATTEMPT FAILED: Attempt for shipment " + 
                    delivery.getTrackingNumber() + " could not be completed: " + reason + ". Our courier will re-attempt delivery shortly.");
        }

        return attempt;
    }

    @Transactional
    public void deleteDelivery(Integer deliveryId) {
        if (!deliveryRepository.existsById(deliveryId)) {
            throw new RuntimeException("Delivery not found: " + deliveryId);
        }
        deliveryRepository.deleteById(deliveryId);
    }
}
