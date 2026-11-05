package com.airline.airline_booking_system.booking.dto;

public record PassengerCreateRequest(String fullName, String dateOfBirth, String gender, String documentNumber,
        String flightSeatId) {

}
