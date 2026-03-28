package com.btg.fondos.application;

import com.btg.fondos.application.service.AuthService;
import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.port.out.ClientRepository;
import com.btg.fondos.infrastructure.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private JwtProvider jwtProvider;
    @Mock private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(clientRepository, jwtProvider, passwordEncoder);
    }

    private Client createClient() {
        return new Client("id-1", "client-001", "Test", "test@test.com", "+573001234567",
                new BigDecimal("500000"), "EMAIL", new ArrayList<>(), "encodedPass", "ROLE_CLIENT");
    }

    @Test
    void authenticate_success() {
        Client client = createClient();
        when(clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("btg2025", "encodedPass")).thenReturn(true);
        when(jwtProvider.generateToken("id-1", "client-001", "test@test.com", "ROLE_CLIENT")).thenReturn("jwt-token");

        String token = authService.authenticate("test@test.com", "btg2025");

        assertEquals("jwt-token", token);
    }

    @Test
    void authenticate_emailNotFound() {
        when(clientRepository.findByEmail("invalid@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.authenticate("invalid@test.com", "pass"));
    }

    @Test
    void authenticate_wrongPassword() {
        Client client = createClient();
        when(clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> authService.authenticate("test@test.com", "wrong"));
    }
}
