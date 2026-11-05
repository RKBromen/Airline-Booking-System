package com.airline.airline_booking_system.flightschedule.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleRequest;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleResponse;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleSeatResponse;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleStatusRequest;
import com.airline.airline_booking_system.flightschedule.enums.FlightScheduleStatus;
import com.airline.airline_booking_system.flightschedule.service.FlightScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flight-schedules")
@RequiredArgsConstructor
@Tag(name = "Flight schedules", description = "Flight timetables, assigned aircraft and scheduled seat inventory")
public class FlightScheduleController {
    private final FlightScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "Search flight schedules", description = "Filter by flight, aircraft and/or schedule status.")
    public List<FlightScheduleResponse> getSchedules(
            @RequestParam(required = false) UUID flightId,
            @RequestParam(required = false) UUID aircraftId,
            @RequestParam(required = false) FlightScheduleStatus status) {
        return scheduleService.getSchedules(flightId, aircraftId, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight schedule details")
    public FlightScheduleResponse getSchedule(@PathVariable UUID id) {
        return scheduleService.getSchedule(id);
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "List seats available for a flight schedule")
    public List<FlightScheduleSeatResponse> getSeats(@PathVariable UUID id) {
        return scheduleService.getSeats(id);
    }

    @PostMapping
    @Operation(summary = "Create a flight schedule",
            description = "ADMIN required. Creates available flight seats from the assigned aircraft layout.")
    @ApiResponse(responseCode = "201", description = "Flight schedule created")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FlightScheduleResponse> createSchedule(
            @Valid @RequestBody FlightScheduleRequest request) {
        FlightScheduleResponse schedule = scheduleService.createSchedule(request);
        return ResponseEntity.created(URI.create("/api/v1/flight-schedules/" + schedule.id())).body(schedule);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change flight schedule status", description = "ADMIN required. Status transitions follow the flight lifecycle.")
    @SecurityRequirement(name = "bearerAuth")
    public FlightScheduleResponse updateStatus(@PathVariable UUID id,
            @Valid @RequestBody FlightScheduleStatusRequest request) {
        return scheduleService.updateStatus(id, request.status());
    }
}
