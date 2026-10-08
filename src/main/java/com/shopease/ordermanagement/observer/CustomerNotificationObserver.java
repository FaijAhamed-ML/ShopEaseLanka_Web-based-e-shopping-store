package com.shopease.ordermanagement.observer;

import com.shopease.ordermanagement.entity.Order;
import com.shopease.reviewmanagement.entity.MessageType;
import com.shopease.reviewmanagement.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer: Customer Notification Handler
 * Reacts to order state updates by dispatching automated in-app notifications
 * to the ordering customer.
 */
@Component
public class CustomerNotificationObserver implements OrderObserver {

    private final NotificationService notificationService;

    public CustomerNotificationObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void update(Order order, OrderEventType eventType, String message) {
        if (order != null && order.getCustomer() != null && message != null) {
            notificationService.sendNotification(
                    order.getCustomer().getCustomerId(),
                    MessageType.ORDER,
                    message
            );
        }
    }
}
