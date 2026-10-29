package com.airline.airline_booking_system.flightschedule.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.airline.airline_booking_system.flightschedule.enums.FlightScheduleStatus;

public record FlightScheduleResponse(
        UUID id,
        UUID flightId,
        String flightNumber,
        UUID aircraftId,
        String aircraftRegistration,
        String departureAirportCode,
        String arrivalAirportCode,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal basePrice,
        FlightScheduleStatus status) {
}
