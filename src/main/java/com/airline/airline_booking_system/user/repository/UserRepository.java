package com.airline.airline_booking_system.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.airline.airline_booking_system.user.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
}
