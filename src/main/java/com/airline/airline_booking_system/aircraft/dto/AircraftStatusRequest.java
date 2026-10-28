package com.airline.airline_booking_system.aircraft.dto;

import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AircraftStatusRequest(@NotNull @Schema(example = "BLOCKED") AircraftStatus status) {
}
