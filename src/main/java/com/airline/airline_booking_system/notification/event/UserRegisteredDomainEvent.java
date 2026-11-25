package com.airline.airline_booking_system.notification.event;

import java.time.Instant;

public record UserRegisteredDomainEvent(
        String eventId,
        String userId,
        String email,
        String fullName,
        Instant registeredAt
) { }
