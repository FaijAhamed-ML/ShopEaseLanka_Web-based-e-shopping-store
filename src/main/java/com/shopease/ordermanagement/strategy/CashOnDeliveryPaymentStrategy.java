package com.shopease.ordermanagement.strategy;

import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.entity.Payment;
import com.shopease.ordermanagement.entity.PaymentMethod;
import com.shopease.ordermanagement.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: Cash On Delivery (COD) Payment
 * Registers payment with status PENDING, to be collected upon parcel handover by the courier rider.
 */
@Component
public class CashOnDeliveryPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CASH_ON_DELIVERY;
    }

    @Override
    public Payment pay(Order order, BigDecimal amount) {
        // Physical cash collection upon arrival
        return new Payment(order, amount, PaymentMethod.CASH_ON_DELIVERY, PaymentStatus.PENDING);
    }
}
