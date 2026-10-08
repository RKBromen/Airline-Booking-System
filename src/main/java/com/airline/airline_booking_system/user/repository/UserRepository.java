package com.airline.airline_booking_system.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airline.airline_booking_system.user.entity.Users;

public interface UserRepository extends JpaRepository<Users, String> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<Users> findByEmailIgnoreCase(String email);
}
