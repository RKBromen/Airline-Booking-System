package com.airline.airline_booking_system.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.airline.airline_booking_system.flight.dto.FlightDTO;

public record BookingDetailByCode(String bookingCode, String status, FlightDTO flight,
        List<BookingPassengerDTO> passengers, BigDecimal totalAmount, LocalDateTime expiresAt) {

}
