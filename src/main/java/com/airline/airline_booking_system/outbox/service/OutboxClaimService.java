
// package com.airline.airline_booking_system.outbox.service;

// import java.time.LocalDateTime;
// import java.util.List;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.airline.airline_booking_system.outbox.entity.OutboxEvent;
// import com.airline.airline_booking_system.outbox.enums.OutboxStatus;
// import com.airline.airline_booking_system.outbox.repository.OutboxEventRepository;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class OutboxClaimService {

//     private static final int BATCH_SIZE = 50;

//     private static final long LOCK_TIMEOUT_SECONDS = 60;

//     private final OutboxEventRepository repository;

//     @Transactional
//     public List<OutboxEvent> claimEvents() {

//         LocalDateTime now = LocalDateTime.now();

//         List<OutboxEvent> events = repository.findEventsToClaim(BATCH_SIZE);

//         for (OutboxEvent event : events) {

//             event.setStatus(OutboxStatus.PROCESSING);

//             event.setLockedUntil(
//                     now.plusSeconds(LOCK_TIMEOUT_SECONDS));
//         }

//         repository.saveAll(events);

//         return events;
//     }
// }

package com.airline.airline_booking_system.outbox.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.outbox.entity.OutboxEvent;
import com.airline.airline_booking_system.outbox.enums.OutboxStatus;
import com.airline.airline_booking_system.outbox.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxClaimService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public List<OutboxEvent> claimEvents(int batchSize, int lockDurationSeconds) {

        LocalDateTime now = LocalDateTime.now();

        // 1. Lấy các event có thể xử lý và khóa record
        List<OutboxEvent> events = outboxEventRepository.findEventsToClaim(batchSize);

        // 2. Claim từng event
        for (OutboxEvent event : events) {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setLockedUntil(now.plusSeconds(lockDurationSeconds));
        }

        // 3. Lưu trạng thái PROCESSING
        return outboxEventRepository.saveAll(events);
    }
}