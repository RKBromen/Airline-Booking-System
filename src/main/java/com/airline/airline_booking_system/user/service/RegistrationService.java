package com.airline.airline_booking_system.user.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.user.entity.Roles;
import com.airline.airline_booking_system.user.entity.UserRoles;
import com.airline.airline_booking_system.user.entity.UserRolesId;
import com.airline.airline_booking_system.user.repository.RoleRepository;
import com.airline.airline_booking_system.user.repository.UserRoleRepository;
import com.airline.airline_booking_system.user.dto.RegisterRequest;
import com.airline.airline_booking_system.user.dto.RegisteredUserResponse;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.notification.event.UserRegisteredDomainEvent;

@Service
public class RegistrationService {
    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrationService(UserRepository userRepository, RoleRepository roleRepository,
            UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisteredUserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Roles customerRole = roleRepository.findByName(CUSTOMER_ROLE)
                .orElseThrow(() -> new IllegalStateException("Required CUSTOMER role is missing"));
        LocalDateTime now = LocalDateTime.now();
        Users user = new Users(UUID.randomUUID().toString(), email, passwordEncoder.encode(request.password()),
                request.fullName().trim(), Users.UserStatus.ACTIVE, now, now);
        userRepository.save(user);
        userRoleRepository.save(new UserRoles(new UserRolesId(user.getId(), customerRole.getId()), user, customerRole));

        eventPublisher.publishEvent(new UserRegisteredDomainEvent(
                UUID.randomUUID().toString(), user.getId(), user.getEmail(), user.getFullName(),
                java.time.Instant.now()));

        return new RegisteredUserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getStatus().name(),
                java.util.List.of(CUSTOMER_ROLE), user.getCreatedAt());
    }
}
