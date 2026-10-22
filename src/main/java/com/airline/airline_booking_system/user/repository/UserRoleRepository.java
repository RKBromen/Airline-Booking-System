package com.airline.airline_booking_system.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.airline.airline_booking_system.user.entity.UserRoles;
import com.airline.airline_booking_system.user.entity.UserRolesId;

public interface UserRoleRepository extends JpaRepository<UserRoles, UserRolesId> {
    @Query("select userRole.role.name from UserRoles userRole where userRole.user.id = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") String userId);

    @Query("select distinct userRole.user.id from UserRoles userRole where userRole.role.name = :roleName")
    List<String> findUserIdsByRoleName(@Param("roleName") String roleName);
}
