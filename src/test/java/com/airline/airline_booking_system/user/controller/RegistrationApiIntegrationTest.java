package com.airline.airline_booking_system.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.airline.airline_booking_system.role.entity.UserRolesId;
import com.airline.airline_booking_system.role.repository.RoleRepository;
import com.airline.airline_booking_system.role.repository.UserRoleRepository;
import com.airline.airline_booking_system.user.entity.Users;
import com.airline.airline_booking_system.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationApiIntegrationTest {
    private static final String PASSWORD = "test-password-123";
    private static final String JWT_SECRET = "local-test-secret-key-must-be-at-least-32-bytes-long";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerCreatesUserWithEncodedPasswordAndCustomerRoleAndRejectsDuplicate() throws Exception {
        String email = "sb47-" + UUID.randomUUID() + "@example.com";
        String body = objectMapper.writeValueAsString(new RegistrationPayload(email, PASSWORD, "Test Customer"));

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            String userId = response.path("id").asText();
            assertNotNull(userId);
            assertEquals(email, response.path("email").asText());
            assertEquals("Test Customer", response.path("fullName").asText());

            Users user = userRepository.findById(userId).orElseThrow();
            assertEquals(email, user.getEmail());
            assertNotEquals(PASSWORD, user.getPassword());
            assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));

            var customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();
            assertTrue(userRoleRepository.existsById(new UserRolesId(userId, customerRole.getId())));

            String loginBody = objectMapper.writeValueAsString(new LoginPayload(email, PASSWORD));
            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode loginResponse = objectMapper.readTree(loginResult.getResponse().getContentAsString());
            assertEquals("Bearer", loginResponse.path("tokenType").asText());
            assertEquals(3600, loginResponse.path("expiresIn").asLong());
            assertEquals(email, loginResponse.path("email").asText());

            SignedJWT token = SignedJWT.parse(loginResponse.path("accessToken").asText());
            assertEquals(JWSAlgorithm.HS256, token.getHeader().getAlgorithm());
            assertEquals(JOSEObjectType.JWT, token.getHeader().getType());
            assertTrue(token.verify(new MACVerifier(JWT_SECRET.getBytes(StandardCharsets.UTF_8))));
            assertEquals(userId, token.getJWTClaimsSet().getSubject());
            assertEquals(email, token.getJWTClaimsSet().getStringClaim("email"));
            assertTrue(token.getJWTClaimsSet().getExpirationTime().getTime() > System.currentTimeMillis());
            assertEquals(List.of("CUSTOMER"), token.getJWTClaimsSet().getStringListClaim("roles"));

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new LoginPayload(email, "wrong-password"))))
                    .andExpect(status().isUnauthorized());

            user.setStatus(Users.UserStatus.BLOCKED);
            userRepository.save(user);
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isConflict());
        } finally {
            userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
                roleRepository.findByName("CUSTOMER").ifPresent(role -> userRoleRepository
                        .deleteById(new UserRolesId(user.getId(), role.getId())));
                userRepository.delete(user);
            });
        }
    }

    private record RegistrationPayload(String email, String password, String fullName) {
    }

    private record LoginPayload(String email, String password) {
    }
}
