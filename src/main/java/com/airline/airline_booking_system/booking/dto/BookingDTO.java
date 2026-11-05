package com.airline.airline_booking_system.booking.dto;

import java.math.*;
import java.time.LocalDateTime;

public record BookingDTO(String id, String bookingCode, LocalDateTime createdAt, String status, String fullName,
        String flightNumber, String departureAirport, String arrivalAirport, LocalDateTime departureTime,
        BigDecimal totalAmount) {

}
