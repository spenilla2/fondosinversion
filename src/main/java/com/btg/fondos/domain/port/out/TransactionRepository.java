package com.btg.fondos.domain.port.out;

import com.btg.fondos.domain.model.Transaction;

import java.util.List;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByClientId(String clientId);
}
