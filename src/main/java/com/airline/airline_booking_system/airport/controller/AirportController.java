package com.airline.airline_booking_system.airport.controller;

import com.airline.airline_booking_system.airport.dtos.request.AirportRequest;
import com.airline.airline_booking_system.airport.dtos.response.AirportResponse;
import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.service.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @GetMapping
    public ResponseEntity<List<AirportResponse>> getAllAirportsByStats(){
        List<AirportResponse> airports = airportService.getAllAirportsByStatus();

        return  ResponseEntity.ok(airports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportResponse> getAirportByIdAndStatus(UUID id)
    {
        AirportResponse airport= airportService.getAirportByIdAndStatus(id);
        return ResponseEntity.ok(airport);
    }

    @PostMapping
    public ResponseEntity<AirportResponse> createAirport(AirportRequest request)
    {
        AirportResponse airport = airportService.createAirport(request);
        return ResponseEntity.ok(airport);
    }

}
