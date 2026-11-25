package com.airline.airline_booking_system.notification.entity;

import java.time.Instant;
import java.util.UUID;

import com.airline.airline_booking_system.notification.enums.NotificationStatus;
import com.airline.airline_booking_system.notification.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_history", indexes = {
        @Index(name = "idx_notification_status_created", columnList = "status, created_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_event_type_recipient",
                columnNames = {"event_id", "notification_type", "recipient"})
})
public class NotificationHistory {
    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Column(name = "booking_id", length = 36)
    private String bookingId;

    @Column(length = 255, nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 32, nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private NotificationStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;

    @Column(name = "processing_until")
    private Instant processingUntil;

    @Column(name = "booking_code", length = 32)
    private String bookingCode;

    @Column(name = "recipient_name", length = 150, nullable = false)
    private String recipientName;

    @Column(name = "flight_number", length = 20)
    private String flightNumber;

    @Column(name = "departure_time")
    private Instant departureTime;

    @PrePersist
    void prepareForInsert() {
        if (id == null) id = UUID.randomUUID().toString();
        if (status == null) status = NotificationStatus.PENDING;
        if (createdAt == null) createdAt = Instant.now();
    }
}
