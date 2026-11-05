package com.airline.airline_booking_system.aircraft.dto;

import com.airline.airline_booking_system.aircraft.enums.SeatClass;

public record AircraftSeatResponse(String id, String aircraftId, String seatNumber, SeatClass seatClass) {
}
