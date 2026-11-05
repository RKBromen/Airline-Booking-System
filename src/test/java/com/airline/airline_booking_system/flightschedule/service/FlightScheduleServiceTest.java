package com.airline.airline_booking_system.flightschedule.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import com.airline.airline_booking_system.aircraft.enums.AircraftStatus;
import com.airline.airline_booking_system.aircraft.enums.SeatClass;
import com.airline.airline_booking_system.aircraft.repository.AircraftRepository;
import com.airline.airline_booking_system.aircraft.repository.AircraftSeatRepository;
import com.airline.airline_booking_system.booking.entity.Bookings.BookingStatus;
import com.airline.airline_booking_system.flight.entity.Flights;
import com.airline.airline_booking_system.flight.enums.FlightStatus;
import com.airline.airline_booking_system.flight.repository.FlightRepository;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleRequest;
import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
import com.airline.airline_booking_system.flightschedule.enums.FlightScheduleStatus;
import com.airline.airline_booking_system.flightschedule.repository.FlightScheduleRepository;
import com.airline.airline_booking_system.flightschedule.repository.FlightSeatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FlightScheduleServiceTest {
    private FlightScheduleRepository scheduleRepository;
    private FlightSeatRepository flightSeatRepository;
    private FlightRepository flightRepository;
    private AircraftRepository aircraftRepository;
    private AircraftSeatRepository aircraftSeatRepository;
    private FlightScheduleService service;
    private Flights flight;
    private Aircrafts aircraft;
    private AircraftSeats aircraftSeat;
    private FlightScheduleRequest request;
    private LocalDateTime departure;
    private LocalDateTime arrival;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(FlightScheduleRepository.class);
        flightSeatRepository = mock(FlightSeatRepository.class);
        flightRepository = mock(FlightRepository.class);
        aircraftRepository = mock(AircraftRepository.class);
        aircraftSeatRepository = mock(AircraftSeatRepository.class);
        service = new FlightScheduleService(scheduleRepository, flightSeatRepository, flightRepository,
                aircraftRepository, aircraftSeatRepository);

        UUID flightId = UUID.randomUUID();
        UUID aircraftId = UUID.randomUUID();
        UUID departureAirportId = UUID.randomUUID();
        UUID arrivalAirportId = UUID.randomUUID();
        flight = new Flights();
        flight.setId(flightId.toString());
        flight.setFlight_number("AB123");
        flight.setStatus(FlightStatus.ACTIVE);
        var departureAirport = new com.airline.airline_booking_system.airport.entity.Airports();
        departureAirport.setId(departureAirportId);
        departureAirport.setCode("SGN");
        var arrivalAirport = new com.airline.airline_booking_system.airport.entity.Airports();
        arrivalAirport.setId(arrivalAirportId);
        arrivalAirport.setCode("HAN");
        flight.setDeparture_airport_id(departureAirport);
        flight.setArrival_airport_id(arrivalAirport);

        aircraft = new Aircrafts();
        aircraft.setId(aircraftId.toString());
        aircraft.setRegistrationNo("VN-ABC");
        aircraft.setStatus(AircraftStatus.ACTIVE);
        aircraftSeat = new AircraftSeats(UUID.randomUUID().toString(), aircraft, "1A", SeatClass.ECONOMY);

        departure = LocalDateTime.of(2026, 12, 1, 8, 30);
        arrival = departure.plusHours(2);
        request = new FlightScheduleRequest(flightId, aircraftId, departure, arrival, new BigDecimal("125.00"));
        when(flightRepository.findById(flightId.toString())).thenReturn(Optional.of(flight));
        when(aircraftRepository.findById(aircraftId.toString())).thenReturn(Optional.of(aircraft));
        when(aircraftSeatRepository.findByAircraftsIdOrderBySeatNumberAsc(aircraftId.toString()))
                .thenReturn(List.of(aircraftSeat));
        when(scheduleRepository.saveAndFlush(any(FlightSchedules.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createScheduleCreatesAvailableInventoryForEveryAircraftSeat() {
        var response = service.createSchedule(request);

        assertEquals("AB123", response.flightNumber());
        assertEquals("VN-ABC", response.aircraftRegistration());
        assertEquals("SGN", response.departureAirportCode());
        assertEquals("HAN", response.arrivalAirportCode());
        assertEquals(FlightScheduleStatus.SCHEDULED, response.status());
        assertEquals(new BigDecimal("125.00"), response.basePrice());
        verify(flightSeatRepository).saveAll(anyList());
    }

    @Test
    void rejectsInvalidTimesBeforeAccessingPersistence() {
        var invalid = new FlightScheduleRequest(request.flightId(), request.aircraftId(), arrival, departure,
                request.basePrice());

        assertThrows(IllegalArgumentException.class, () -> service.createSchedule(invalid));
        verify(flightRepository, never()).findById(any());
    }

    @Test
    void rejectsInactiveFlightAndAircraftWithoutCreatingSchedule() {
        flight.setStatus(FlightStatus.INACTIVE);
        ResponseStatusException inactiveFlight = assertThrows(ResponseStatusException.class,
                () -> service.createSchedule(request));
        assertEquals(HttpStatus.CONFLICT, inactiveFlight.getStatusCode());

        flight.setStatus(FlightStatus.ACTIVE);
        aircraft.setStatus(AircraftStatus.BLOCKED);
        ResponseStatusException inactiveAircraft = assertThrows(ResponseStatusException.class,
                () -> service.createSchedule(request));
        assertEquals(HttpStatus.CONFLICT, inactiveAircraft.getStatusCode());
        verify(scheduleRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsAircraftTimeConflictAndMissingSeatLayout() {
        when(scheduleRepository.findOverlappingSchedule(aircraft.getId(), departure, arrival,
                FlightScheduleStatus.CANCELLED, null)).thenReturn(Optional.of("existing-schedule"));
        ResponseStatusException conflict = assertThrows(ResponseStatusException.class,
                () -> service.createSchedule(request));
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        verify(scheduleRepository, never()).saveAndFlush(any());

        when(scheduleRepository.findOverlappingSchedule(aircraft.getId(), departure, arrival,
                FlightScheduleStatus.CANCELLED, null)).thenReturn(Optional.empty());
        when(aircraftSeatRepository.findByAircraftsIdOrderBySeatNumberAsc(aircraft.getId())).thenReturn(List.of());
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> service.createSchedule(request)).getStatusCode());
    }

    @Test
    void rejectsCancellationWithActiveBookingsAndInvalidStateTransitions() {
        FlightSchedules schedule = schedule(FlightScheduleStatus.SCHEDULED);
        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(scheduleRepository.countActiveBookings(schedule.getId(), BookingStatus.CANCELLED,
                BookingStatus.EXPIRED)).thenReturn(1L);

        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.updateStatus(UUID.fromString(schedule.getId()), FlightScheduleStatus.CANCELLED))
                .getStatusCode());
        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(UUID.fromString(schedule.getId()), FlightScheduleStatus.COMPLETED));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void advancesScheduleThroughLifecycle() {
        FlightSchedules schedule = schedule(FlightScheduleStatus.SCHEDULED);
        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(FlightScheduleStatus.BOARDING,
                service.updateStatus(UUID.fromString(schedule.getId()), FlightScheduleStatus.BOARDING).status());
        assertEquals(FlightScheduleStatus.DEPARTED,
                service.updateStatus(UUID.fromString(schedule.getId()), FlightScheduleStatus.DEPARTED).status());
        assertEquals(FlightScheduleStatus.COMPLETED,
                service.updateStatus(UUID.fromString(schedule.getId()), FlightScheduleStatus.COMPLETED).status());
    }

    @Test
    void reportsMissingFlight() {
        when(flightRepository.findById(request.flightId().toString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.createSchedule(request));
        verify(scheduleRepository, never()).saveAndFlush(any());
    }

    private FlightSchedules schedule(FlightScheduleStatus status) {
        FlightSchedules schedule = new FlightSchedules(UUID.randomUUID().toString(), flight, aircraft, departure,
                arrival, request.basePrice(), status);
        return schedule;
    }
}
