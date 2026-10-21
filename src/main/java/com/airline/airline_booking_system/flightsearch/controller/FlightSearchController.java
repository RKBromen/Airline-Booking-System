package com.airline.airline_booking_system.flightsearch.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleDetail;
import com.airline.airline_booking_system.flightschedule.dto.FlightSeatListResponse;
import com.airline.airline_booking_system.flightschedule.dto.SearchFlight;
import com.airline.airline_booking_system.flightsearch.service.FlightSearchService;

@RestController
@RequestMapping("api/v1/search/flights")
public class FlightSearchController {
    private final FlightSearchService service;

    public FlightSearchController(FlightSearchService service) {
        this.service = service;
    }

    @GetMapping
    public List<SearchFlight> searchFlights(@RequestParam("departure") String departure,
            @RequestParam("arrival") String arrival,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // System.out.println("CONTROLLER CALLED: " + departure + " " + arrival + " " +
        // date);
        return service.searchFlights(departure, arrival, date);
    }

    @GetMapping("/{scheduleId}/seats")
    public FlightSeatListResponse searchSeats(@PathVariable("scheduleId") String schedule_id) {
        return service.seatChart(schedule_id);
    }

    @GetMapping("/{scheduleId}")
    public FlightScheduleDetail getFlightScheduleDetail(@PathVariable("scheduleId") String schedule_id) {
        return service.findScheduleById(schedule_id);
    }

}
