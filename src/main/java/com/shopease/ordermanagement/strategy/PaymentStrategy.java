package com.shopease.ordermanagement.strategy;

import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.entity.Payment;
import com.shopease.ordermanagement.entity.PaymentMethod;

import java.math.BigDecimal;

/**
 * Strategy Interface (Behavioral Design Pattern)
 * Defines the contract that all concrete payment algorithms must implement.
 */
public interface PaymentStrategy {

    /**
     * Identifies the payment method handled by this strategy.
     */
    PaymentMethod getPaymentMethod();

    /**
     * Executes the payment processing algorithm.
     */
    Payment pay(Order order, BigDecimal amount);
}
