package com.airline.airline_booking_system.payment.service;

import com.airline.airline_booking_system.payment.dto.PendingPaymentData;

public interface PaymentUrlProvider {
    String createPaymentUrl(PendingPaymentData paymentData, String clientIp);
}
