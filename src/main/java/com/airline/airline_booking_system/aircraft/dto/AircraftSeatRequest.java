package com.airline.airline_booking_system.aircraft.dto;

import com.airline.airline_booking_system.aircraft.enums.SeatClass;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AircraftSeatRequest(
        @NotBlank @Size(max = 10) @Schema(example = "12A") String seatNumber,
        @NotNull @Schema(example = "ECONOMY") SeatClass seatClass) {
}
