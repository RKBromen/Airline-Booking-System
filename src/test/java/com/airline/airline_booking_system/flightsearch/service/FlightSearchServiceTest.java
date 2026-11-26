package com.airline.airline_booking_system.flightsearch.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.airline.airline_booking_system.flight.repository.FlightRepository;
import com.airline.airline_booking_system.flightschedule.dto.FlightScheduleDetail;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlightSearchServiceTest {
    private FlightRepository repository;
    private FlightSearchService service;

    @BeforeEach
    void setUp() {
        repository = mock(FlightRepository.class);
        service = new FlightSearchService(repository);
    }

    @Test
    void missingScheduleIsNotFoundForDetailsAndSeats() {
        when(repository.getScheduleById("missing")).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> service.findScheduleById("missing"));
        assertThrows(EntityNotFoundException.class, () -> service.seatChart("missing"));
        verify(repository, never()).seatingChart("missing");
    }

    @Test
    void emptyProjectionIsNotTreatedAsAValidSchedule() {
        when(repository.getScheduleById("missing"))
                .thenReturn(new FlightScheduleDetail(null, null, null, null, null, null, null, null, null, null));

        assertThrows(EntityNotFoundException.class, () -> service.findScheduleById("missing"));
        assertThrows(EntityNotFoundException.class, () -> service.seatChart("missing"));
        verify(repository, never()).seatingChart("missing");
    }
}
