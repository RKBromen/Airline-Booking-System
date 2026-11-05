package com.airline.airline_booking_system.flightschedule.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.airline.airline_booking_system.aircraft.enums.SeatClass;
import com.airline.airline_booking_system.flightschedule.enums.FlightSeatStatus;

public record FlightScheduleSeatResponse(
        UUID id,
        String seatNumber,
        SeatClass seatClass,
        BigDecimal price,
        FlightSeatStatus status) {
}
