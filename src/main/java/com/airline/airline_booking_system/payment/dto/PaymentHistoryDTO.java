package com.airline.airline_booking_system.payment.dto;

import java.math.*;
import java.time.LocalDateTime;

public record PaymentHistoryDTO(String id, String paymentMethod, BigDecimal amount, String status,
        LocalDateTime createdAt, LocalDateTime paidAt) {

}
