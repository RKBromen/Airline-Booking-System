package com.airline.airline_booking_system.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthExchangeRequest(@NotBlank String code) {
}
