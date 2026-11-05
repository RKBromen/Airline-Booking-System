package com.airline.airline_booking_system.auth.service;

public class OAuthAccountConflictException extends RuntimeException {
    public OAuthAccountConflictException(String message) { super(message); }
}
