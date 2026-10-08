package com.airline.airline_booking_system.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airline.airline_booking_system.role.entity.Roles;

public interface RoleRepository extends JpaRepository<Roles, String> {
    Optional<Roles> findByName(String name);
}
