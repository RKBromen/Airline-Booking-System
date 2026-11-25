
package com.airline.airline_booking_system.outbox.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingCancelledEvent(
        String bookingId,
        String bookingCode,
        String userId,
        String flightScheduleId,
        BigDecimal totalAmount,
        List<String> seatIds,
        LocalDateTime occurredAt) {
}
