package com.airline.airline_booking_system.airport.dtos.response;

import java.util.UUID;
import com.airline.airline_booking_system.airport.enums.AirportStatus;

public record AirportResponse (
    UUID id,
    String code,
    String name,
    String city,
    String country,
    AirportStatus status
){}
