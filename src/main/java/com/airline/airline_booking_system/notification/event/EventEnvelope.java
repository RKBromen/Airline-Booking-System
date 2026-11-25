package com.airline.airline_booking_system.notification.event;

import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;

/** Generic event metadata contract; the payload remains independent of booking entities. */
public record EventEnvelope(
        String eventId,
        String aggregateId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        JsonNode payload
) { }
