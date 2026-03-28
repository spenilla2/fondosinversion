package com.btg.fondos.application.dto;

import com.btg.fondos.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String id,
        String fundId,
        String fundName,
        String type,
        BigDecimal amount,
        Instant timestamp
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFundId(),
                transaction.getFundName(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTimestamp()
        );
    }
}
