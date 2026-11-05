package com.airline.airline_booking_system.booking.dto;

import java.math.*;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(String id, String bookingCode, String flightScheduleId, String status,
        BigDecimal totalAmount, LocalDateTime expiresAt, List<BookedPassengerResponse> passengers) {

}
