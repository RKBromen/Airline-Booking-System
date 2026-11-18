package com.airline.airline_booking_system.payment.entity;

import java.time.LocalDateTime;
import java.math.*;

import com.airline.airline_booking_system.booking.entity.Bookings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(name = "payments")
public class Payments {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Bookings bookings;

    public enum PaymentMethod {
        VNPAY, MOMO, CREDIT_CARD, BANK_TRANSFER
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod payment_method;
    @Column(nullable = false)
    private BigDecimal amount;

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    @Column(name = "transaction_id", unique = true)
    private String transactionId;
    @Column(nullable = false)
    private LocalDateTime created_at;
    private LocalDateTime paid_at;

    @Column(name = "payment_url", length = 2048)
    private String payment_url;

    public Payments(String id, Bookings bookings, PaymentMethod payment_method, BigDecimal amount, PaymentStatus status,
            String transaction_id, LocalDateTime created_at, LocalDateTime paid_at, String payment_url) {
        this.id = id;
        this.bookings = bookings;
        this.payment_method = payment_method;
        this.amount = amount;
        this.status = status;
        this.transactionId = transaction_id;
        this.created_at = created_at;
        this.paid_at = paid_at;
        this.payment_url = payment_url;
    }
}
