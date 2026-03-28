package com.btg.fondos.application.service;

import com.btg.fondos.domain.exception.AlreadySubscribedException;
import com.btg.fondos.domain.exception.InsufficientBalanceException;
import com.btg.fondos.domain.exception.NotSubscribedException;
import com.btg.fondos.domain.exception.ResourceNotFoundException;
import com.btg.fondos.domain.model.Client;
import com.btg.fondos.domain.model.Fund;
import com.btg.fondos.domain.model.Transaction;
import com.btg.fondos.domain.port.in.FundUseCase;
import com.btg.fondos.domain.port.out.ClientRepository;
import com.btg.fondos.domain.port.out.FundRepository;
import com.btg.fondos.domain.port.out.NotificationService;
import com.btg.fondos.domain.port.out.TransactionRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FundService implements FundUseCase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FundService.class);

    private final ClientRepository clientRepository;
    private final FundRepository fundRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    public FundService(ClientRepository clientRepository,
                       FundRepository fundRepository,
                       TransactionRepository transactionRepository,
                       NotificationService notificationService) {
        this.clientRepository = clientRepository;
        this.fundRepository = fundRepository;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Transaction subscribe(String clientId, String fundId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clientId));

        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fondo no encontrado: " + fundId));

        if (client.getSubscribedFundIds() != null && client.getSubscribedFundIds().contains(fundId)) {
            throw new AlreadySubscribedException(fund.getName());
        }

        if (client.getBalance().compareTo(fund.getMinimumAmount()) < 0) {
            throw new InsufficientBalanceException(fund.getName());
        }

        client.setBalance(client.getBalance().subtract(fund.getMinimumAmount()));
        log.info("Suscripción - Nuevo saldo: {} para cliente: {}", client.getBalance(), clientId);

        List<String> subscribedFunds = client.getSubscribedFundIds() != null
                ? new ArrayList<>(client.getSubscribedFundIds())
                : new ArrayList<>();
        subscribedFunds.add(fundId);
        client.setSubscribedFundIds(subscribedFunds);

        Client saved = clientRepository.save(client);
        log.info("Cliente guardado - Balance en DB: {}, Fondos: {}", saved.getBalance(), saved.getSubscribedFundIds());

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                clientId,
                fundId,
                fund.getName(),
                "SUBSCRIBE",
                fund.getMinimumAmount(),
                Instant.now()
        );
        transactionRepository.save(transaction);

        sendNotification(client, fund, "SUBSCRIBE");

        return transaction;
    }

    @Override
    public Transaction cancel(String clientId, String fundId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clientId));

        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fondo no encontrado: " + fundId));

        if (client.getSubscribedFundIds() == null || !client.getSubscribedFundIds().contains(fundId)) {
            throw new NotSubscribedException(fund.getName());
        }

        client.setBalance(client.getBalance().add(fund.getMinimumAmount()));
        log.info("Cancelación - Nuevo saldo: {} para cliente: {}", client.getBalance(), clientId);

        List<String> subscribedFunds = new ArrayList<>(client.getSubscribedFundIds());
        subscribedFunds.remove(fundId);
        client.setSubscribedFundIds(subscribedFunds);

        Client saved = clientRepository.save(client);
        log.info("Cliente guardado - Balance en DB: {}, Fondos: {}", saved.getBalance(), saved.getSubscribedFundIds());

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                clientId,
                fundId,
                fund.getName(),
                "CANCEL",
                fund.getMinimumAmount(),
                Instant.now()
        );
        transactionRepository.save(transaction);

        return transaction;
    }

    @Override
    public List<Transaction> getTransactionHistory(String clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clientId));
        return transactionRepository.findByClientId(clientId);
    }

    private void sendNotification(Client client, Fund fund, String type) {
        String message = String.format("Se ha realizado la %s al fondo %s por un monto de COP $%s",
                type.equals("SUBSCRIBE") ? "suscripción" : "cancelación",
                fund.getName(),
                fund.getMinimumAmount().toPlainString());

        try {
            String pref = client.getPreferredNotification().toUpperCase();
            if ("SMS".equals(pref) || "SNS".equals(pref)) {
                notificationService.sendSms(client.getPhone(), message);
            } else {
                notificationService.sendEmail(client.getEmail(), "BTG Pactual - Notificación de Fondo", message);
            }
        } catch (Exception e) {
            // Log pero no falla la transacción
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}
