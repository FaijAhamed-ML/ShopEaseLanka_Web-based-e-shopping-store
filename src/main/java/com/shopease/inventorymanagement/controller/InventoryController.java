package com.shopease.inventorymanagement.controller;

import com.shopease.common.ApiResponse;
import com.shopease.inventorymanagement.dto.InventoryDtos.*;
import com.shopease.inventorymanagement.entity.Inventory;
import com.shopease.inventorymanagement.entity.StockMovement;
import com.shopease.inventorymanagement.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Inventory>>> getAllInventory() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllInventory()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> getByProductId(@PathVariable Integer productId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(inventoryService.getInventoryByProductId(productId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<Inventory>>> getLowStockAlerts() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getLowStockAlerts()));
    }

    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<List<StockMovement>>> getStockMovements() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllStockMovements()));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Inventory>> addBatch(@Valid @RequestBody AddBatchRequest request) {
        try {
            Inventory updated = inventoryService.addBatch(request);
            return ResponseEntity.ok(ApiResponse.ok("Stock batch added successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to add batch: " + e.getMessage()));
        }
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<Inventory>> adjustStock(@Valid @RequestBody AdjustStockRequest request) {
        try {
            Inventory updated = inventoryService.adjustStock(request);
            return ResponseEntity.ok(ApiResponse.ok("Stock adjusted successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Adjustment failed: " + e.getMessage()));
        }
    }

    @PutMapping("/threshold/{productId}")
    public ResponseEntity<ApiResponse<Inventory>> updateThreshold(@PathVariable Integer productId, @Valid @RequestBody ThresholdUpdateRequest req) {
        try {
            Inventory updated = inventoryService.updateThreshold(productId, req.getThreshold());
            return ResponseEntity.ok(ApiResponse.ok("Threshold updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<InventorySummaryDto>> getInventorySummary() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getInventorySummary()));
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Integer inventoryId) {
        try {
            inventoryService.deleteInventory(inventoryId);
            return ResponseEntity.ok(ApiResponse.ok("Obsolete stock record deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete stock record: " + e.getMessage()));
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteInventoryByProduct(@PathVariable Integer productId) {
        try {
            inventoryService.deleteInventoryByProductId(productId);
            return ResponseEntity.ok(ApiResponse.ok("Obsolete product inventory deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete product inventory: " + e.getMessage()));
        }
    }

    @DeleteMapping("/movements/{movementId}")
    public ResponseEntity<ApiResponse<Void>> deleteStockMovement(@PathVariable Integer movementId) {
        try {
            inventoryService.deleteStockMovement(movementId);
            return ResponseEntity.ok(ApiResponse.ok("Stock movement record deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete stock movement: " + e.getMessage()));
        }
    }

    @DeleteMapping("/movements/clear-old")
    public ResponseEntity<ApiResponse<String>> clearOldMovements(@RequestParam(defaultValue = "30") int days) {
        try {
            long count = inventoryService.clearOldMovements(days);
            return ResponseEntity.ok(ApiResponse.ok("Cleared " + count + " obsolete stock movement records older than " + days + " days", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed clearing movements: " + e.getMessage()));
        }
    }
}
