package com.airline.airline_booking_system.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airline.airline_booking_system.payment.dto.PaymentDetailDTO;
import com.airline.airline_booking_system.payment.repository.PaymentRepository;

@RestController
@RequestMapping("api/v1/payments")
public class PaymentController {
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/{id}")
    public PaymentDetailDTO findPaymentDetailDTO(@PathVariable("id") String paymentId) {
        return paymentRepository.findPaymentDetailDTO(paymentId);
    }
}
