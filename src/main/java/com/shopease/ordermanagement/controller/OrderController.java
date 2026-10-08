package com.shopease.ordermanagement.controller;

import com.shopease.common.ApiResponse;
import static com.shopease.ordermanagement.dto.OrderDtos.*;
import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.placeOrder(request);
            return ResponseEntity.ok(ApiResponse.ok("Order placed successfully", order));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Order placement failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Order>>> getCustomerOrders(@PathVariable Integer customerId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getCustomerOrders(customerId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(@PathVariable Integer id, @RequestParam(required = false) Integer customerId) {
        try {
            Order cancelled = orderService.cancelOrder(id, customerId);
            return ResponseEntity.ok(ApiResponse.ok("Order cancelled and stock restored successfully", cancelled));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to cancel order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(@PathVariable Integer id, @RequestBody UpdateOrderStatusRequest request) {
        try {
            Order updated = orderService.updateOrderStatus(id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.ok("Order status updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<com.shopease.ordermanagement.entity.Payment>> updatePaymentStatus(
            @PathVariable Integer id,
            @RequestBody UpdatePaymentStatusRequest request) {
        try {
            var updatedPayment = orderService.updatePaymentStatus(id, request.getPaymentStatus());
            return ResponseEntity.ok(ApiResponse.ok("Payment status updated successfully", updatedPayment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/manage")
    public ResponseEntity<ApiResponse<Order>> updateOrderAndPayment(
            @PathVariable Integer id,
            @RequestBody UpdateOrderAndPaymentStatusRequest request) {
        try {
            Order updated = orderService.updateOrderAndPaymentStatus(id, request.getOrderStatus(), request.getPaymentStatus());
            return ResponseEntity.ok(ApiResponse.ok("Order and payment status updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomerOrder(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer customerId) {
        try {
            orderService.deleteCustomerOrder(id, customerId);
            return ResponseEntity.ok(ApiResponse.ok("Order removed from history successfully", null));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to delete order: " + e.getMessage()));
        }
    }
}
