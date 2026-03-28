package com.btg.fondos.domain.port.in;

import com.btg.fondos.domain.model.Transaction;

import java.util.List;

public interface FundUseCase {

    Transaction subscribe(String clientId, String fundId);

    Transaction cancel(String clientId, String fundId);

    List<Transaction> getTransactionHistory(String clientId);
}
