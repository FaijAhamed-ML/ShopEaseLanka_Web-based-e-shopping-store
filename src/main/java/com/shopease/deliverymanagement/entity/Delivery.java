package com.shopease.deliverymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopease.usermanagement.entity.User;
import com.shopease.ordermanagement.entity.Order;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliveries")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Integer deliveryId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_person_id")
    private User deliveryPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", nullable = false)
    private ShipmentStatus shipmentStatus = ShipmentStatus.PROCESSING;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("delivery")
    private List<DeliveryAttempt> attempts = new ArrayList<>();

    public Delivery() {}

    public Delivery(Order order, String trackingNumber, String notes) {
        this.order = order;
        this.trackingNumber = trackingNumber;
        this.notes = notes;
        this.shipmentStatus = ShipmentStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Integer deliveryId) { this.deliveryId = deliveryId; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public User getDeliveryPerson() { return deliveryPerson; }
    public void setDeliveryPerson(User deliveryPerson) { this.deliveryPerson = deliveryPerson; }

    public ShipmentStatus getShipmentStatus() { return shipmentStatus; }
    public void setShipmentStatus(ShipmentStatus shipmentStatus) { this.shipmentStatus = shipmentStatus; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<DeliveryAttempt> getAttempts() { return attempts; }
    public void setAttempts(List<DeliveryAttempt> attempts) { this.attempts = attempts; }
}
