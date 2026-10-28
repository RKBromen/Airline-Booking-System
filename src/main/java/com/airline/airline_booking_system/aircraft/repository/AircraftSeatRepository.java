package com.airline.airline_booking_system.aircraft.repository;

import com.airline.airline_booking_system.aircraft.entity.AircraftSeats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AircraftSeatRepository extends JpaRepository<AircraftSeats, String> {
    List<AircraftSeats> findByAircraftsIdOrderBySeatNumberAsc(String aircraftId);
    boolean existsByAircraftsIdAndSeatNumberIgnoreCase(String aircraftId, String seatNumber);
}
