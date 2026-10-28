package com.airline.airline_booking_system.aircraft.dto;

import com.airline.airline_booking_system.aircraft.enums.SeatClass;

public record AircraftSeatListItem(String id, String seatNumber, SeatClass seatClass) {
}
