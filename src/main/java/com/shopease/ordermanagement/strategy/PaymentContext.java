package com.shopease.ordermanagement.strategy;

import com.shopease.ordermanagement.entity.Order;
import com.shopease.ordermanagement.entity.Payment;
import com.shopease.ordermanagement.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Context Class (Strategy Pattern)
 * Holds a reference to available PaymentStrategy implementations and executes
 * the appropriate algorithm at runtime without tightly coupled if-else chains.
 */
@Component
public class PaymentContext {

    private final Map<PaymentMethod, PaymentStrategy> strategyMap = new EnumMap<>(PaymentMethod.class);

    public PaymentContext(List<PaymentStrategy> strategies) {
        if (strategies != null) {
            for (PaymentStrategy strategy : strategies) {
                this.strategyMap.put(strategy.getPaymentMethod(), strategy);
            }
        }
    }

    /**
     * Executes the appropriate payment strategy based on customer choice.
     */
    public Payment executePayment(PaymentMethod method, Order order, BigDecimal amount) {
        PaymentStrategy strategy = strategyMap.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("No payment strategy registered for method: " + method);
        }
        return strategy.pay(order, amount);
    }
}
