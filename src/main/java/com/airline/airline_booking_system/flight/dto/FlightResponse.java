package com.airline.airline_booking_system.flight.dto;

import java.util.UUID;

import com.airline.airline_booking_system.flight.enums.FlightStatus;

public record FlightResponse(
        UUID id,
        String flightNumber,
        UUID departureAirportId,
        String departureAirportCode,
        UUID arrivalAirportId,
        String arrivalAirportCode,
        FlightStatus status) {
}
