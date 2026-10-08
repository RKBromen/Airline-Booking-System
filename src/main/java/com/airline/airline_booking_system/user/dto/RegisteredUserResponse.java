package com.airline.airline_booking_system.user.dto;

import java.time.LocalDateTime;

public record RegisteredUserResponse(String id, String email, String fullName, LocalDateTime createdAt) {
}
