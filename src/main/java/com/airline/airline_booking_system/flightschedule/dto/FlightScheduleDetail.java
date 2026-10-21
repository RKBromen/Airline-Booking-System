package com.airline.airline_booking_system.flightschedule.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record FlightScheduleDetail(String id, String flightNumber, String departure_city, String departure_airport,
        String arrival_city, String arrival_airport, LocalDateTime departure_time, LocalDateTime arrival_time,
        LocalTime duration, String aircraft_number) {

}
