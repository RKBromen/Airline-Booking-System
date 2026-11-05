package com.airline.airline_booking_system.user.service;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException() {
        super("An account with this email already exists");
    }
}
