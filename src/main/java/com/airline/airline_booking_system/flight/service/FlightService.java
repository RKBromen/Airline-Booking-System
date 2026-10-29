package com.airline.airline_booking_system.flight.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.airline.airline_booking_system.airport.entity.Airports;
import com.airline.airline_booking_system.airport.repository.AirportRepository;
import com.airline.airline_booking_system.flight.dto.FlightRequest;
import com.airline.airline_booking_system.flight.dto.FlightResponse;
import com.airline.airline_booking_system.flight.entity.Flights;
import com.airline.airline_booking_system.flight.enums.FlightStatus;
import com.airline.airline_booking_system.flight.repository.FlightRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService {
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    public List<FlightResponse> getFlights(String keyword, FlightStatus status) {
        Specification<Flights> filter = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (status != null) {
            filter = filter.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            filter = filter.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("flight_number")), pattern, '\\'),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("departure_airport_id").get("code")),
                            pattern, '\\'),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("arrival_airport_id").get("code")),
                            pattern, '\\')));
        }
        return flightRepository.findAll(filter, Sort.by("flight_number")).stream().map(this::toResponse).toList();
    }

    public FlightResponse getFlight(UUID id) {
        return toResponse(findFlight(id));
    }

    @Transactional
    public FlightResponse createFlight(FlightRequest request) {
        String flightNumber = normalizeFlightNumber(request.flightNumber());
        validateFlightNumberAvailable(flightNumber, null);
        Airports departureAirport = findAirport(request.departureAirportId());
        Airports arrivalAirport = findAirport(request.arrivalAirportId());
        validateRoute(departureAirport, arrivalAirport, null);

        Flights flight = new Flights(UUID.randomUUID().toString(), flightNumber,
                departureAirport, arrivalAirport, FlightStatus.ACTIVE);
        return toResponse(saveWithConflictTranslation(flight));
    }

    @Transactional
    public FlightResponse updateFlight(UUID id, FlightRequest request) {
        Flights flight = findFlight(id);
        String flightNumber = normalizeFlightNumber(request.flightNumber());
        validateFlightNumberAvailable(flightNumber, flight.getId());
        Airports departureAirport = findAirport(request.departureAirportId());
        Airports arrivalAirport = findAirport(request.arrivalAirportId());
        validateRoute(departureAirport, arrivalAirport, flight.getId());

        flight.setFlight_number(flightNumber);
        flight.setDeparture_airport_id(departureAirport);
        flight.setArrival_airport_id(arrivalAirport);
        return toResponse(saveWithConflictTranslation(flight));
    }

    @Transactional
    public FlightResponse updateStatus(UUID id, FlightStatus status) {
        Flights flight = findFlight(id);
        flight.setStatus(status);
        return toResponse(flightRepository.save(flight));
    }

    private Flights findFlight(UUID id) {
        return flightRepository.findById(id.toString())
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + id));
    }

    private Airports findAirport(UUID id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Airport not found with id: " + id));
    }

    private String normalizeFlightNumber(String flightNumber) {
        return flightNumber.trim().toUpperCase(Locale.ROOT);
    }

    private void validateFlightNumberAvailable(String flightNumber, String excludedId) {
        if (flightRepository.findFlightByNumber(flightNumber, excludedId).isPresent()) {
            throw conflict("Flight number already exists: " + flightNumber);
        }
    }

    private void validateRoute(Airports departureAirport, Airports arrivalAirport, String excludedId) {
        if (departureAirport.getId().equals(arrivalAirport.getId())) {
            throw new IllegalArgumentException("Departure and arrival airports must be different");
        }
        flightRepository.findFlightOnRoute(departureAirport.getId(), arrivalAirport.getId(), excludedId)
                .ifPresent(existingId -> {
                    throw conflict("A flight already exists on this route");
                });
    }

    private Flights saveWithConflictTranslation(Flights flight) {
        try {
            return flightRepository.saveAndFlush(flight);
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && violation.getConstraintName() != null) {
                    String constraint = violation.getConstraintName().toLowerCase(Locale.ROOT);
                    if (constraint.equals("uk_flights_flight_number")
                            || constraint.endsWith(".uk_flights_flight_number")) {
                        throw conflict("Flight number already exists: " + flight.getFlight_number());
                    }
                    if (constraint.equals("uk_flights_route") || constraint.endsWith(".uk_flights_route")) {
                        throw conflict("A flight already exists on this route");
                    }
                }
            }
            throw exception;
        }
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private FlightResponse toResponse(Flights flight) {
        return new FlightResponse(UUID.fromString(flight.getId()), flight.getFlight_number(),
                flight.getDeparture_airport_id().getId(), flight.getDeparture_airport_id().getCode(),
                flight.getArrival_airport_id().getId(), flight.getArrival_airport_id().getCode(), flight.getStatus());
    }
}
