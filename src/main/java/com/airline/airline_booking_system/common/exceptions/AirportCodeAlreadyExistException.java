package com.airline.airline_booking_system.common.exceptions;

import java.util.UUID;

public class AirportCodeAlreadyExistException extends  RuntimeException {
    public AirportCodeAlreadyExistException(String code)

    {
        super("Airport code: "+code+" already exists" );
    }
}