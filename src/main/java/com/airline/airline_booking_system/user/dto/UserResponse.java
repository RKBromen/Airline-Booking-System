package com.airline.airline_booking_system.user.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(String id, String email, String fullName, String status,
        List<String> roles, List<String> authProviders, LocalDateTime createdAt) { }
