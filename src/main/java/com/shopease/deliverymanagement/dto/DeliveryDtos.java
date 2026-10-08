package com.shopease.deliverymanagement.dto;

import com.shopease.deliverymanagement.entity.AttemptStatus;
import com.shopease.deliverymanagement.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public class DeliveryDtos {

    public static class UpdateShipmentRequest {
        @NotNull(message = "Shipment status is required")
        private ShipmentStatus shipmentStatus;
        private String notes;

        public UpdateShipmentRequest() {}

        public ShipmentStatus getShipmentStatus() { return shipmentStatus; }
        public void setShipmentStatus(ShipmentStatus shipmentStatus) { this.shipmentStatus = shipmentStatus; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class AssignRiderRequest {
        @NotNull(message = "Delivery person ID is required")
        private Integer deliveryPersonId;

        public AssignRiderRequest() {}

        public Integer getDeliveryPersonId() { return deliveryPersonId; }
        public void setDeliveryPersonId(Integer deliveryPersonId) { this.deliveryPersonId = deliveryPersonId; }
    }

    public static class RecordAttemptRequest {
        @NotNull(message = "Attempt status is required")
        private AttemptStatus attemptStatus;
        private String failureReason;

        public RecordAttemptRequest() {}

        public AttemptStatus getAttemptStatus() { return attemptStatus; }
        public void setAttemptStatus(AttemptStatus attemptStatus) { this.attemptStatus = attemptStatus; }

        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    }
}
