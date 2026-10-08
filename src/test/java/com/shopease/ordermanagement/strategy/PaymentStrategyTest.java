package com.shopease.ordermanagement.strategy;

import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.entity.Payment;
import com.shopease.ordermanagement.entity.PaymentMethod;
import com.shopease.ordermanagement.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStrategyTest {

    @Test
    @DisplayName("Strategy Pattern: Card Payment produces COMPLETED payment status")
    void testCardPaymentStrategy() {
        PaymentStrategy cardStrategy = new CardPaymentStrategy();
        assertEquals(PaymentMethod.CARD, cardStrategy.getPaymentMethod());

        Order order = new Order();
        order.setOrderId(101);
        BigDecimal amount = new BigDecimal("4500.00");

        Payment payment = cardStrategy.pay(order, amount);
        assertNotNull(payment);
        assertEquals(PaymentMethod.CARD, payment.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus());
        assertEquals(amount, payment.getAmount());
    }

    @Test
    @DisplayName("Strategy Pattern: Cash On Delivery produces PENDING payment status")
    void testCashOnDeliveryStrategy() {
        PaymentStrategy codStrategy = new CashOnDeliveryPaymentStrategy();
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, codStrategy.getPaymentMethod());

        Order order = new Order();
        order.setOrderId(102);
        BigDecimal amount = new BigDecimal("2500.00");

        Payment payment = codStrategy.pay(order, amount);
        assertNotNull(payment);
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, payment.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
        assertEquals(amount, payment.getAmount());
    }

    @Test
    @DisplayName("Strategy Pattern: PaymentContext dynamically selects appropriate strategy")
    void testPaymentContextDynamicSwitching() {
        PaymentStrategy cardStrategy = new CardPaymentStrategy();
        PaymentStrategy codStrategy = new CashOnDeliveryPaymentStrategy();

        PaymentContext context = new PaymentContext(List.of(cardStrategy, codStrategy));

        Order order = new Order();
        order.setOrderId(103);
        BigDecimal amount = new BigDecimal("3200.00");

        Payment cardPayment = context.executePayment(PaymentMethod.CARD, order, amount);
        assertEquals(PaymentStatus.COMPLETED, cardPayment.getPaymentStatus());

        Payment codPayment = context.executePayment(PaymentMethod.CASH_ON_DELIVERY, order, amount);
        assertEquals(PaymentStatus.PENDING, codPayment.getPaymentStatus());
    }
}
