package com.shopease.reviewmanagement.repository;

import com.shopease.reviewmanagement.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_UserIdOrderBySentTimeDesc(Integer userId);
    List<Notification> findByUser_UserIdAndStatusOrderBySentTimeDesc(Integer userId, String status);
}
