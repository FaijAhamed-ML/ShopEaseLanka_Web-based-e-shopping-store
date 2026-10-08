package com.shopease.ordermanagement.observer;

import com.shopease.ordermanagement.entity.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderObserverTest {

    @Test
    @DisplayName("Observer Pattern: OrderEventSubject broadcasts state changes to all attached observers")
    void testObserverNotificationFlow() {
        OrderEventSubject subject = new OrderEventSubject(new ArrayList<>());

        List<String> receivedEvents = new ArrayList<>();

        // Test Observer 1
        OrderObserver observer1 = (order, eventType, message) ->
                receivedEvents.add("Observer1:" + eventType + ":" + order.getOrderId());

        // Test Observer 2
        OrderObserver observer2 = (order, eventType, message) ->
                receivedEvents.add("Observer2:" + eventType + ":" + order.getOrderId());

        subject.addObserver(observer1);
        subject.addObserver(observer2);

        Order order = new Order();
        order.setOrderId(999);

        // Notify
        subject.notifyObservers(order, OrderEventType.ORDER_CONFIRMED, "Order confirmed");

        assertEquals(2, receivedEvents.size());
        assertTrue(receivedEvents.contains("Observer1:ORDER_CONFIRMED:999"));
        assertTrue(receivedEvents.contains("Observer2:ORDER_CONFIRMED:999"));

        // Detach observer 2
        subject.removeObserver(observer2);
        receivedEvents.clear();

        subject.notifyObservers(order, OrderEventType.ORDER_CANCELLED, "Order cancelled");
        assertEquals(1, receivedEvents.size());
        assertEquals("Observer1:ORDER_CANCELLED:999", receivedEvents.get(0));
    }
}
