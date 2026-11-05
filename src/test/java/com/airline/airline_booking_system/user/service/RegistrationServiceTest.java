package com.airline.airline_booking_system.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.airline.airline_booking_system.user.entity.Roles;
import com.airline.airline_booking_system.user.entity.UserRoles;
import com.airline.airline_booking_system.user.repository.RoleRepository;
import com.airline.airline_booking_system.user.repository.UserRoleRepository;
import com.airline.airline_booking_system.user.dto.RegisterRequest;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void registerNormalizesEmailHashesPasswordAndAssignsCustomerRole() {
        Roles customerRole = new Roles("customer-role-id", "CUSTOMER");
        when(userRepository.existsByEmailIgnoreCase("lam@example.com")).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        var response = registrationService.register(
                new RegisterRequest("  Lam@Example.com ", "password123", "  Lam Nguyen  "));

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(userCaptor.capture());
        Users savedUser = userCaptor.getValue();
        assertEquals("lam@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertNotEquals("password123", savedUser.getPassword());
        assertEquals("Lam Nguyen", savedUser.getFull_name());
        assertEquals(Users.UserStatus.ACTIVE, savedUser.getStatus());
        assertEquals(savedUser.getId(), response.id());

        ArgumentCaptor<UserRoles> userRoleCaptor = ArgumentCaptor.forClass(UserRoles.class);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        assertEquals(savedUser.getId(), userRoleCaptor.getValue().getUser().getId());
        assertEquals(customerRole.getId(), userRoleCaptor.getValue().getRole().getId());
    }

    @Test
    void registerRejectsAnExistingEmail() {
        when(userRepository.existsByEmailIgnoreCase("lam@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class, () -> registrationService.register(
                new RegisterRequest("Lam@example.com", "password123", "Lam Nguyen")));

        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void registerFailsWhenCustomerRoleIsMissing() {
        when(userRepository.existsByEmailIgnoreCase("lam@example.com")).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> registrationService.register(
                new RegisterRequest("lam@example.com", "password123", "Lam Nguyen")));

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }
}
