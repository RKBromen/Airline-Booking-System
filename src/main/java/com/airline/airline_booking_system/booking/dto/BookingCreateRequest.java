package com.airline.airline_booking_system.booking.dto;

import java.util.List;

public record BookingCreateRequest(String flightScheduleId, List<PassengerCreateRequest> passengers) {

}
