package com.shopease.inventorymanagement.repository;

import com.shopease.inventorymanagement.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByProduct_ProductId(Integer productId);

    @Query("SELECT i FROM Inventory i WHERE i.stockQuantity <= i.lowStockThreshold ORDER BY i.stockQuantity ASC")
    List<Inventory> findLowStockInventories();
}
