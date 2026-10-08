package com.shopease.deliverymanagement.repository;

import com.shopease.deliverymanagement.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
    Optional<Delivery> findByOrder_OrderId(Integer orderId);
    List<Delivery> findByDeliveryPerson_UserId(Integer deliveryPersonId);
    List<Delivery> findByOrder_Customer_CustomerIdOrderByUpdatedAtDesc(Integer customerId);
    List<Delivery> findAllByOrderByUpdatedAtDesc();
}
