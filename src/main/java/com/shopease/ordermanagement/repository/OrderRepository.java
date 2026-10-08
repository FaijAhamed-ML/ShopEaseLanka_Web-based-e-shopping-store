package com.shopease.ordermanagement.repository;

import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomer_CustomerIdOrderByCreatedAtDesc(Integer customerId);
    List<Order> findByCustomer_CustomerIdAndDeletedByCustomerFalseOrderByCreatedAtDesc(Integer customerId);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus);
}
