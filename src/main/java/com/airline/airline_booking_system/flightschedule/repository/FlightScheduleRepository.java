package com.airline.airline_booking_system.flightschedule.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;
import com.airline.airline_booking_system.flightschedule.enums.FlightScheduleStatus;
import com.airline.airline_booking_system.booking.entity.Bookings.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightScheduleRepository
        extends JpaRepository<FlightSchedules, String>, JpaSpecificationExecutor<FlightSchedules> {
    @Query("""
            select s.id from FlightSchedules s
            where s.aircrafts.id = :aircraftId
              and s.status <> :cancelled
              and s.departure_time < :arrivalTime
              and s.arrival_time > :departureTime
              and (:excludedId is null or s.id <> :excludedId)
            """)
    Optional<String> findOverlappingSchedule(@Param("aircraftId") String aircraftId,
            @Param("departureTime") LocalDateTime departureTime,
            @Param("arrivalTime") LocalDateTime arrivalTime,
            @Param("cancelled") FlightScheduleStatus cancelled,
            @Param("excludedId") String excludedId);

    @Query("""
            select count(b) from Bookings b
            where b.flight_schedules.id = :scheduleId
              and b.status not in (:cancelled, :expired)
            """)
    long countActiveBookings(@Param("scheduleId") String scheduleId,
            @Param("cancelled") BookingStatus cancelled, @Param("expired") BookingStatus expired);
}
