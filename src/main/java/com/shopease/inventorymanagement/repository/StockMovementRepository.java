package com.shopease.inventorymanagement.repository;

import com.shopease.inventorymanagement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Integer> {
    List<StockMovement> findByInventory_InventoryIdOrderByTimestampDesc(Integer inventoryId);
    List<StockMovement> findAllByOrderByTimestampDesc();
    void deleteByInventory_InventoryId(Integer inventoryId);
    void deleteByTimestampBefore(java.time.LocalDateTime cutoff);
}
