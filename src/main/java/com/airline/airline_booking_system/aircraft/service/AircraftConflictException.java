package com.airline.airline_booking_system.aircraft.service;

public class AircraftConflictException extends RuntimeException {
    public AircraftConflictException(String message) {
        super(message);
    }
}
