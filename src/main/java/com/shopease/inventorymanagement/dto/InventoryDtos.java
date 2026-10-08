package com.shopease.inventorymanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InventoryDtos {

    public static class AddBatchRequest {
        @NotNull(message = "Product ID is required")
        private Integer productId;

        @NotBlank(message = "Batch number is required")
        private String batchNumber;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        private Integer performedByUserId = 3; // Default to Inventory Officer

        public AddBatchRequest() {}

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Integer getPerformedByUserId() { return performedByUserId; }
        public void setPerformedByUserId(Integer performedByUserId) { this.performedByUserId = performedByUserId; }
    }

    public static class AdjustStockRequest {
        @NotNull(message = "Product ID is required")
        private Integer productId;

        @NotNull(message = "Adjustment quantity is required (positive or negative)")
        private Integer quantityChange;

        @NotBlank(message = "Reason / reference is required")
        private String reason;

        private Integer performedByUserId = 3;

        public AdjustStockRequest() {}

        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }

        public Integer getQuantityChange() { return quantityChange; }
        public void setQuantityChange(Integer quantityChange) { this.quantityChange = quantityChange; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public Integer getPerformedByUserId() { return performedByUserId; }
        public void setPerformedByUserId(Integer performedByUserId) { this.performedByUserId = performedByUserId; }
    }

    public static class ThresholdUpdateRequest {
        @NotNull
        @Min(1)
        private Integer threshold;

        public ThresholdUpdateRequest() {}

        public Integer getThreshold() { return threshold; }
        public void setThreshold(Integer threshold) { this.threshold = threshold; }
    }

    public static class InventorySummaryDto {
        private long totalSkus;
        private long totalUnits;
        private long lowStockCount;
        private long outOfStockCount;
        private long healthyCount;

        public InventorySummaryDto() {}

        public InventorySummaryDto(long totalSkus, long totalUnits, long lowStockCount, long outOfStockCount, long healthyCount) {
            this.totalSkus = totalSkus;
            this.totalUnits = totalUnits;
            this.lowStockCount = lowStockCount;
            this.outOfStockCount = outOfStockCount;
            this.healthyCount = healthyCount;
        }

        public long getTotalSkus() { return totalSkus; }
        public void setTotalSkus(long totalSkus) { this.totalSkus = totalSkus; }

        public long getTotalUnits() { return totalUnits; }
        public void setTotalUnits(long totalUnits) { this.totalUnits = totalUnits; }

        public long getLowStockCount() { return lowStockCount; }
        public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

        public long getOutOfStockCount() { return outOfStockCount; }
        public void setOutOfStockCount(long outOfStockCount) { this.outOfStockCount = outOfStockCount; }

        public long getHealthyCount() { return healthyCount; }
        public void setHealthyCount(long healthyCount) { this.healthyCount = healthyCount; }
    }
}
