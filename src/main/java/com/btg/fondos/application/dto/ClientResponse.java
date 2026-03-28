package com.btg.fondos.application.dto;

import com.btg.fondos.domain.model.Client;

import java.math.BigDecimal;
import java.util.List;

public record ClientResponse(
        String id,
        String user,
        String name,
        String email,
        String phone,
        BigDecimal balance,
        String preferredNotification,
        List<String> subscribedFundIds
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getUser(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getBalance(),
                client.getPreferredNotification(),
                client.getSubscribedFundIds()
        );
    }
}
