package com.btg.fondos.infrastructure;

import com.btg.fondos.infrastructure.security.JwtProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider("BTGPactualSuperSecretKeyForJWT2025MustBe256BitsLong!!", 86400000);
    }

    @Test
    void generateToken_andParseToken() {
        String token = jwtProvider.generateToken("id-1", "client-001", "test@test.com", "ROLE_CLIENT");

        assertNotNull(token);
        Claims claims = jwtProvider.parseToken(token);
        assertEquals("client-001", claims.getSubject());
        assertEquals("id-1", claims.get("clientId", String.class));
        assertEquals("test@test.com", claims.get("email", String.class));
        assertEquals("ROLE_CLIENT", claims.get("role", String.class));
    }

    @Test
    void isValid_validToken() {
        String token = jwtProvider.generateToken("id-1", "client-001", "test@test.com", "ROLE_CLIENT");
        assertTrue(jwtProvider.isValid(token));
    }

    @Test
    void isValid_invalidToken() {
        assertFalse(jwtProvider.isValid("invalid.token.here"));
    }

    @Test
    void isValid_nullToken() {
        assertFalse(jwtProvider.isValid(null));
    }

    @Test
    void getClientId() {
        String token = jwtProvider.generateToken("id-1", "client-001", "test@test.com", "ROLE_CLIENT");
        assertEquals("id-1", jwtProvider.getClientId(token));
    }

    @Test
    void getUser() {
        String token = jwtProvider.generateToken("id-1", "client-001", "test@test.com", "ROLE_CLIENT");
        assertEquals("client-001", jwtProvider.getUser(token));
    }

    @Test
    void getRole() {
        String token = jwtProvider.generateToken("id-1", "client-001", "test@test.com", "ROLE_CLIENT");
        assertEquals("ROLE_CLIENT", jwtProvider.getRole(token));
    }

    @Test
    void expiredToken_isInvalid() {
        JwtProvider shortLived = new JwtProvider("BTGPactualSuperSecretKeyForJWT2025MustBe256BitsLong!!", -1000);
        String token = shortLived.generateToken("id-1", "client-001", "test@test.com", "ROLE_CLIENT");
        assertFalse(jwtProvider.isValid(token));
    }
}
