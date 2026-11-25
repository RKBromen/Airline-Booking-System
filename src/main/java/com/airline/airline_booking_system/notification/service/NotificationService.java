package com.airline.airline_booking_system.notification.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.notification.entity.NotificationHistory;
import com.airline.airline_booking_system.notification.enums.NotificationStatus;
import com.airline.airline_booking_system.notification.enums.NotificationType;
import com.airline.airline_booking_system.notification.event.BookingNotificationEvent;
import com.airline.airline_booking_system.notification.event.UserRegisteredEvent;
import com.airline.airline_booking_system.notification.repository.NotificationHistoryRepository;

@Service
public class NotificationService {
    private final NotificationHistoryRepository repository;

    public NotificationService(NotificationHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void accept(BookingNotificationEvent event, NotificationType type) {
        if (repository.existsByEventIdAndNotificationTypeAndRecipient(
                event.eventId(), type, event.recipientEmail())) {
            return;
        }

        NotificationHistory notification = new NotificationHistory();
        notification.setEventId(event.eventId());
        notification.setBookingId(event.bookingId());
        notification.setBookingCode(event.bookingCode());
        notification.setRecipient(event.recipientEmail());
        notification.setRecipientName(event.recipientName());
        notification.setFlightNumber(event.flightNumber());
        notification.setDepartureTime(event.departureTime());
        savePending(notification, type);
    }

    @Transactional
    public void accept(UserRegisteredEvent event) {
        if (repository.existsByEventIdAndNotificationTypeAndRecipient(
                event.eventId(), NotificationType.ACCOUNT_REGISTERED, event.email())) {
            return;
        }

        NotificationHistory notification = new NotificationHistory();
        notification.setEventId(event.eventId());
        notification.setRecipient(event.email());
        notification.setRecipientName(event.fullName());
        savePending(notification, NotificationType.ACCOUNT_REGISTERED);
    }

    private void savePending(NotificationHistory notification, NotificationType type) {
        notification.setNotificationType(type);
        notification.setStatus(NotificationStatus.PENDING);
        repository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationHistory> pendingBatch() {
        return repository.findTop20ByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING);
    }

    @Transactional
    public Optional<NotificationHistory> claim(String id) {
        int claimed = repository.claimPending(
                id,
                NotificationStatus.PENDING,
                NotificationStatus.PROCESSING,
                Instant.now().plus(Duration.ofMinutes(10)));
        if (claimed == 0) return Optional.empty();
        return repository.findById(id);
    }

    @Transactional
    public void markSent(String id) {
        repository.findById(id).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setProcessingUntil(null);
            notification.setLastError(null);
        });
    }

    @Transactional
    public void markFailed(String id, Exception failure) {
        repository.findById(id).ifPresent(notification -> {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setProcessingUntil(null);
            String message = failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
            notification.setLastError(message.substring(0, Math.min(message.length(), 2000)));
        });
    }
}
