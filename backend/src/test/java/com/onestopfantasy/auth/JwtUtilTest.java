package com.onestopfantasy.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "test-secret-key-that-is-at-least-32-characters-long";
    private static final long EXPIRATION_MS = 3_600_000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION_MS);
    }

    @Test
    void generateToken_extractEmail_roundTrip() {
        String token = jwtUtil.generateToken("test@example.com");
        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void isValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("user@example.com");
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void isValid_garbageToken_returnsFalse() {
        assertFalse(jwtUtil.isValid("not.a.real.token"));
    }

    @Test
    void isValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L);
        String token = jwtUtil.generateToken("user@example.com");
        assertFalse(jwtUtil.isValid(token));
    }

    @Test
    void isValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("user@example.com");
        assertFalse(jwtUtil.isValid(token + "tampered"));
    }
}
