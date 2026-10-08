package com.shopease.ordermanagement.dto;

import com.shopease.ordermanagement.entity.OrderStatus;
import com.shopease.ordermanagement.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderDtos {

    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Integer productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        public OrderItemRequest() {}

        public OrderItemRequest(Integer productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CreateOrderRequest {
        @NotNull(message = "Customer ID is required")
        private Integer customerId;

        @NotBlank(message = "Shipping address is required")
        private String shippingAddress;

        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;

        @NotEmpty(message = "At least one order item is required")
        private List<OrderItemRequest> items;

        public CreateOrderRequest() {}

        public Integer getCustomerId() { return customerId; }
        public void setCustomerId(Integer customerId) { this.customerId = customerId; }

        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
    }

    public static class UpdateOrderStatusRequest {
        @NotNull
        private OrderStatus status;

        public UpdateOrderStatusRequest() {}

        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
    }

    public static class UpdatePaymentStatusRequest {
        @NotNull
        private com.shopease.ordermanagement.entity.PaymentStatus paymentStatus;

        public UpdatePaymentStatusRequest() {}

        public com.shopease.ordermanagement.entity.PaymentStatus getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(com.shopease.ordermanagement.entity.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    }

    public static class UpdateOrderAndPaymentStatusRequest {
        private OrderStatus orderStatus;
        private com.shopease.ordermanagement.entity.PaymentStatus paymentStatus;

        public UpdateOrderAndPaymentStatusRequest() {}

        public OrderStatus getOrderStatus() { return orderStatus; }
        public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }

        public com.shopease.ordermanagement.entity.PaymentStatus getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(com.shopease.ordermanagement.entity.PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    }
}
