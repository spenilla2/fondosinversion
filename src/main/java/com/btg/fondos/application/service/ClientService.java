package com.btg.fondos.application.service;

import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.port.in.ClientUseCase;
import com.btg.fondos.domain.port.out.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientService implements ClientUseCase {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Client create(String user, String name, String email, String phone, String password, String preferredNotification) {
        clientRepository.findByEmail(email).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe un cliente con el email " + email);
        });
        clientRepository.findByUser(user).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe un cliente con el usuario " + user);
        });

        Client client = new Client(
                UUID.randomUUID().toString(),
                user, name, email, phone,
                new BigDecimal("500000"),
                preferredNotification != null ? preferredNotification : "EMAIL",
                new ArrayList<>(),
                passwordEncoder.encode(password),
                "ROLE_CLIENT"
        );
        return clientRepository.save(client);
    }

    @Override
    public Client update(String id, String name, String phone, String preferredNotification) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));

        if (name != null) client.setName(name);
        if (phone != null) client.setPhone(phone);
        if (preferredNotification != null) client.setPreferredNotification(preferredNotification);

        return clientRepository.save(client);
    }

    @Override
    public void delete(String id) {
        clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
        clientRepository.deleteById(id);
    }

    @Override
    public Client getById(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    @Override
    public Client getByUser(String user) {
        return clientRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con usuario: " + user));
    }

    @Override
    public List<Client> getAll() {
        return clientRepository.findAll();
    }
}
