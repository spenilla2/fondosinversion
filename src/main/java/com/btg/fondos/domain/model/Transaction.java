package com.btg.fondos.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {

    private String id;
    private String clientId;
    private String fundId;
    private String fundName;
    private String type; // SUBSCRIBE, CANCEL
    private BigDecimal amount;
    private Instant timestamp;

    public Transaction() {}

    public Transaction(String id, String clientId, String fundId, String fundName,
                       String type, BigDecimal amount, Instant timestamp) {
        this.id = id;
        this.clientId = clientId;
        this.fundId = fundId;
        this.fundName = fundName;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getFundId() { return fundId; }
    public void setFundId(String fundId) { this.fundId = fundId; }
    public String getFundName() { return fundName; }
    public void setFundName(String fundName) { this.fundName = fundName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
