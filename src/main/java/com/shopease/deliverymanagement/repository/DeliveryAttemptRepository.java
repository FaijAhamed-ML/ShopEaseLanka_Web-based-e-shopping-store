package com.shopease.deliverymanagement.repository;

import com.shopease.deliverymanagement.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Integer> {
    List<DeliveryAttempt> findByDelivery_DeliveryIdOrderByAttemptTimeDesc(Integer deliveryId);
}
