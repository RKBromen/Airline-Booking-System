package com.airline.airline_booking_system.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.airline.airline_booking_system.auth.entity.UserAuthProvider;
import com.airline.airline_booking_system.auth.enums.AuthProvider;

public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, String> {
    Optional<UserAuthProvider> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    List<UserAuthProvider> findAllByUser_Id(String userId);
}
