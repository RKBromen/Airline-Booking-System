package com.airline.airline_booking_system.payment.dto;

import java.math.BigDecimal;

public record PendingPaymentData(String paymentId, String bookingId, BigDecimal amount, String paymentMethod,
        String status, String transactionId, String paymentUrl) {

}
