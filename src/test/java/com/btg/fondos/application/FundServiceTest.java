package com.btg.fondos.application;

import com.btg.fondos.application.service.FundService;
import com.btg.fondos.domain.exception.AlreadySubscribedException;
import com.btg.fondos.domain.exception.InsufficientBalanceException;
import com.btg.fondos.domain.exception.NotSubscribedException;
import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Transaction;
import com.btg.fondos.domain.port.out.ClientRepository;
import com.btg.fondos.domain.port.out.FundRepository;
import com.btg.fondos.domain.port.out.NotificationService;
import com.btg.fondos.domain.port.out.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private FundRepository fundRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private NotificationService notificationService;

    private FundService fundService;

    @BeforeEach
    void setUp() {
        fundService = new FundService(clientRepository, fundRepository, transactionRepository, notificationService);
    }

    private Client createClient(BigDecimal balance, List<String> subscribedFunds, String notification) {
        return new Client("c-1", "client-001", "Test", "test@test.com", "+573001234567",
                balance, notification, subscribedFunds, "encoded", "ROLE_CLIENT");
    }

    private Client createClient(BigDecimal balance, List<String> subscribedFunds) {
        return createClient(balance, subscribedFunds, "EMAIL");
    }

    private Fund createFund(String id, String name, BigDecimal minAmount) {
        return new Fund(id, name, minAmount, "FPV");
    }

    private void mockSaveAndTransaction() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(clientRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void subscribe_success() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>());
        Fund fund = createFund("1", "FPV_BTG_PACTUAL_RECAUDADORA", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));
        mockSaveAndTransaction();

        Transaction result = fundService.subscribe("c-1", "1");

        assertEquals("SUBSCRIBE", result.getType());
        assertEquals(new BigDecimal("425000"), client.getBalance());
        assertTrue(client.getSubscribedFundIds().contains("1"));
        verify(notificationService).sendEmail(eq("test@test.com"), anyString(), anyString());
    }

    @Test
    void subscribe_withNullSubscribedFunds_success() {
        Client client = new Client("c-1", "client-001", "Test", "test@test.com", "+573001234567",
                new BigDecimal("500000"), "EMAIL", null, "encoded", "ROLE_CLIENT");
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));
        mockSaveAndTransaction();

        Transaction result = fundService.subscribe("c-1", "1");

        assertEquals("SUBSCRIBE", result.getType());
        assertTrue(client.getSubscribedFundIds().contains("1"));
    }

    @Test
    void subscribe_insufficientBalance() {
        Client client = createClient(new BigDecimal("50000"), new ArrayList<>());
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));

        var ex = assertThrows(InsufficientBalanceException.class, () -> fundService.subscribe("c-1", "1"));
        assertTrue(ex.getMessage().contains("FPV_BTG"));
    }

    @Test
    void subscribe_alreadySubscribed() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>(List.of("1")));
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));

        assertThrows(AlreadySubscribedException.class, () -> fundService.subscribe("c-1", "1"));
    }

    @Test
    void subscribe_clientNotFound() {
        when(clientRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> fundService.subscribe("invalid", "1"));
    }

    @Test
    void subscribe_fundNotFound() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>());
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fundService.subscribe("c-1", "99"));
    }

    @Test
    void subscribe_withSmsNotification() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>(), "SMS");
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));
        mockSaveAndTransaction();

        fundService.subscribe("c-1", "1");

        verify(notificationService).sendSms(eq("+573001234567"), anyString());
    }

    @Test
    void subscribe_withSnsNotification() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>(), "SNS");
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));
        mockSaveAndTransaction();

        fundService.subscribe("c-1", "1");

        verify(notificationService).sendSms(eq("+573001234567"), anyString());
    }

    @Test
    void subscribe_notificationFails_doesNotThrow() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>());
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));
        mockSaveAndTransaction();
        doThrow(new RuntimeException("SES error")).when(notificationService).sendEmail(anyString(), anyString(), anyString());

        Transaction result = fundService.subscribe("c-1", "1");

        assertNotNull(result);
        assertEquals("SUBSCRIBE", result.getType());
    }

    @Test
    void cancel_success() {
        Client client = createClient(new BigDecimal("425000"), new ArrayList<>(List.of("1")));
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));
        mockSaveAndTransaction();

        Transaction result = fundService.cancel("c-1", "1");

        assertEquals("CANCEL", result.getType());
        assertEquals(new BigDecimal("500000"), client.getBalance());
        assertFalse(client.getSubscribedFundIds().contains("1"));
    }

    @Test
    void cancel_notSubscribed() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>());
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));

        assertThrows(NotSubscribedException.class, () -> fundService.cancel("c-1", "1"));
    }

    @Test
    void cancel_nullSubscribedFunds() {
        Client client = new Client("c-1", "client-001", "Test", "test@test.com", "+573001234567",
                new BigDecimal("500000"), "EMAIL", null, "encoded", "ROLE_CLIENT");
        Fund fund = createFund("1", "FPV_BTG", new BigDecimal("75000"));
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("1")).thenReturn(Optional.of(fund));

        assertThrows(NotSubscribedException.class, () -> fundService.cancel("c-1", "1"));
    }

    @Test
    void cancel_clientNotFound() {
        when(clientRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> fundService.cancel("invalid", "1"));
    }

    @Test
    void cancel_fundNotFound() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>());
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(fundRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fundService.cancel("c-1", "99"));
    }

    @Test
    void getTransactionHistory_success() {
        Client client = createClient(new BigDecimal("500000"), new ArrayList<>());
        when(clientRepository.findById("c-1")).thenReturn(Optional.of(client));
        when(transactionRepository.findByClientId("c-1")).thenReturn(List.of());

        List<Transaction> result = fundService.getTransactionHistory("c-1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTransactionHistory_clientNotFound() {
        when(clientRepository.findById("invalid")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> fundService.getTransactionHistory("invalid"));
    }
}
