package com.kickoff.user_service.service;

import com.kickoff.user_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-minimum-32-characters-long";
    private static final Long EXPIRATION = 86400000L; // 24 hours

    private JwtService jwtService;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void generatedTokenShouldContainUserId() {
        User user = userWith(UUID.randomUUID(), "testuser");

        String token = jwtService.generateToken(user);

        Claims claims = parseClaims(token);
        assertEquals(user.getId().toString(), claims.getSubject());
    }

    @Test
    void generatedTokenShouldContainUsername() {
        User user = userWith(UUID.randomUUID(), "testuser");

        String token = jwtService.generateToken(user);

        Claims claims = parseClaims(token);
        assertEquals("testuser", claims.get("username", String.class));
    }

    @Test
    void generatedTokenShouldNotBeExpired() {
        User user = userWith(UUID.randomUUID(), "testuser");

        String token = jwtService.generateToken(user);

        Claims claims = parseClaims(token);
        assertTrue(claims.getExpiration().after(new java.util.Date()));
    }

    @Test
    void tokenWithNegativeExpirationShouldFailParsing() {
        JwtService shortLivedService = new JwtService(SECRET, -1000L);
        User user = userWith(UUID.randomUUID(), "testuser");

        String token = shortLivedService.generateToken(user);

        assertThrows(Exception.class, () -> parseClaims(token));
    }

    @Test
    void generatedTokenForDifferentUsersShouldProduceDifferentTokens() {
        User user1 = userWith(UUID.randomUUID(), "user1");
        User user2 = userWith(UUID.randomUUID(), "user2");

        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        assertNotEquals(token1, token2);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private User userWith(UUID id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}