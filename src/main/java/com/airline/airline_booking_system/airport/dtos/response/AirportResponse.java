package com.airline.airline_booking_system.airport.dtos.response;

import java.util.UUID;

public record AirportResponse (
    UUID id,
    String code,
    String name,
    String city,
    String country
){}
