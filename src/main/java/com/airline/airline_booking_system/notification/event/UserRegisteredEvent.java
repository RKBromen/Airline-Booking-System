package com.airline.airline_booking_system.notification.event;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRegisteredEvent(
        @NotBlank @Size(max = 36) String eventId,
        @Min(1) int eventVersion,
        @NotBlank @Size(max = 36) String userId,
        @NotBlank @Email String email,
        @NotBlank @Size(max = 150) String fullName,
        Instant registeredAt
) { }
