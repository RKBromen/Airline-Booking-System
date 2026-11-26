package com.airline.airline_booking_system.flightsearch.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
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

    public FlightSeatListResponse seatChart(String scheduleId) {
        FlightScheduleDetail schedule = findScheduleById(scheduleId);
        List<FlightSeatResponse> seats = repository.seatingChart(scheduleId);
        return new FlightSeatListResponse(schedule.id(), seats);
    }

    public FlightScheduleDetail findScheduleById(String scheduleId) {
        FlightScheduleDetail schedule = repository.getScheduleById(scheduleId);
        if (schedule == null || schedule.id() == null) {
            throw new EntityNotFoundException("Flight schedule not found with id: " + scheduleId);
        }
        return schedule;
    }
}
