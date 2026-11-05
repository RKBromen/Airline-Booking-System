package com.airline.airline_booking_system.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.airline.airline_booking_system.auth.repository.RefreshTokenRepository;
import com.airline.airline_booking_system.auth.repository.UserAuthProviderRepository;
import com.airline.airline_booking_system.user.dto.ChangePasswordRequest;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.RoleRepository;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.repository.UserRoleRepository;

class UserServiceTest {
    private UserRepository users;
    private RefreshTokenRepository refreshTokens;
    private PasswordEncoder passwordEncoder;
    private UserService service;
    private Users user;

    @BeforeEach
    void setUp() {
        users = org.mockito.Mockito.mock(UserRepository.class);
        refreshTokens = org.mockito.Mockito.mock(RefreshTokenRepository.class);
        passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        service = new UserService(users, org.mockito.Mockito.mock(RoleRepository.class),
                org.mockito.Mockito.mock(UserRoleRepository.class),
                org.mockito.Mockito.mock(UserAuthProviderRepository.class), refreshTokens, passwordEncoder);
        user = new Users(UUID.randomUUID().toString(), "customer@example.com", "old-hash", "Customer",
                Users.UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void softDeleteMarksDeletedAndRevokesRefreshTokensWithoutPhysicalDelete() {
        service.delete(user.getId());

        assertEquals(Users.UserStatus.DELETED, user.getStatus());
        verify(users).save(user);
        verify(users, never()).deleteById(user.getId());
        verify(refreshTokens).revokeAllForUser(user.getId());
    }

    @Test
    void passwordChangeChecksCurrentPasswordAndRevokesOtherSessions() {
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        service.changeOwnPassword(user.getId(), new ChangePasswordRequest("old-password", "new-password"));

        assertEquals("new-hash", user.getPassword());
        verify(refreshTokens).revokeAllForUser(user.getId());
    }

    @Test
    void passwordChangeRejectsAnIncorrectCurrentPassword() {
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.changeOwnPassword(user.getId(),
                new ChangePasswordRequest("wrong-password", "new-password")));

        assertEquals("old-hash", user.getPassword());
        verify(refreshTokens, never()).revokeAllForUser(user.getId());
    }
}
