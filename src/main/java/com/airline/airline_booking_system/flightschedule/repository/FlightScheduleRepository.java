package com.airline.airline_booking_system.flightschedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.flightschedule.entity.FlightSchedules;

@Repository
public interface FlightScheduleRepository extends JpaRepository<FlightSchedules, String> {
}
