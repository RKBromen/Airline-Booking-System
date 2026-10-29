package com.airline.airline_booking_system.flightschedule.repository;

import java.util.List;

import com.airline.airline_booking_system.flightschedule.entity.FlightSeats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightSeatRepository extends JpaRepository<FlightSeats, String> {
    @Query("select s from FlightSeats s where s.flight_schedules.id = :scheduleId")
    List<FlightSeats> findAllByScheduleId(@Param("scheduleId") String scheduleId);
}
