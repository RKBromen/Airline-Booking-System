package com.airline.airline_booking_system.flight.dto;

import com.airline.airline_booking_system.flight.enums.FlightStatus;
import jakarta.validation.constraints.NotNull;

public record FlightStatusRequest(@NotNull FlightStatus status) {
}
