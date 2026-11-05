package com.airline.airline_booking_system.flight.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.airline.airline_booking_system.flight.dto.FlightRequest;
import com.airline.airline_booking_system.flight.dto.FlightResponse;
import com.airline.airline_booking_system.flight.dto.FlightStatusRequest;
import com.airline.airline_booking_system.flight.enums.FlightStatus;
import com.airline.airline_booking_system.flight.service.FlightService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight routes and administration")
public class FlightController {
    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Search flights", description = "Filter by flight number, airport code and/or status.")
    public List<FlightResponse> getFlights(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) FlightStatus status) {
        return flightService.getFlights(keyword, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight details")
    public FlightResponse getFlight(@PathVariable UUID id) {
        return flightService.getFlight(id);
    }

    @PostMapping
    @Operation(summary = "Create a flight", description = "ADMIN required. Flight number and route must be unique.")
    @ApiResponse(responseCode = "201", description = "Flight created")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightRequest request) {
        FlightResponse flight = flightService.createFlight(request);
        return ResponseEntity.created(URI.create("/api/v1/flights/" + flight.id())).body(flight);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a flight", description = "ADMIN required. Updates flight number and both airports.")
    @SecurityRequirement(name = "bearerAuth")
    public FlightResponse updateFlight(@PathVariable UUID id, @Valid @RequestBody FlightRequest request) {
        return flightService.updateFlight(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change flight status", description = "ADMIN required. ACTIVE or INACTIVE.")
    @SecurityRequirement(name = "bearerAuth")
    public FlightResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody FlightStatusRequest request) {
        return flightService.updateStatus(id, request.status());
    }
}
