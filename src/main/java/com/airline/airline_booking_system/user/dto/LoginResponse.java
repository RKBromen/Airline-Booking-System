package com.airline.airline_booking_system.user.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String userId,
        String email,
        List<String> roles) {
}
