package com.airline.airline_booking_system.airport.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record AirportRequest(
        @NotBlank(message = "Airport code is required")
        @Pattern(regexp = "[a-zA-Z]{3}", message = "Airport code must contain exactly three letters")
        @Schema(example = "SGN")
        String code,

        @NotBlank(message="Airport name is required")
        @Size(max = 150)
        @Schema(example = "Tan Son Nhat International Airport")
        String name,

        @NotBlank(message= "Airport city is required")
        @Size(max = 100)
        @Schema(example = "Ho Chi Minh City")
        String city,

        @NotBlank(message = "Airport country is required")
        @Size(max = 100)
        @Schema(example = "Vietnam")
        String country
) {
}
