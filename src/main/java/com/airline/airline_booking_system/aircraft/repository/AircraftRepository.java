package com.airline.airline_booking_system.aircraft.repository;

import com.airline.airline_booking_system.aircraft.entity.Aircrafts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftRepository extends JpaRepository<Aircrafts, String> {
    boolean existsByRegistrationNoIgnoreCase(String registrationNo);
    boolean existsByRegistrationNoIgnoreCaseAndIdNot(String registrationNo, String id);
}
