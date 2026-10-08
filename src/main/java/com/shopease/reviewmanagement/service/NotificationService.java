package com.shopease.reviewmanagement.service;

import com.shopease.usermanagement.entity.User;
import com.shopease.usermanagement.repository.UserRepository;
import com.shopease.reviewmanagement.entity.MessageType;
import com.shopease.reviewmanagement.entity.Notification;
import com.shopease.reviewmanagement.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification sendNotification(Integer userId, MessageType type, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Notification notification = new Notification(user, type, message);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdOrderBySentTimeDesc(userId);
    }

    @Transactional
    public Notification markAsRead(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setStatus("READ");
        return notificationRepository.save(notification);
    }
}
