package com.airline.airline_booking_system.airport.dtos.request;

import com.airline.airline_booking_system.airport.enums.AirportStatus;
import jakarta.validation.constraints.NotNull;

public record AirportStatusRequest(@NotNull AirportStatus status) {
}
