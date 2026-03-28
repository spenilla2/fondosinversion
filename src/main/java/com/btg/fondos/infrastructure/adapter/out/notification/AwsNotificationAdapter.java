package com.btg.fondos.infrastructure.adapter.out.notification;

import com.btg.fondos.domain.port.out.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
public class AwsNotificationAdapter implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(AwsNotificationAdapter.class);

    private final SesClient sesClient;
    private final SnsClient snsClient;
    private final String senderEmail;

    public AwsNotificationAdapter(SesClient sesClient,
                                  SnsClient snsClient,
                                  @Value("${app.notification.sender-email}") String senderEmail) {
        this.sesClient = sesClient;
        this.snsClient = snsClient;
        this.senderEmail = senderEmail;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("Enviando EMAIL a: {}, asunto: {}", to, subject);
        try {
            sesClient.sendEmail(SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).charset("UTF-8").build())
                            .body(Body.builder()
                                    .text(Content.builder().data(body).charset("UTF-8").build())
                                    .build())
                            .build())
                    .build());
            log.info("EMAIL enviado exitosamente a: {}", to);
        } catch (Exception e) {
            log.error("Error enviando EMAIL a {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("Enviando SMS a: {}, mensaje: {}", phoneNumber, message);
        try {
            PublishResponse response = snsClient.publish(PublishRequest.builder()
                    .phoneNumber(phoneNumber)
                    .message(message)
                    .build());
            log.info("SMS enviado exitosamente a: {}, messageId: {}", phoneNumber, response.messageId());
        } catch (Exception e) {
            log.error("Error enviando SMS a {}: {}", phoneNumber, e.getMessage(), e);
            throw e;
        }
    }
}
