package com.medilens.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncodingTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Test
    void shouldHashPasswordWithBCrypt() {
        String rawPassword = "SecureMedicalPassword#2024";

        String encoded = passwordEncoder.encode(rawPassword);

        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$12$") || encoded.startsWith("$2b$12$"));
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
    }

    @Test
    void shouldRejectMismatchedPassword() {
        String rawPassword = "CorrectPassword123";
        String encoded = passwordEncoder.encode(rawPassword);

        assertFalse(passwordEncoder.matches("WrongPassword123", encoded));
    }
}
