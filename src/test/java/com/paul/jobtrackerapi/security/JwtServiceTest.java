package com.paul.jobtrackerapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "this-is-a-test-secret-key-that-is-at-least-32-characters-long",
                86400000L
        );
    }

    @Test
    void generateToken_shouldStoreUsername() {

        String token = jwtService.generateToken("paul");

        String username = jwtService.extractUsername(token);

        assertEquals("paul", username);
    }

    @Test
    void isTokenValid_shouldReturnTrue_forMatchingUsername() {

        String token = jwtService.generateToken("paul");

        boolean valid = jwtService.isTokenValid(token, "paul");

        assertTrue(valid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_forDifferentUsername() {

        String token = jwtService.generateToken("paul");

        boolean valid = jwtService.isTokenValid(token, "alice");

        assertFalse(valid);
    }

    @Test
    void extractUsername_shouldThrowException_whenSigningKeyIsWrong() {

        JwtService otherJwtService = new JwtService(
                "this-is-a-completely-different-secret-key-123456",
                86400000L
        );

        String token = jwtService.generateToken("paul");

        assertThrows(
                Exception.class,
                () -> otherJwtService.extractUsername(token)
        );
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() throws InterruptedException {

        JwtService shortLivedJwtService = new JwtService(
                "this-is-a-test-secret-key-that-is-at-least-32-characters-long",
                1L
        );

        String token = shortLivedJwtService.generateToken("paul");

        Thread.sleep(10);

        boolean valid = shortLivedJwtService.isTokenValid(token, "paul");

        assertFalse(valid);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {

        boolean valid = jwtService.isTokenValid(
                "definitely-not-a-real-token",
                "paul"
        );

        assertFalse(valid);
    }
}
