package com.airline.airline_booking_system.airport.controller;

import com.airline.airline_booking_system.airport.dtos.request.AirportRequest;
import com.airline.airline_booking_system.airport.dtos.request.AirportStatusRequest;
import com.airline.airline_booking_system.airport.dtos.request.UpdateAirportRequest;
import com.airline.airline_booking_system.airport.dtos.response.AirportResponse;
import com.airline.airline_booking_system.airport.enums.AirportStatus;
import com.airline.airline_booking_system.airport.service.AirportService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
@Tag(name = "Airports", description = "Airport directory and administration")
public class AirportController {
    private final AirportService airportService;

    @GetMapping
    @Operation(summary = "Search airports", description = "Public. Filter by keyword and/or status; omit filters to list all airports.")
    public List<AirportResponse> getAirports(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) AirportStatus status) {
        return airportService.getAirports(keyword, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get airport details")
    public AirportResponse getAirportById(@PathVariable UUID id) {
        return airportService.getAirportById(id);
    }

    @PostMapping
    @Operation(summary = "Create an airport", description = "ADMIN required. Code must contain exactly three letters.")
    @ApiResponse(responseCode = "201", description = "Airport created")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AirportResponse> createAirport(@Valid @RequestBody AirportRequest request) {
        AirportResponse airport = airportService.createAirport(request);
        return ResponseEntity.created(URI.create("/api/v1/airports/" + airport.id())).body(airport);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace airport details", description = "ADMIN required. Supply name, city and country. Airport code cannot change.")
    @SecurityRequirement(name = "bearerAuth")
    public AirportResponse replaceAirport(@PathVariable UUID id, @Valid @RequestBody UpdateAirportRequest request) {
        return airportService.updateAirport(id, request, false);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update airport details", description = "ADMIN required. Only supplied fields are updated. Airport code cannot change.")
    @SecurityRequirement(name = "bearerAuth")
    public AirportResponse updateAirport(@PathVariable UUID id, @Valid @RequestBody UpdateAirportRequest request) {
        return airportService.updateAirport(id, request, true);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change airport status", description = "ADMIN required. ACTIVE, BLOCKED or DELETED.")
    @SecurityRequirement(name = "bearerAuth")
    public AirportResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody AirportStatusRequest request) {
        return airportService.updateStatus(id, request.status());
    }
}
