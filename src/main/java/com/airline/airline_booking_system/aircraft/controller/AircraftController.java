package com.airline.airline_booking_system.aircraft.controller;

import com.airline.airline_booking_system.aircraft.dto.*;
import com.airline.airline_booking_system.aircraft.service.AircraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aircrafts")
@RequiredArgsConstructor
@Tag(name = "Aircrafts", description = "Aircraft directory and physical seat layout. Reads require ADMIN or STAFF; writes require ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class AircraftController {
    private final AircraftService aircraftService;

    @GetMapping
    @Operation(summary = "List aircraft", description = "ADMIN or STAFF required.")
    public List<AircraftResponse> listAircrafts() {
        return aircraftService.listAircrafts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get aircraft details", description = "ADMIN or STAFF required.")
    public AircraftResponse getAircraft(@PathVariable UUID id) {
        return aircraftService.getAircraft(id);
    }

    @PostMapping
    @Operation(summary = "Create an aircraft", description = "ADMIN required. Registration is unique; status defaults to ACTIVE.")
    @ApiResponse(responseCode = "201", description = "Aircraft created")
    public ResponseEntity<AircraftResponse> createAircraft(@Valid @RequestBody AircraftRequest request) {
        AircraftResponse result = aircraftService.createAircraft(request);
        return ResponseEntity.created(URI.create("/api/v1/aircrafts/" + result.id())).body(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update aircraft registration", description = "ADMIN required. Existing seats and status are preserved.")
    public AircraftResponse updateAircraft(@PathVariable UUID id, @Valid @RequestBody AircraftRequest request) {
        return aircraftService.updateAircraft(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change aircraft status", description = "ADMIN required. ACTIVE, BLOCKED or DELETED.")
    public AircraftResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody AircraftStatusRequest request) {
        return aircraftService.updateStatus(id, request.status());
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "List physical seats", description = "ADMIN or STAFF required. Returns an empty list when the aircraft has no seats.")
    public List<AircraftSeatListItem> listSeats(@PathVariable UUID id) {
        return aircraftService.listSeats(id);
    }

    @PostMapping("/{id}/seats")
    @Operation(summary = "Add a physical seat", description = "ADMIN required. Seat number must be unique within this aircraft. Class: ECONOMY, BUSINESS or FIRST.")
    @ApiResponse(responseCode = "201", description = "Physical seat created")
    public ResponseEntity<AircraftSeatResponse> addSeat(@PathVariable UUID id, @Valid @RequestBody AircraftSeatRequest request) {
        AircraftSeatResponse result = aircraftService.addSeat(id, request);
        return ResponseEntity.status(201).body(result);
    }
}
