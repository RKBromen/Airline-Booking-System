package com.airline.airline_booking_system.flight.dto;

import java.time.LocalDateTime;

public record FlightDTO(String flight_number, String departureAirport, String arrivalAirport,
        LocalDateTime departureTime) {

}
