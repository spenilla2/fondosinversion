package com.btg.fondos.domain.model;

import java.math.BigDecimal;

public class Fund {

    private String id;
    private String name;
    private BigDecimal minimumAmount;
    private String category; // FPV, FIC

    public Fund() {}

    public Fund(String id, String name, BigDecimal minimumAmount, String category) {
        this.id = id;
        this.name = name;
        this.minimumAmount = minimumAmount;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMinimumAmount() { return minimumAmount; }
    public void setMinimumAmount(BigDecimal minimumAmount) { this.minimumAmount = minimumAmount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
