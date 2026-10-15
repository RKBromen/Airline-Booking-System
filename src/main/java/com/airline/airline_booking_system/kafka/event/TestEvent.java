package com.airline.airline_booking_system.kafka.event;
public record TestEvent(
        String eventId,
        String message
) {}
