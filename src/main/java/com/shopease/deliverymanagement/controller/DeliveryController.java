package com.shopease.deliverymanagement.controller;

import com.shopease.common.ApiResponse;
import com.shopease.deliverymanagement.dto.DeliveryDtos.*;
import com.shopease.deliverymanagement.entity.Delivery;
import com.shopease.deliverymanagement.entity.DeliveryAttempt;
import com.shopease.deliverymanagement.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Delivery>>> getAllDeliveries() {
        return ResponseEntity.ok(ApiResponse.ok(deliveryService.getAllDeliveries()));
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<Delivery>> trackShipment(@PathVariable String trackingNumber) {
        try {
            Delivery delivery = deliveryService.getDeliveryByTrackingNumber(trackingNumber);
            return ResponseEntity.ok(ApiResponse.ok("Shipment located", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Delivery>> getByOrderId(@PathVariable Integer orderId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(deliveryService.getDeliveryByOrderId(orderId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Delivery>> assignRider(@PathVariable Integer id, @Valid @RequestBody AssignRiderRequest request) {
        try {
            Delivery delivery = deliveryService.assignDeliveryPerson(id, request.getDeliveryPersonId());
            return ResponseEntity.ok(ApiResponse.ok("Rider assigned successfully", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Delivery>> updateStatus(@PathVariable Integer id, @Valid @RequestBody UpdateShipmentRequest request) {
        try {
            Delivery delivery = deliveryService.updateShipmentStatus(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Shipment status updated", delivery));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/attempt")
    public ResponseEntity<ApiResponse<DeliveryAttempt>> recordAttempt(@PathVariable Integer id, @Valid @RequestBody RecordAttemptRequest request) {
        try {
            DeliveryAttempt attempt = deliveryService.recordAttempt(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Attempt recorded", attempt));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Delivery>>> getByCustomerId(@PathVariable Integer customerId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(deliveryService.getCustomerDeliveries(customerId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDelivery(@PathVariable Integer id) {
        try {
            deliveryService.deleteDelivery(id);
            return ResponseEntity.ok(ApiResponse.ok("Delivery record deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete delivery: " + e.getMessage()));
        }
    }
}
