package com.medilens.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "test_super_secret_jwt_signing_key_at_least_256_bits_length_for_hmac_sha256";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000L, 86400000L);
    }

    @Test
    void shouldGenerateAndValidateAccessToken() {
        UUID userId = UUID.randomUUID();
        String email = "test.patient@example.com";
        String role = "ROLE_PATIENT";

        String token = jwtService.generateAccessToken(userId, email, role);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void shouldGenerateAndValidateRefreshToken() {
        UUID userId = UUID.randomUUID();
        String email = "test.patient@example.com";

        String refreshToken = jwtService.generateRefreshToken(userId, email);

        assertNotNull(refreshToken);
        assertTrue(jwtService.validateToken(refreshToken));
        assertEquals(userId, jwtService.extractUserId(refreshToken));
        assertEquals(email, jwtService.extractEmail(refreshToken));
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidpayload.invalidsignature";
        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    void shouldDetectExpiredToken() {
        // JwtService configured with 0ms expiration
        JwtService expiredJwtService = new JwtService(SECRET, -1000L, -1000L);
        UUID userId = UUID.randomUUID();
        String token = expiredJwtService.generateAccessToken(userId, "expired@example.com", "ROLE_PATIENT");

        assertFalse(expiredJwtService.validateToken(token));
    }
}
