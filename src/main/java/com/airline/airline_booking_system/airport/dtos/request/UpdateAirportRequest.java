package com.airline.airline_booking_system.airport.dtos.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateAirportRequest(
        @Schema(description = "Optional. Must match the existing airport code.", example = "SGN")
        @Pattern(regexp = "[a-zA-Z]{3}") String code,
        @Schema(example = "Tan Son Nhat International Airport")
        @Pattern(regexp = "(?s).*\\S.*", message = "Airport name must not be blank") @Size(max = 150) String name,
        @Schema(example = "Ho Chi Minh City")
        @Pattern(regexp = "(?s).*\\S.*", message = "Airport city must not be blank") @Size(max = 100) String city,
        @Schema(example = "Vietnam")
        @Pattern(regexp = "(?s).*\\S.*", message = "Airport country must not be blank") @Size(max = 100) String country
) {
}
