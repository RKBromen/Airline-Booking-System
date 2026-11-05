package com.airline.airline_booking_system.aircraft.dto;

import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;

public record AircraftResponse(String id, String registrationNo, AircraftStatus status) {
}
