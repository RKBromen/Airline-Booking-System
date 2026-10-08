package com.airline.airline_booking_system.user.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.role.repository.UserRoleRepository;
import com.airline.airline_booking_system.user.dto.LoginRequest;
import com.airline.airline_booking_system.user.dto.LoginResponse;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthenticationService(UserRepository userRepository, UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Users user = userRepository.findByEmailIgnoreCase(email)
                .filter(candidate -> candidate.getStatus() == Users.UserStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);

        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        String token = jwtTokenService.createToken(user, roles);
        return new LoginResponse(token, "Bearer", jwtTokenService.getExpirationSeconds(), user.getId(),
                user.getEmail(), roles);
    }
}
