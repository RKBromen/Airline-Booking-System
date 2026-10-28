package com.airline.airline_booking_system.auth.dto;

import com.airline.airline_booking_system.user.dto.LoginResponse;

public record AuthSession(LoginResponse response, String refreshToken) {
}
