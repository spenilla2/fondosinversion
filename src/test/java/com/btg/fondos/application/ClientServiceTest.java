package com.btg.fondos.application;

import com.btg.fondos.application.service.ClientService;
import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.port.out.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepository, passwordEncoder);
    }

    private Client createClient() {
        return new Client("id-1", "user-001", "Test", "test@test.com", "+573001234567",
                new BigDecimal("500000"), "EMAIL", new ArrayList<>(), "encoded", "ROLE_CLIENT");
    }

    @Test
    void create_success() {
        when(clientRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(clientRepository.findByUser("new-user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(clientRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Client result = clientService.create("new-user", "New User", "new@test.com", "+573009999999", "pass123", "EMAIL");

        assertNotNull(result);
        assertEquals("New User", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("new-user", result.getUser());
        assertEquals(new BigDecimal("500000"), result.getBalance());
        assertEquals("ROLE_CLIENT", result.getRole());
    }

    @Test
    void create_nullPreferredNotification_defaultsToEmail() {
        when(clientRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(clientRepository.findByUser("new-user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(clientRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Client result = clientService.create("new-user", "New User", "new@test.com", "+573009999999", "pass123", null);

        assertEquals("EMAIL", result.getPreferredNotification());
    }

    @Test
    void create_duplicateEmail_throws() {
        when(clientRepository.findByEmail("test@test.com")).thenReturn(Optional.of(createClient()));

        assertThrows(IllegalArgumentException.class,
                () -> clientService.create("new-user", "Test", "test@test.com", "+573001234567", "pass", "EMAIL"));
    }

    @Test
    void create_duplicateUser_throws() {
        when(clientRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(clientRepository.findByUser("user-001")).thenReturn(Optional.of(createClient()));

        assertThrows(IllegalArgumentException.class,
                () -> clientService.create("user-001", "Test", "new@test.com", "+573001234567", "pass", "EMAIL"));
    }

    @Test
    void update_success() {
        Client client = createClient();
        when(clientRepository.findById("id-1")).thenReturn(Optional.of(client));
        when(clientRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Client result = clientService.update("id-1", "Updated", "+573009999999", "SMS");

        assertEquals("Updated", result.getName());
        assertEquals("+573009999999", result.getPhone());
        assertEquals("SMS", result.getPreferredNotification());
    }

    @Test
    void update_partialFields() {
        Client client = createClient();
        when(clientRepository.findById("id-1")).thenReturn(Optional.of(client));
        when(clientRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Client result = clientService.update("id-1", null, null, null);

        assertEquals("Test", result.getName());
        assertEquals("+573001234567", result.getPhone());
        assertEquals("EMAIL", result.getPreferredNotification());
    }

    @Test
    void update_notFound() {
        when(clientRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.update("invalid", "Name", null, null));
    }

    @Test
    void delete_success() {
        when(clientRepository.findById("id-1")).thenReturn(Optional.of(createClient()));

        clientService.delete("id-1");

        verify(clientRepository).deleteById("id-1");
    }

    @Test
    void delete_notFound() {
        when(clientRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.delete("invalid"));
    }

    @Test
    void getById_success() {
        Client client = createClient();
        when(clientRepository.findById("id-1")).thenReturn(Optional.of(client));

        Client result = clientService.getById("id-1");

        assertEquals("id-1", result.getId());
    }

    @Test
    void getById_notFound() {
        when(clientRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.getById("invalid"));
    }

    @Test
    void getByUser_success() {
        Client client = createClient();
        when(clientRepository.findByUser("user-001")).thenReturn(Optional.of(client));

        Client result = clientService.getByUser("user-001");

        assertEquals("user-001", result.getUser());
    }

    @Test
    void getByUser_notFound() {
        when(clientRepository.findByUser("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.getByUser("invalid"));
    }

    @Test
    void getAll_success() {
        when(clientRepository.findAll()).thenReturn(List.of(createClient()));

        List<Client> result = clientService.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void getAll_empty() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<Client> result = clientService.getAll();

        assertTrue(result.isEmpty());
    }
}
