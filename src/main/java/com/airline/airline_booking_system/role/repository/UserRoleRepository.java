package com.airline.airline_booking_system.role.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.airline.airline_booking_system.role.entity.UserRoles;
import com.airline.airline_booking_system.role.entity.UserRolesId;

public interface UserRoleRepository extends JpaRepository<UserRoles, UserRolesId> {
    @Query("select userRole.role.name from UserRoles userRole where userRole.user.id = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") String userId);
}
