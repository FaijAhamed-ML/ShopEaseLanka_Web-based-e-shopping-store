package com.shopease.ordermanagement.observer;

import com.shopease.deliverymanagement.service.DeliveryService;
import com.shopease.ordermanagement.entity.Order;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer: Delivery Logistics Dispatcher
 * Automatically triggers the generation of shipping records and tracking numbers
 * when an order confirmation event occurs.
 */
@Component
public class DeliveryDispatchObserver implements OrderObserver {

    private final DeliveryService deliveryService;

    public DeliveryDispatchObserver(@Lazy DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public void update(Order order, OrderEventType eventType, String message) {
        if (eventType == OrderEventType.ORDER_CONFIRMED && order != null) {
            deliveryService.initializeDeliveryForOrder(order);
        }
    }
}
