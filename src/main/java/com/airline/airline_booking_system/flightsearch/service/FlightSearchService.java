package com.airline.airline_booking_system.flightsearch.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.flight.repository.FlightRepository;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleDetail;
import com.airline.airline_booking_system.flightschedule.dto.FlightSeatListResponse;
import com.airline.airline_booking_system.flightschedule.dto.FlightSeatResponse;
import com.airline.airline_booking_system.flightschedule.dto.SearchFlight;

@Service
public class FlightSearchService {
    private final FlightRepository repository;

    public FlightSearchService(FlightRepository repository) {
        this.repository = repository;
    }

    public List<SearchFlight> searchFlights(String departure, String arrival, LocalDate date) {
        return repository.searchFlight(departure, arrival, date);
    }

    public FlightSeatListResponse seatChart(String schedule_id) {
        List<FlightSeatResponse> seats = repository.seatingChart(schedule_id);
        return new FlightSeatListResponse(schedule_id, seats);
    }

    public FlightScheduleDetail findScheduleById(String schedule_id) {
        FlightScheduleDetail response = repository.getScheduleById(schedule_id);
        return response;
    }
}
