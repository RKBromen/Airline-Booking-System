package com.airline.airline_booking_system.user.dto;

import com.airline.airline_booking_system.user.entity.Users.UserStatus;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(@NotNull UserStatus status) { }
