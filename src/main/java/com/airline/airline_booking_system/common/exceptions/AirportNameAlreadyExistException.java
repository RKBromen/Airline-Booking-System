package com.airline.airline_booking_system.common.exceptions;

public class AirportNameAlreadyExistException extends RuntimeException {
    public AirportNameAlreadyExistException(String name) {
        super("Airport name: "+name+" already exists");
    }
}
