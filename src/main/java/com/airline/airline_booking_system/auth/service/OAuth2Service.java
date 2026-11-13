package com.airline.airline_booking_system.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.airline.airline_booking_system.auth.dto.AuthSession;
import com.airline.airline_booking_system.auth.entity.UserAuthProvider;
import com.airline.airline_booking_system.auth.enums.AuthProvider;
import com.airline.airline_booking_system.auth.repository.UserAuthProviderRepository;
import com.airline.airline_booking_system.user.entity.Roles;
import com.airline.airline_booking_system.user.entity.UserRoles;
import com.airline.airline_booking_system.user.entity.UserRolesId;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.RoleRepository;
import com.airline.airline_booking_system.user.repository.UserRepository;
import com.airline.airline_booking_system.user.repository.UserRoleRepository;
import com.airline.airline_booking_system.user.service.AuthenticationService;

@Service
public class OAuth2Service {
    public static final String LINK_FLOW_SESSION_KEY = OAuth2Service.class.getName() + ".LINK_FLOW";
    private static final long LINK_FLOW_TTL_SECONDS = 300;
    private static final long HANDOFF_TTL_SECONDS = 90;
    private final UserAuthProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthenticationService authenticationService;
    private final String frontendRedirectUri;
    private final Map<String, Handoff> handoffs = new ConcurrentHashMap<>();

    public OAuth2Service(UserAuthProviderRepository providerRepository, UserRepository userRepository,
            RoleRepository roleRepository, UserRoleRepository userRoleRepository,
            AuthenticationService authenticationService,
            @Value("${app.oauth2.frontend-redirect-uri:http://localhost:3000/auth/callback}") String frontendRedirectUri) {
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.authenticationService = authenticationService;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Transactional
    public AuthSession loginOrRegister(AuthProvider provider, OAuth2User principal) {
        OAuthIdentity identity = identity(provider, principal);
        Users user = providerRepository.findByProviderAndProviderUserId(provider, identity.providerId())
                .map(UserAuthProvider::getUser)
                .orElseGet(() -> createOAuthUser(provider, identity.providerId(), identity.email(), identity.fullName()));
        if (user.getStatus() != Users.UserStatus.ACTIVE) {
            throw new OAuthAccountConflictException("This account is not active");
        }
        return authenticationService.createSession(user);
    }

    @Transactional
    public void link(String userId, AuthProvider provider, OAuth2User principal) {
        OAuthIdentity identity = identity(provider, principal);
        Users user = userRepository.findById(userId)
                .filter(value -> value.getStatus() == Users.UserStatus.ACTIVE)
                .orElseThrow(() -> new OAuthAccountConflictException("The account to link is unavailable"));
        providerRepository.findByProviderAndProviderUserId(provider, identity.providerId()).ifPresent(existing -> {
            if (!existing.getUser().getId().equals(userId)) {
                throw new OAuthAccountConflictException("This provider account is already linked to another user");
            }
        });
        providerRepository.findByUser_IdAndProvider(userId, provider).ifPresent(existing -> {
            if (!existing.getProviderUserId().equals(identity.providerId())) {
                throw new OAuthAccountConflictException("Another account from this provider is already linked");
            }
        });
        if (providerRepository.findByProviderAndProviderUserId(provider, identity.providerId()).isEmpty()) {
            UserAuthProvider authProvider = new UserAuthProvider();
            authProvider.setId(UUID.randomUUID().toString());
            authProvider.setUser(user);
            authProvider.setProvider(provider);
            authProvider.setProviderUserId(identity.providerId());
            authProvider.setCreatedAt(LocalDateTime.now());
            providerRepository.save(authProvider);
        }
    }

    @Transactional
    public void unlink(String userId, AuthProvider provider) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new OAuthAccountConflictException("Account was not found"));
        List<UserAuthProvider> linked = providerRepository.findAllByUser_Id(userId);
        boolean hasPassword = user.getPassword() != null;
        boolean providerIsLinked = linked.stream().anyMatch(item -> item.getProvider() == provider);
        if (providerIsLinked && !hasPassword && linked.size() <= 1) {
            throw new OAuthAccountConflictException("Set a password or link another provider before removing this sign-in method");
        }
        providerRepository.deleteByUser_IdAndProvider(userId, provider);
    }

    private OAuthIdentity identity(AuthProvider provider, OAuth2User principal) {
        String providerId = firstString(principal.getAttributes(), "sub", "id");
        String email = firstString(principal.getAttributes(), "email");
        boolean emailVerified = provider == AuthProvider.FACEBOOK
                ? email != null && !email.isBlank()
                : Boolean.TRUE.equals(principal.getAttribute("email_verified"));
        String fullName = firstString(principal.getAttributes(), "name");
        if (providerId == null || providerId.isBlank() || email == null || email.isBlank() || !emailVerified) {
            throw new OAuthAccountConflictException("Provider must return a verified email to continue");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.length() > 255 || !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new OAuthAccountConflictException("Provider must return a valid email to continue");
        }
        return new OAuthIdentity(providerId, normalizedEmail, fullName);
    }

    public boolean isValidLinkFlow(LinkFlow flow, AuthProvider provider) {
        return flow != null && flow.provider() == provider && flow.createdAt() != null
                && Instant.now().isBefore(flow.createdAt().plusSeconds(LINK_FLOW_TTL_SECONDS));
    }

    @Transactional
    protected Users createOAuthUser(AuthProvider provider, String providerId, String email, String name) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new OAuthAccountConflictException("An account already uses this email; sign in with that account first");
        }
        Roles customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("Required CUSTOMER role is missing"));
        LocalDateTime now = LocalDateTime.now();
        String fullName = name == null || name.isBlank() ? email.substring(0, email.indexOf('@')) : name.trim();
        if (fullName.length() > 150) fullName = fullName.substring(0, 150);
        Users user = userRepository.save(new Users(UUID.randomUUID().toString(), email, null,
                fullName,
                Users.UserStatus.ACTIVE, now, now));
        userRoleRepository.save(new UserRoles(new UserRolesId(user.getId(), customerRole.getId()), user, customerRole));
        UserAuthProvider authProvider = new UserAuthProvider();
        authProvider.setId(UUID.randomUUID().toString());
        authProvider.setUser(user);
        authProvider.setProvider(provider);
        authProvider.setProviderUserId(providerId);
        authProvider.setCreatedAt(now);
        providerRepository.save(authProvider);
        return user;
    }

    public String issueHandoff(AuthSession session) {
        cleanExpiredHandoffs();
        String code = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        handoffs.put(code, new Handoff(session, LocalDateTime.now().plusSeconds(HANDOFF_TTL_SECONDS)));
        return code;
    }

    public AuthSession exchange(String code) {
        Handoff handoff = handoffs.remove(code);
        if (handoff == null) {
            throw new OAuthAccountConflictException("OAuth exchange code is invalid or expired");
        }
        if (!handoff.expiresAt().isAfter(LocalDateTime.now())) {
            authenticationService.logout(handoff.session().refreshToken());
            throw new OAuthAccountConflictException("OAuth exchange code is invalid or expired");
        }
        return handoff.session();
    }

    public String getFrontendRedirectUri() { return frontendRedirectUri; }

    private void cleanExpiredHandoffs() {
        LocalDateTime now = LocalDateTime.now();
        handoffs.forEach((code, handoff) -> {
            if (!handoff.expiresAt().isAfter(now) && handoffs.remove(code, handoff)) {
                authenticationService.logout(handoff.session().refreshToken());
            }
        });
    }

    private static String firstString(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value instanceof String text && !text.isBlank()) return text;
        }
        return null;
    }

    private record Handoff(AuthSession session, LocalDateTime expiresAt) { }
    private record OAuthIdentity(String providerId, String email, String fullName) { }
    public record LinkFlow(String userId, AuthProvider provider, Instant createdAt) implements java.io.Serializable { }
}
