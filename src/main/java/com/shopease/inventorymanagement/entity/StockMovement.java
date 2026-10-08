package com.shopease.inventorymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopease.usermanagement.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Integer movementId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    public StockMovement() {}

    public StockMovement(Inventory inventory, String batchNumber, MovementType movementType, Integer quantity, User performedBy) {
        this.inventory = inventory;
        this.batchNumber = batchNumber;
        this.movementType = movementType;
        this.quantity = quantity;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now();
    }

    public Integer getMovementId() { return movementId; }
    public void setMovementId(Integer movementId) { this.movementId = movementId; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public MovementType getMovementType() { return movementType; }
    public void setMovementType(MovementType movementType) { this.movementType = movementType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public User getPerformedBy() { return performedBy; }
    public void setPerformedBy(User performedBy) { this.performedBy = performedBy; }
}
