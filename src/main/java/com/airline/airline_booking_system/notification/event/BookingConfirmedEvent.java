package com.airline.airline_booking_system.notification.event;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingConfirmedEvent(
        @NotBlank @Size(max = 36) String eventId,
        @Min(1) int eventVersion,
        @NotBlank @Size(max = 36) String bookingId,
        @NotBlank @Size(max = 32) String bookingCode,
        @NotBlank @Email String recipientEmail,
        @NotBlank @Size(max = 150) String recipientName,
        @NotBlank @Size(max = 20) String flightNumber,
        Instant departureTime
) implements BookingNotificationEvent { }
