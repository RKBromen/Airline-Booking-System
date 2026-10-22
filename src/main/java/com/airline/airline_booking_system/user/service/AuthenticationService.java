package com.airline.airline_booking_system.user.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.user.repository.UserRoleRepository;
import com.airline.airline_booking_system.auth.dto.AuthSession;
import com.airline.airline_booking_system.auth.service.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(UserRepository userRepository, UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Users user = userRepository.findByEmailIgnoreCase(email)
                .filter(candidate -> candidate.getStatus() == Users.UserStatus.ACTIVE)
                .filter(candidate -> candidate.getPassword() != null
                        && passwordEncoder.matches(request.password(), candidate.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return createSession(user);
    }

    @Transactional
    public AuthSession createSession(Users user) {
        List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
        String token = jwtTokenService.createToken(user, roles);
        String refreshToken = refreshTokenService.issue(user);
        return new AuthSession(new LoginResponse(token, "Bearer", jwtTokenService.getExpirationSeconds(), user.getId(),
                user.getEmail(), roles), refreshToken);
    }

    @Transactional
    public AuthSession refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) throw new InvalidCredentialsException();
        Users user = refreshTokenService.rotate(rawToken);
        return createSession(user);
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokenService.revoke(rawToken);
    }

    @Transactional
    public void revokeAll(String userId) {
        refreshTokenService.revokeAll(userId);
    }
}
