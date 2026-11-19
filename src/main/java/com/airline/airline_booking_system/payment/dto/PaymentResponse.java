package com.airline.airline_booking_system.payment.dto;

import java.math.*;

public record PaymentResponse(String paymentId, String bookingId, BigDecimal amount, String paymentMethod,
        String status, String paymentUrl) {

}
