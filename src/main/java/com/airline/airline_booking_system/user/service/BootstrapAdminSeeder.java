package com.airline.airline_booking_system.user.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.user.entity.Roles;
import com.airline.airline_booking_system.user.entity.UserRoles;
import com.airline.airline_booking_system.user.entity.UserRolesId;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.RoleRepository;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.repository.UserRoleRepository;

@Component
@Profile("dev")
public class BootstrapAdminSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;

    public BootstrapAdminSeeder(UserRepository userRepository, RoleRepository roleRepository,
            UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.full-name:Quản trị viên}") String fullName) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Both bootstrap admin email and password must be configured");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Roles adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Required ADMIN role is missing"));
        Users admin = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(() -> {
            LocalDateTime now = LocalDateTime.now();
            return userRepository.save(new Users(UUID.randomUUID().toString(), normalizedEmail,
                    passwordEncoder.encode(password), fullName.trim(), Users.UserStatus.ACTIVE, now, now));
        });

        UserRolesId roleId = new UserRolesId(admin.getId(), adminRole.getId());
        if (!userRoleRepository.existsById(roleId)) {
            userRoleRepository.save(new UserRoles(roleId, admin, adminRole));
        }
    }
}
