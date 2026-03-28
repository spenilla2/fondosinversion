package com.btg.fondos.domain.port.out;

public interface NotificationService {

    void sendEmail(String to, String subject, String body);

    void sendSms(String phoneNumber, String message);
}
