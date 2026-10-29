package com.airline.airline_booking_system.flight.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FlightRequest(
        @NotBlank(message = "Flight number is required")
        @Size(max = 20, message = "Flight number must be at most 20 characters")
        @Schema(example = "AB123")
        String flightNumber,

        @NotNull(message = "Departure airport is required")
        @Schema(example = "5a55f8a6-0d2f-4f34-93be-7a13ee932501")
        UUID departureAirportId,

        @NotNull(message = "Arrival airport is required")
        @Schema(example = "b66da1d1-1dc4-48b4-9c2a-25624b890e4a")
        UUID arrivalAirportId) {
}
