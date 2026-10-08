package com.airline.airline_booking_system.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airline.airline_booking_system.role.entity.UserRoles;
import com.airline.airline_booking_system.role.entity.UserRolesId;

public interface UserRoleRepository extends JpaRepository<UserRoles, UserRolesId> {
}
