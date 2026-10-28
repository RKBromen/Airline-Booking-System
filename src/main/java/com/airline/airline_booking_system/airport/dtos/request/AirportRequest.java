package com.airline.airline_booking_system.airport.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record AirportRequest(
        @NotBlank(message = "Airport code is required")
        String code,

        @NotBlank(message="Airport name is required")
        String name,

        @NotBlank(message= "Airport city is required")
        String city,

        @NotBlank(message = "Airport country is required")
        String country
) {
}
