package com.airline.airline_booking_system.payment.service;

import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.payment.dto.PaymentCreateRequest;
import com.airline.airline_booking_system.payment.dto.PaymentResponse;
import com.airline.airline_booking_system.payment.dto.PendingPaymentData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentInitializationService {
    private final PaymentConfirmationService paymentConfirmationService;
    private final PaymentUrlProvider paymentUrlProvider;

    public PaymentResponse initializePayment(String bookingId, String userId, PaymentCreateRequest request,
            String clientIp) {
        PendingPaymentData paymentData = paymentConfirmationService.createPendingPayments(bookingId, userId, request);

        String paymentUrl = paymentData.paymentUrl();

        if (paymentUrl == null || paymentUrl.isBlank()) {
            paymentUrl = paymentUrlProvider.createPaymentUrl(paymentData, clientIp);

            paymentConfirmationService.savePaymentUrl(paymentData.paymentId(), paymentUrl);
        }

        return new PaymentResponse(paymentData.paymentId(), paymentData.bookingId(), paymentData.amount(),
                paymentData.paymentMethod(), paymentData.status(), paymentUrl);
    }
}
