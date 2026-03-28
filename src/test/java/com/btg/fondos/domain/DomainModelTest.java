package com.btg.fondos.domain;

import com.btg.fondos.application.dto.ClientResponse;
import com.btg.fondos.application.dto.ErrorResponse;
import com.btg.fondos.application.dto.TransactionResponse;
import com.btg.fondos.domain.exception.AlreadySubscribedException;
import com.btg.fondos.domain.exception.InsufficientBalanceException;
import com.btg.fondos.domain.exception.NotSubscribedException;
import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    @Test
    void client_fullConstructorAndGetters() {
        Client client = new Client("1", "user-001", "Test", "test@test.com", "+57300",
                new BigDecimal("500000"), "EMAIL", new ArrayList<>(), "pass", "ROLE_CLIENT");

        assertEquals("1", client.getId());
        assertEquals("user-001", client.getUser());
        assertEquals("Test", client.getName());
        assertEquals("test@test.com", client.getEmail());
        assertEquals("+57300", client.getPhone());
        assertEquals(new BigDecimal("500000"), client.getBalance());
        assertEquals("EMAIL", client.getPreferredNotification());
        assertTrue(client.getSubscribedFundIds().isEmpty());
        assertEquals("pass", client.getPassword());
        assertEquals("ROLE_CLIENT", client.getRole());
    }

    @Test
    void client_setters() {
        Client client = new Client();
        client.setId("1");
        client.setUser("user-001");
        client.setName("Updated");
        client.setEmail("updated@test.com");
        client.setPhone("+573009999999");
        client.setBalance(new BigDecimal("100000"));
        client.setPreferredNotification("SMS");
        client.setSubscribedFundIds(List.of("1", "2"));
        client.setPassword("newpass");
        client.setRole("ROLE_ADMIN");

        assertEquals("1", client.getId());
        assertEquals("user-001", client.getUser());
        assertEquals("Updated", client.getName());
        assertEquals("updated@test.com", client.getEmail());
        assertEquals("+573009999999", client.getPhone());
        assertEquals(new BigDecimal("100000"), client.getBalance());
        assertEquals("SMS", client.getPreferredNotification());
        assertEquals(2, client.getSubscribedFundIds().size());
        assertEquals("newpass", client.getPassword());
        assertEquals("ROLE_ADMIN", client.getRole());
    }

    @Test
    void fund_fullConstructorAndGetters() {
        Fund fund = new Fund("1", "FPV_BTG", new BigDecimal("75000"), "FPV");

        assertEquals("1", fund.getId());
        assertEquals("FPV_BTG", fund.getName());
        assertEquals(new BigDecimal("75000"), fund.getMinimumAmount());
        assertEquals("FPV", fund.getCategory());
    }

    @Test
    void fund_setters() {
        Fund fund = new Fund();
        fund.setId("2");
        fund.setName("FIC_BTG");
        fund.setMinimumAmount(new BigDecimal("50000"));
        fund.setCategory("FIC");

        assertEquals("2", fund.getId());
        assertEquals("FIC_BTG", fund.getName());
        assertEquals(new BigDecimal("50000"), fund.getMinimumAmount());
        assertEquals("FIC", fund.getCategory());
    }

    @Test
    void transaction_fullConstructorAndGetters() {
        Instant now = Instant.now();
        Transaction tx = new Transaction("tx-1", "c-1", "f-1", "Fund", "SUBSCRIBE", new BigDecimal("75000"), now);

        assertEquals("tx-1", tx.getId());
        assertEquals("c-1", tx.getClientId());
        assertEquals("f-1", tx.getFundId());
        assertEquals("Fund", tx.getFundName());
        assertEquals("SUBSCRIBE", tx.getType());
        assertEquals(new BigDecimal("75000"), tx.getAmount());
        assertEquals(now, tx.getTimestamp());
    }

    @Test
    void transaction_setters() {
        Transaction tx = new Transaction();
        Instant now = Instant.now();
        tx.setId("tx-2");
        tx.setClientId("c-2");
        tx.setFundId("f-2");
        tx.setFundName("Fund2");
        tx.setType("CANCEL");
        tx.setAmount(new BigDecimal("50000"));
        tx.setTimestamp(now);

        assertEquals("tx-2", tx.getId());
        assertEquals("c-2", tx.getClientId());
        assertEquals("f-2", tx.getFundId());
        assertEquals("Fund2", tx.getFundName());
        assertEquals("CANCEL", tx.getType());
        assertEquals(new BigDecimal("50000"), tx.getAmount());
        assertEquals(now, tx.getTimestamp());
    }

    @Test
    void insufficientBalanceException_message() {
        var ex = new InsufficientBalanceException("FPV_BTG");
        assertEquals("No tiene saldo disponible para vincularse al fondo FPV_BTG", ex.getMessage());
    }

    @Test
    void alreadySubscribedException_message() {
        var ex = new AlreadySubscribedException("FPV_BTG");
        assertTrue(ex.getMessage().contains("Ya se encuentra suscrito"));
    }

    @Test
    void notSubscribedException_message() {
        var ex = new NotSubscribedException("FPV_BTG");
        assertTrue(ex.getMessage().contains("No se encuentra suscrito"));
    }

    @Test
    void resourceNotFoundException_message() {
        var ex = new ResourceNotFoundException("No encontrado");
        assertEquals("No encontrado", ex.getMessage());
    }

    @Test
    void transactionResponse_from() {
        Instant now = Instant.now();
        Transaction tx = new Transaction("tx-1", "c-1", "f-1", "FPV_BTG", "SUBSCRIBE", new BigDecimal("75000"), now);

        TransactionResponse response = TransactionResponse.from(tx);

        assertEquals("tx-1", response.id());
        assertEquals("f-1", response.fundId());
        assertEquals("FPV_BTG", response.fundName());
        assertEquals("SUBSCRIBE", response.type());
        assertEquals(new BigDecimal("75000"), response.amount());
        assertEquals(now, response.timestamp());
    }

    @Test
    void clientResponse_from() {
        Client client = new Client("1", "user-001", "Test", "test@test.com", "+57300",
                new BigDecimal("500000"), "EMAIL", List.of("1", "2"), "pass", "ROLE_CLIENT");

        ClientResponse response = ClientResponse.from(client);

        assertEquals("1", response.id());
        assertEquals("user-001", response.user());
        assertEquals("Test", response.name());
        assertEquals("test@test.com", response.email());
        assertEquals("+57300", response.phone());
        assertEquals(new BigDecimal("500000"), response.balance());
        assertEquals("EMAIL", response.preferredNotification());
        assertEquals(2, response.subscribedFundIds().size());
    }

    @Test
    void errorResponse_of() {
        ErrorResponse response = ErrorResponse.of(400, "Bad Request");

        assertEquals(400, response.status());
        assertEquals("Bad Request", response.message());
        assertNotNull(response.timestamp());
    }
}
