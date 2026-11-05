package com.airline.airline_booking_system.user.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.auth.enums.AuthProvider;
import com.airline.airline_booking_system.auth.repository.RefreshTokenRepository;
import com.airline.airline_booking_system.auth.repository.UserAuthProviderRepository;
import com.airline.airline_booking_system.user.dto.ChangePasswordRequest;
import com.airline.airline_booking_system.user.dto.PageResponse;
import com.airline.airline_booking_system.user.dto.UpdateProfileRequest;
import com.airline.airline_booking_system.user.dto.UserCreateRequest;
import com.airline.airline_booking_system.user.dto.UserResponse;
import com.airline.airline_booking_system.user.dto.UserStatusRequest;
import com.airline.airline_booking_system.user.dto.UserUpdateRequest;
import com.airline.airline_booking_system.user.entity.Roles;
import com.airline.airline_booking_system.user.entity.UserRoles;
import com.airline.airline_booking_system.user.entity.UserRolesId;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.RoleRepository;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.repository.UserRoleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final UserAuthProviderRepository providers;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, RoleRepository roles, UserRoleRepository userRoles,
            UserAuthProviderRepository providers, RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.userRoles = userRoles;
        this.providers = providers;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Users.UserStatus status, int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page or size");
        String property = switch (sort == null ? "createdAt" : sort) {
            case "email" -> "email";
            case "fullName" -> "full_name";
            case "createdAt" -> "created_at";
            default -> throw new IllegalArgumentException("Unsupported sort field");
        };
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, property));
        Page<Users> result = status == null ? users.findAll(pageable) : users.findAllByStatus(status, pageable);
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(), page, size,
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public UserResponse get(String id) { return toResponse(findUser(id)); }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw new EmailAlreadyRegisteredException();
        List<Roles> assignedRoles;
        if (request.roleIds() == null || request.roleIds().isEmpty()) {
            assignedRoles = List.of(roles.findByName("CUSTOMER")
                    .orElseThrow(() -> new IllegalStateException("Required CUSTOMER role is missing")));
        } else {
            List<String> ids = request.roleIds().stream().distinct().toList();
            assignedRoles = new ArrayList<>(roles.findAllById(ids));
            if (assignedRoles.size() != ids.size()) throw new IllegalArgumentException("One or more roles do not exist");
        }
        LocalDateTime now = LocalDateTime.now();
        Users user = users.save(new Users(UUID.randomUUID().toString(), email,
                passwordEncoder.encode(request.password()), request.fullName().trim(), Users.UserStatus.ACTIVE, now, now));
        userRoles.saveAll(assignedRoles.stream().map(role -> new UserRoles(
                new UserRolesId(user.getId(), role.getId()), user, role)).toList());
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(String id, UserUpdateRequest request) {
        Users user = findUser(id);
        requireNotDeleted(user);
        user.setFull_name(request.fullName().trim());
        user.setUpdated_at(LocalDateTime.now());
        return toResponse(users.save(user));
    }

    @Transactional
    public UserResponse updateStatus(String id, UserStatusRequest request) {
        Users user = findUser(id);
        user.setStatus(request.status());
        user.setUpdated_at(LocalDateTime.now());
        if (request.status() != Users.UserStatus.ACTIVE) refreshTokens.revokeAllForUser(id);
        return toResponse(users.save(user));
    }

    @Transactional
    public void delete(String id) {
        Users user = findUser(id);
        user.setStatus(Users.UserStatus.DELETED);
        user.setUpdated_at(LocalDateTime.now());
        users.save(user);
        refreshTokens.revokeAllForUser(id);
    }

    @Transactional
    public UserResponse updateOwnProfile(String userId, UpdateProfileRequest request) {
        Users user = findUser(userId);
        requireActive(user);
        user.setFull_name(request.fullName().trim());
        user.setUpdated_at(LocalDateTime.now());
        return toResponse(users.save(user));
    }

    @Transactional
    public void changeOwnPassword(String userId, ChangePasswordRequest request) {
        Users user = findUser(userId);
        requireActive(user);
        if (user.getPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdated_at(LocalDateTime.now());
        users.save(user);
        refreshTokens.revokeAllForUser(userId);
    }

    private Users findUser(String id) {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void requireActive(Users user) {
        if (user.getStatus() != Users.UserStatus.ACTIVE) throw new InvalidCredentialsException();
    }

    private void requireNotDeleted(Users user) {
        if (user.getStatus() == Users.UserStatus.DELETED) throw new EntityNotFoundException("User not found");
    }

    private UserResponse toResponse(Users user) {
        List<String> userRoleNames = userRoles.findRoleNamesByUserId(user.getId());
        List<String> authProviders = providers.findAllByUser_Id(user.getId()).stream()
                .map(provider -> provider.getProvider().name()).toList();
        return new UserResponse(user.getId(), user.getEmail(), user.getFull_name(), user.getStatus().name(),
                userRoleNames, authProviders, user.getCreated_at());
    }
}
