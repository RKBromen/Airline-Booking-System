package com.airline.airline_booking_system.user.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public Users getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof String userId)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return userRepository.findById(userId)
                .filter(user -> user.getStatus() == Users.UserStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("User not found or inactive"));
    }

    public String getCurrentUserId() {
        String userId = getCurrentUser().getId();
        System.out.println("========== CURRENT USER ==========");
        System.out.println("Authenticated userId: " + userId);
        System.out.println("==================================");

        return userId;
    }
}
