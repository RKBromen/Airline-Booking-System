package com.airline.airline_booking_system.auth.dto;

import java.util.List;

public record CurrentUserResponse(String id, String email, String fullName, String status, List<String> roles) {
}
