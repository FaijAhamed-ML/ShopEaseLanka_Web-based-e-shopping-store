package com.shopease.deliverymanagement.service;

import com.shopease.deliverymanagement.dto.DeliveryDtos.UpdateShipmentRequest;
import com.shopease.deliverymanagement.entity.Delivery;
import com.shopease.deliverymanagement.entity.ShipmentStatus;
import com.shopease.deliverymanagement.repository.DeliveryAttemptRepository;
import com.shopease.deliverymanagement.repository.DeliveryRepository;
import com.shopease.ordermanagement.repository.OrderRepository;
import com.shopease.reviewmanagement.service.NotificationService;
import com.shopease.usermanagement.entity.User;
import com.shopease.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryStatusTransitionTest {

    private DeliveryRepository deliveryRepository;
    private NotificationService notificationService;
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryRepository = mock(DeliveryRepository.class);
        DeliveryAttemptRepository deliveryAttemptRepository = mock(DeliveryAttemptRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        OrderRepository orderRepository = mock(OrderRepository.class);
        notificationService = mock(NotificationService.class);

        deliveryService = new DeliveryService(
                deliveryRepository,
                deliveryAttemptRepository,
                userRepository,
                orderRepository,
                notificationService
        );
    }

    @Test
    @DisplayName("Reject invalid status transition: Delivery in PROCESSING directly to DELIVERED must be rejected")
    void testRejectInvalidStatusTransition_ProcessingToDelivered() {
        // Arrange: Delivery record #3 is in 'PROCESSING'
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(3);
        delivery.setTrackingNumber("SEL-LK-102938");
        delivery.setShipmentStatus(ShipmentStatus.PROCESSING);
        
        User rider = new User();
        rider.setUserId(7);
        rider.setUsername("rider_kamal");
        delivery.setDeliveryPerson(rider);

        when(deliveryRepository.findById(3)).thenReturn(Optional.of(delivery));

        // Act & Assert: Attempt invalid jump to DELIVERED
        UpdateShipmentRequest request = new UpdateShipmentRequest();
        request.setShipmentStatus(ShipmentStatus.DELIVERED);
        request.setNotes("Skipping straight to delivered");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateShipmentStatus(3, request),
                "Should reject direct transition from PROCESSING to DELIVERED"
        );

        assertTrue(exception.getMessage().contains("Cannot transition delivery from PROCESSING to DELIVERED"));
        assertTrue(exception.getMessage().contains("Sequential stages cannot be skipped"));

        // Verify state is unchanged
        assertEquals(ShipmentStatus.PROCESSING, delivery.getShipmentStatus(), "Delivery status must remain unchanged");
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Permit valid sequential status transition: PROCESSING -> DISPATCHED when driver is assigned")
    void testPermitValidSequentialTransition_WithAssignedDriver() {
        // Arrange: Delivery record #3 is in 'PROCESSING' with an assigned driver
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(3);
        delivery.setTrackingNumber("SEL-LK-102938");
        delivery.setShipmentStatus(ShipmentStatus.PROCESSING);
        
        User rider = new User();
        rider.setUserId(7);
        rider.setUsername("rider_kamal");
        delivery.setDeliveryPerson(rider);

        when(deliveryRepository.findById(3)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Valid transition to DISPATCHED
        UpdateShipmentRequest request = new UpdateShipmentRequest();
        request.setShipmentStatus(ShipmentStatus.DISPATCHED);
        request.setNotes("Dispatched from warehouse with assigned driver");

        Delivery updated = deliveryService.updateShipmentStatus(3, request);

        // Assert
        assertEquals(ShipmentStatus.DISPATCHED, updated.getShipmentStatus());
        verify(deliveryRepository, atLeastOnce()).save(delivery);
    }

    @Test
    @DisplayName("Reject status change from initial status (PROCESSING) without an assigned driver")
    void testRejectStatusChangeWithoutAssignedDriver_FromInitialStatus() {
        // Arrange: Delivery record #3 is in initial status 'PROCESSING' without an assigned driver
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(3);
        delivery.setTrackingNumber("SEL-LK-102938");
        delivery.setShipmentStatus(ShipmentStatus.PROCESSING);
        delivery.setDeliveryPerson(null); // No driver assigned

        when(deliveryRepository.findById(3)).thenReturn(Optional.of(delivery));

        // Act & Assert: Attempt to change status to DISPATCHED without driver assignment
        UpdateShipmentRequest request = new UpdateShipmentRequest();
        request.setShipmentStatus(ShipmentStatus.DISPATCHED);
        request.setNotes("Attempting dispatch without driver assignment");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> deliveryService.updateShipmentStatus(3, request),
                "Should reject status change from initial PROCESSING status when driver is not assigned"
        );

        assertTrue(exception.getMessage().contains("A delivery driver must be assigned before changing from initial status (PROCESSING)"));

        // Verify status remains unchanged
        assertEquals(ShipmentStatus.PROCESSING, delivery.getShipmentStatus(), "Delivery status must remain in PROCESSING");
        verify(deliveryRepository, never()).save(any());
    }
}
