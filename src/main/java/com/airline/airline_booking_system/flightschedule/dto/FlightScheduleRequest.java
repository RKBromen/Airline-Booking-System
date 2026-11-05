package com.airline.airline_booking_system.flightschedule.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record FlightScheduleRequest(
        @NotNull(message = "Flight is required")
        @Schema(example = "5a55f8a6-0d2f-4f34-93be-7a13ee932501")
        UUID flightId,

        @NotNull(message = "Aircraft is required")
        @Schema(example = "b66da1d1-1dc4-48b4-9c2a-25624b890e4a")
        UUID aircraftId,

        @NotNull(message = "Departure time is required")
        @Schema(example = "2026-12-01T08:30:00")
        LocalDateTime departureTime,

        @NotNull(message = "Arrival time is required")
        @Schema(example = "2026-12-01T10:45:00")
        LocalDateTime arrivalTime,

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.00", message = "Base price must not be negative")
        @Digits(integer = 13, fraction = 2, message = "Base price must have at most 2 decimal places")
        @Schema(example = "125.00")
        BigDecimal basePrice) {
}
