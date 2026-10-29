package com.airline.airline_booking_system.flightschedule.dto;

import com.airline.airline_booking_system.flightschedule.enums.FlightScheduleStatus;
import jakarta.validation.constraints.NotNull;

public record FlightScheduleStatusRequest(@NotNull FlightScheduleStatus status) {
}
