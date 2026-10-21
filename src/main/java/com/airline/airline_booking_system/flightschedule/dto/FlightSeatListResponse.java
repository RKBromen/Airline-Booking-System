package com.airline.airline_booking_system.flightschedule.dto;

import java.util.List;

public record FlightSeatListResponse(
        String scheduleId,
        List<FlightSeatResponse> seats) {
}
