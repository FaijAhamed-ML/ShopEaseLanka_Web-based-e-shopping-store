package com.shopease.ordermanagement.observer;

/**
 * Event Types for the Order Lifecycle (Observer Pattern)
 */
public enum OrderEventType {
    ORDER_CONFIRMED,
    ORDER_CANCELLED,
    ORDER_STATUS_CHANGED,
    PAYMENT_STATUS_CHANGED
}
