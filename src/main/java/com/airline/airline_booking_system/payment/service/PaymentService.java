package com.airline.airline_booking_system.payment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.payment.dto.PaymentDetailDTO;
import com.airline.airline_booking_system.payment.dto.PaymentHistoryDTO;
import com.airline.airline_booking_system.payment.repository.PaymentRepository;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<PaymentHistoryDTO> findPaymentByBookingId(String bookingId) {
        return paymentRepository.findPaymentByBookingId(bookingId);
    }

    public PaymentDetailDTO findPaymentDetailDTO(String paymentId) {
        return paymentRepository.findPaymentDetailDTO(paymentId);
    }
}
