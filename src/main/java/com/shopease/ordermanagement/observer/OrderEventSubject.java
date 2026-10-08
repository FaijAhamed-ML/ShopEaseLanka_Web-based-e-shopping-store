package com.shopease.ordermanagement.observer;

import com.shopease.ordermanagement.entity.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Subject (Observer Pattern)
 * Maintains the list of observers and broadcasts order lifecycle state changes
 * automatically to all registered observers.
 */
@Component
public class OrderEventSubject implements OrderSubject {

    private final List<OrderObserver> observers = new ArrayList<>();

    public OrderEventSubject(List<OrderObserver> initialObservers) {
        if (initialObservers != null) {
            this.observers.addAll(initialObservers);
        }
    }

    @Override
    public void addObserver(OrderObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Order order, OrderEventType eventType, String message) {
        for (OrderObserver observer : observers) {
            observer.update(order, eventType, message);
        }
    }
}
