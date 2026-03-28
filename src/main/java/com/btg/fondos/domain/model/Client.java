package com.btg.fondos.domain.model;

import java.math.BigDecimal;
import java.util.List;

public class Client {

    private String id;
    private String user;
    private String name;
    private String email;
    private String phone;
    private BigDecimal balance;
    private String preferredNotification;
    private List<String> subscribedFundIds;
    private String password;
    private String role;

    public Client() {}

    public Client(String id, String user, String name, String email, String phone,
                  BigDecimal balance, String preferredNotification,
                  List<String> subscribedFundIds, String password, String role) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.balance = balance;
        this.preferredNotification = preferredNotification;
        this.subscribedFundIds = subscribedFundIds;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getPreferredNotification() { return preferredNotification; }
    public void setPreferredNotification(String preferredNotification) { this.preferredNotification = preferredNotification; }
    public List<String> getSubscribedFundIds() { return subscribedFundIds; }
    public void setSubscribedFundIds(List<String> subscribedFundIds) { this.subscribedFundIds = subscribedFundIds; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
