package com.shopease.inventorymanagement.service;

import com.shopease.usermanagement.entity.Role;
import com.shopease.usermanagement.entity.User;
import com.shopease.usermanagement.repository.UserRepository;
import com.shopease.productmanagement.entity.Product;
import com.shopease.productmanagement.repository.ProductRepository;
import com.shopease.inventorymanagement.dto.InventoryDtos.*;
import com.shopease.inventorymanagement.entity.Inventory;
import com.shopease.inventorymanagement.entity.MovementType;
import com.shopease.inventorymanagement.entity.StockMovement;
import com.shopease.inventorymanagement.repository.InventoryRepository;
import com.shopease.inventorymanagement.repository.StockMovementRepository;
import com.shopease.reviewmanagement.entity.MessageType;
import com.shopease.reviewmanagement.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public InventoryService(InventoryRepository inventoryRepository,
                            StockMovementRepository stockMovementRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            NotificationService notificationService) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<Inventory> getAllInventory() {
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            inventoryRepository.findByProduct_ProductId(p.getProductId())
                    .orElseGet(() -> inventoryRepository.save(new Inventory(p, 0, 10)));
        }
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryByProductId(Integer productId) {
        return inventoryRepository.findByProduct_ProductId(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                    Inventory newInv = new Inventory(product, 0, 10);
                    return inventoryRepository.save(newInv);
                });
    }

    public List<Inventory> getLowStockAlerts() {
        return inventoryRepository.findLowStockInventories();
    }

    public InventorySummaryDto getInventorySummary() {
        List<Inventory> all = getAllInventory();
        long totalSkus = all.size();
        long totalUnits = all.stream().mapToLong(Inventory::getStockQuantity).sum();
        long outOfStockCount = all.stream().filter(Inventory::isOutOfStock).count();
        long lowStockCount = all.stream().filter(i -> i.isLowStock() && !i.isOutOfStock()).count();
        long healthyCount = all.stream().filter(i -> !i.isLowStock()).count();
        return new InventorySummaryDto(totalSkus, totalUnits, lowStockCount, outOfStockCount, healthyCount);
    }

    public List<StockMovement> getAllStockMovements() {
        return stockMovementRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional
    public Inventory addBatch(AddBatchRequest request) {
        Inventory inventory = getInventoryByProductId(request.getProductId());
        User performedBy = userRepository.findById(request.getPerformedByUserId())
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElseThrow());

        int newQty = inventory.getStockQuantity() + request.getQuantity();
        inventory.setStockQuantity(newQty);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory = inventoryRepository.save(inventory);

        StockMovement movement = new StockMovement(
                inventory,
                request.getBatchNumber(),
                MovementType.ADD_BATCH,
                request.getQuantity(),
                performedBy
        );
        stockMovementRepository.save(movement);

        return inventory;
    }

    @Transactional
    public Inventory adjustStock(AdjustStockRequest request) {
        Inventory inventory = getInventoryByProductId(request.getProductId());
        User performedBy = userRepository.findById(request.getPerformedByUserId())
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElseThrow());

        int newQty = inventory.getStockQuantity() + request.getQuantityChange();
        if (newQty < 0) {
            throw new IllegalArgumentException("Stock quantity cannot become negative (Current: " + inventory.getStockQuantity() + ", Adjustment: " + request.getQuantityChange() + ")");
        }

        inventory.setStockQuantity(newQty);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory = inventoryRepository.save(inventory);

        StockMovement movement = new StockMovement(
                inventory,
                request.getReason(),
                MovementType.MANUAL_ADJUSTMENT,
                request.getQuantityChange(),
                performedBy
        );
        stockMovementRepository.save(movement);

        triggerLowStockAlertNotification(inventory);

        return inventory;
    }

    @Transactional
    public void deductStockForOrder(Integer productId, int quantity, String orderRef, Integer performedByUserId) {
        Inventory inventory = getInventoryByProductId(productId);
        if (inventory.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product '" + inventory.getProduct().getName() + "'. Available: " + inventory.getStockQuantity() + ", Requested: " + quantity);
        }

        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory = inventoryRepository.save(inventory);

        User user = null;
        if (performedByUserId != null) {
            user = userRepository.findById(performedByUserId).orElse(null);
        }
        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElse(null);
        }

        StockMovement movement = new StockMovement(
                inventory,
                orderRef,
                MovementType.ORDER_DEDUCTION,
                -quantity,
                user
        );
        stockMovementRepository.save(movement);

        triggerLowStockAlertNotification(inventory);
    }

    @Transactional
    public void restoreStockForOrder(Integer productId, int quantity, String orderRef, Integer performedByUserId) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory = inventoryRepository.save(inventory);

        User user = null;
        if (performedByUserId != null) {
            user = userRepository.findById(performedByUserId).orElse(null);
        }
        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElse(null);
        }

        StockMovement movement = new StockMovement(
                inventory,
                "RESTORE-" + orderRef,
                MovementType.MANUAL_ADJUSTMENT,
                quantity,
                user
        );
        stockMovementRepository.save(movement);
    }

    @Transactional
    public Inventory updateThreshold(Integer productId, Integer threshold) {
        Inventory inventory = getInventoryByProductId(productId);
        inventory.setLowStockThreshold(threshold);
        inventory = inventoryRepository.save(inventory);
        triggerLowStockAlertNotification(inventory);
        return inventory;
    }

    @Transactional
    public void deleteInventory(Integer inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found: " + inventoryId));
        stockMovementRepository.deleteByInventory_InventoryId(inventoryId);
        inventoryRepository.delete(inventory);
    }

    @Transactional
    public void deleteInventoryByProductId(Integer productId) {
        Inventory inventory = inventoryRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory record not found for product ID: " + productId));
        stockMovementRepository.deleteByInventory_InventoryId(inventory.getInventoryId());
        inventoryRepository.delete(inventory);
    }

    @Transactional
    public void deleteStockMovement(Integer movementId) {
        if (!stockMovementRepository.existsById(movementId)) {
            throw new RuntimeException("Stock movement not found: " + movementId);
        }
        stockMovementRepository.deleteById(movementId);
    }

    @Transactional
    public long clearOldMovements(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<StockMovement> oldMovements = stockMovementRepository.findAll().stream()
                .filter(m -> m.getTimestamp() != null && m.getTimestamp().isBefore(cutoff))
                .toList();
        stockMovementRepository.deleteAll(oldMovements);
        return oldMovements.size();
    }

    private void triggerLowStockAlertNotification(Inventory inventory) {
        if (inventory.isLowStock()) {
            try {
                List<User> recipients = userRepository.findByRole(Role.INVENTORY_OFFICER);
                if (recipients == null || recipients.isEmpty()) {
                    recipients = userRepository.findByRole(Role.ADMIN);
                }
                String alertMsg = "STOCK ALERT: Product '" + inventory.getProduct().getName() + 
                                  "' has reached low stock: " + inventory.getStockQuantity() + 
                                  " units remaining (Threshold: " + inventory.getLowStockThreshold() + ").";
                for (User u : recipients) {
                    notificationService.sendNotification(u.getUserId(), MessageType.STOCK, alertMsg);
                }
            } catch (Exception ex) {
                System.err.println("Notice: Could not dispatch stock alert notification: " + ex.getMessage());
            }
        }
    }
}
