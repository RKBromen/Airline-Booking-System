package com.airline.airline_booking_system.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDetailDTO(String id, String bookingId, String bookingCode, BigDecimal amount,
                String paymentMethod, String status, String transtionId, LocalDateTime createdAt,
                LocalDateTime paidAt, LocalDateTime updatedAt) {

}
