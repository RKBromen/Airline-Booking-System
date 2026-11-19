package com.airline.airline_booking_system.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PaymentCreateRequest(
        @NotBlank(message = "Payment method is required") @Pattern(regexp = "VNPAY|MOMO|CREDIT_CARD|BANK_TRANSFER", message = "Invalid payment method") String paymentMethod) {

}
