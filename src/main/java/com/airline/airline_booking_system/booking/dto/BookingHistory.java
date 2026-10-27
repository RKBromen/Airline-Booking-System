package com.airline.airline_booking_system.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingHistory(String id, String bookingCode, LocalDateTime createAt, String status,
        BigDecimal totalAmount, String flightNumber, String departureAirport, String arrivalAirport,
        LocalDateTime departureTime, LocalDateTime arrivalTime) {

}
