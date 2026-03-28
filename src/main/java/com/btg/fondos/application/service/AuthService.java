package com.btg.fondos.application.service;

import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.port.in.AuthUseCase;
import com.btg.fondos.domain.port.out.ClientRepository;
import com.btg.fondos.infrastructure.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthService implements AuthUseCase {

    private final ClientRepository clientRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(ClientRepository clientRepository,
                       JwtProvider jwtProvider,
                       PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String authenticate(String email, String password) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, client.getPassword())) {
            throw new ResourceNotFoundException("Credenciales inválidas");
        }

        return jwtProvider.generateToken(client.getId(), client.getUser(), client.getEmail(), client.getRole());
    }
}
