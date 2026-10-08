package com.shopease.ordermanagement.observer;

import com.shopease.ordermanagement.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer: Audit Log Recorder
 * Records order lifecycle state changes to server diagnostic logs for audit compliance.
 */
@Component
public class AuditLogOrderObserver implements OrderObserver {

    private static final Logger log = LoggerFactory.getLogger(AuditLogOrderObserver.class);

    @Override
    public void update(Order order, OrderEventType eventType, String message) {
        if (order != null) {
            log.info("[OBSERVER AUDIT] Order #{} Event: {} - {}", order.getOrderId(), eventType, message);
        }
    }
}
