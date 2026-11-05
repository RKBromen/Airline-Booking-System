package com.airline.airline_booking_system.aircraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AircraftRequest(
        @NotBlank @Size(max = 50) @Schema(example = "VN-A321") String registrationNo) {
}
