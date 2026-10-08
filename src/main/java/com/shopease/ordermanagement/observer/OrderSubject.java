package com.shopease.ordermanagement.observer;

import com.shopease.ordermanagement.entity.Order;

/**
 * Subject Interface (Behavioral Design Pattern)
 * Outlines the operations for managing and notifying observers.
 */
public interface OrderSubject {

    void addObserver(OrderObserver observer);

    void removeObserver(OrderObserver observer);

    void notifyObservers(Order order, OrderEventType eventType, String message);
}
