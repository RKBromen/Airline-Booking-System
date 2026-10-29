package com.airline.airline_booking_system.flightschedule.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import com.airline.airline_booking_system.aircraft.repository.AircraftRepository;
import com.airline.airline_booking_system.aircraft.repository.AircraftSeatRepository;
import com.airline.airline_booking_system.booking.entity.Bookings.BookingStatus;
import com.airline.airline_booking_system.flight.entity.Flights;
import com.airline.airline_booking_system.flight.enums.FlightStatus;
import com.airline.airline_booking_system.flight.repository.FlightRepository;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleRequest;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleResponse;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleSeatResponse;
import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;
import com.airline.airline_booking_system.flightschedule.enums.FlightScheduleStatus;
import com.airline.airline_booking_system.flightschedule.enums.FlightSeatStatus;
import com.airline.airline_booking_system.flightschedule.repository.FlightScheduleRepository;
import com.airline.airline_booking_system.flightschedule.repository.FlightSeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightScheduleService {
    private final FlightScheduleRepository scheduleRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;
    private final AircraftSeatRepository aircraftSeatRepository;

    public List<FlightScheduleResponse> getSchedules(UUID flightId, UUID aircraftId, FlightScheduleStatus status) {
        Specification<FlightSchedules> filter = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (flightId != null) {
            filter = filter.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("flights").get("id"), flightId.toString()));
        }
        if (aircraftId != null) {
            filter = filter.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("aircrafts").get("id"), aircraftId.toString()));
        }
        if (status != null) {
            filter = filter.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }
        return scheduleRepository.findAll(filter, Sort.by(Sort.Direction.ASC, "departure_time"))
                .stream().map(this::toResponse).toList();
    }

    public FlightScheduleResponse getSchedule(UUID id) {
        return toResponse(findSchedule(id));
    }

    public List<FlightScheduleSeatResponse> getSeats(UUID id) {
        FlightSchedules schedule = findSchedule(id);
        return flightSeatRepository.findAllByScheduleId(schedule.getId()).stream()
                .map(this::toSeatResponse)
                .toList();
    }

    @Transactional
    public FlightScheduleResponse createSchedule(FlightScheduleRequest request) {
        validateScheduleData(request.departureTime(), request.arrivalTime(), request.basePrice());

        Flights flight = flightRepository.findById(request.flightId().toString())
                .orElseThrow(() -> new EntityNotFoundException("Flight not found with id: " + request.flightId()));
        if (flight.getStatus() != FlightStatus.ACTIVE) {
            throw conflict("Cannot schedule an inactive flight");
        }

        Aircrafts aircraft = aircraftRepository.findById(request.aircraftId().toString())
                .orElseThrow(() -> new EntityNotFoundException("Aircraft not found with id: " + request.aircraftId()));
        if (aircraft.getStatus() != AircraftStatus.ACTIVE) {
            throw conflict("Cannot assign an inactive aircraft");
        }

        List<AircraftSeats> aircraftSeats = aircraftSeatRepository
                .findByAircraftsIdOrderBySeatNumberAsc(aircraft.getId());
        if (aircraftSeats.isEmpty()) {
            throw conflict("Aircraft must have at least one configured seat before scheduling");
        }
        validateAircraftAvailability(aircraft.getId(), request.departureTime(), request.arrivalTime(), null);

        FlightSchedules schedule = new FlightSchedules(UUID.randomUUID().toString(), flight, aircraft,
                request.departureTime(), request.arrivalTime(), request.basePrice(), FlightScheduleStatus.SCHEDULED);
        FlightSchedules savedSchedule = scheduleRepository.saveAndFlush(schedule);

        List<FlightSeats> flightSeats = new ArrayList<>(aircraftSeats.size());
        for (AircraftSeats aircraftSeat : aircraftSeats) {
            flightSeats.add(new FlightSeats(UUID.randomUUID().toString(), savedSchedule, aircraftSeat,
                    FlightSeatStatus.AVAILABLE, request.basePrice()));
        }
        flightSeatRepository.saveAll(flightSeats);
        return toResponse(savedSchedule);
    }

    @Transactional
    public FlightScheduleResponse updateStatus(UUID id, FlightScheduleStatus requestedStatus) {
        FlightSchedules schedule = findSchedule(id);
        FlightScheduleStatus currentStatus = schedule.getStatus();
        if (!isAllowedTransition(currentStatus, requestedStatus)) {
            throw new IllegalArgumentException(
                    "Cannot change flight schedule status from " + currentStatus + " to " + requestedStatus);
        }
        if (requestedStatus == FlightScheduleStatus.CANCELLED
                && scheduleRepository.countActiveBookings(schedule.getId(), BookingStatus.CANCELLED,
                        BookingStatus.EXPIRED) > 0) {
            throw conflict("Cannot cancel a schedule that has active bookings");
        }
        schedule.setStatus(requestedStatus);
        return toResponse(scheduleRepository.save(schedule));
    }

    private FlightSchedules findSchedule(UUID id) {
        return scheduleRepository.findById(id.toString())
                .orElseThrow(() -> new EntityNotFoundException("Flight schedule not found with id: " + id));
    }

    private void validateScheduleData(java.time.LocalDateTime departureTime,
            java.time.LocalDateTime arrivalTime, BigDecimal basePrice) {
        if (departureTime == null || arrivalTime == null || !arrivalTime.isAfter(departureTime)) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }
        if (basePrice == null || basePrice.signum() < 0 || basePrice.scale() > 2) {
            throw new IllegalArgumentException("Base price must be non-negative with at most 2 decimal places");
        }
    }

    private void validateAircraftAvailability(String aircraftId, java.time.LocalDateTime departureTime,
            java.time.LocalDateTime arrivalTime, String excludedScheduleId) {
        scheduleRepository.findOverlappingSchedule(aircraftId, departureTime, arrivalTime,
                FlightScheduleStatus.CANCELLED, excludedScheduleId)
                .ifPresent(existingId -> {
                    throw conflict("Aircraft already has a schedule that overlaps this time");
                });
    }

    private boolean isAllowedTransition(FlightScheduleStatus current, FlightScheduleStatus requested) {
        if (current == requested) {
            return true;
        }
        return switch (current) {
            case SCHEDULED -> requested == FlightScheduleStatus.BOARDING
                    || requested == FlightScheduleStatus.CANCELLED;
            case BOARDING -> requested == FlightScheduleStatus.DEPARTED
                    || requested == FlightScheduleStatus.CANCELLED;
            case DEPARTED -> requested == FlightScheduleStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private FlightScheduleResponse toResponse(FlightSchedules schedule) {
        return new FlightScheduleResponse(UUID.fromString(schedule.getId()),
                UUID.fromString(schedule.getFlights().getId()), schedule.getFlights().getFlight_number(),
                UUID.fromString(schedule.getAircrafts().getId()), schedule.getAircrafts().getRegistrationNo(),
                schedule.getFlights().getDeparture_airport_id().getCode(),
                schedule.getFlights().getArrival_airport_id().getCode(),
                schedule.getDeparture_time(), schedule.getArrival_time(), schedule.getBase_price(),
                schedule.getStatus());
    }

    private FlightScheduleSeatResponse toSeatResponse(FlightSeats seat) {
        return new FlightScheduleSeatResponse(UUID.fromString(seat.getId()), seat.getSeats().getSeatNumber(),
                seat.getSeats().getSeatClass(), seat.getPrice(), seat.getStatus());
    }
}
