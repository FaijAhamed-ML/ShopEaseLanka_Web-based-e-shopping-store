package com.shopease.ordermanagement.strategy;

import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.entity.Payment;
import com.shopease.ordermanagement.entity.PaymentMethod;
import com.shopease.ordermanagement.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: Credit/Debit Card Payment
 * Simulates real-time card authorization via a payment gateway.
 * The transaction is marked as COMPLETED upon successful placement.
 */
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public Payment pay(Order order, BigDecimal amount) {
        // Electronic card authorization simulation
        return new Payment(order, amount, PaymentMethod.CARD, PaymentStatus.COMPLETED);
    }
}
