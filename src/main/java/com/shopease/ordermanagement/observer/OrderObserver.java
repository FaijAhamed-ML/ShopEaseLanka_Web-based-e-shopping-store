package com.shopease.ordermanagement.observer;

import com.shopease.ordermanagement.entity.Order;

/**
 * Observer Interface (Behavioral Design Pattern)
 * Defines the contract that all concrete observers implement to receive updates
 * when the Order Subject changes state.
 */
public interface OrderObserver {

    /**
     * Receives event notifications from the Concrete Subject.
     *
     * @param order The order entity associated with the event
     * @param eventType The specific order event type
     * @param message Descriptive message regarding the event
     */
    void update(Order order, OrderEventType eventType, String message);
}
