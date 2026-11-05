package com.airline.airline_booking_system.common.exceptions;

import java.util.UUID;

public class AirportNotFoundException extends  RuntimeException {
    public AirportNotFoundException(UUID id)
    {
        super("Airport not found with id: "+id );
    }
}
