package com.shopease.deliverymanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_attempts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeliveryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Integer attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    @JsonIgnore
    private Delivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status", nullable = false)
    private AttemptStatus attemptStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "attempt_time")
    private LocalDateTime attemptTime = LocalDateTime.now();

    public DeliveryAttempt() {}

    public DeliveryAttempt(Delivery delivery, AttemptStatus attemptStatus, String failureReason) {
        this.delivery = delivery;
        this.attemptStatus = attemptStatus;
        this.failureReason = failureReason;
        this.attemptTime = LocalDateTime.now();
    }

    public Integer getAttemptId() { return attemptId; }
    public void setAttemptId(Integer attemptId) { this.attemptId = attemptId; }

    public Delivery getDelivery() { return delivery; }
    public void setDelivery(Delivery delivery) { this.delivery = delivery; }

    public AttemptStatus getAttemptStatus() { return attemptStatus; }
    public void setAttemptStatus(AttemptStatus attemptStatus) { this.attemptStatus = attemptStatus; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getAttemptTime() { return attemptTime; }
    public void setAttemptTime(LocalDateTime attemptTime) { this.attemptTime = attemptTime; }
}
