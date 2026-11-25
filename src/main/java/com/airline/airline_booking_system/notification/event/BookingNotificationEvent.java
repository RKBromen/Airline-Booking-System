package com.airline.airline_booking_system.notification.event;

import java.time.Instant;

/** Stable JSON contract expected from the booking event publisher. */
public interface BookingNotificationEvent {
    String eventId();
    int eventVersion();
    String bookingId();
    String bookingCode();
    String recipientEmail();
    String recipientName();
    String flightNumber();
    Instant departureTime();
}
