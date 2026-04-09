package com.project01.skillineserver.utils;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mailjet.client.transactional.response.SendEmailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MailjetUtil {

    @Value("${mailjet.api-key}")
    @NonFinal
    private String apiKey;

    @Value("${mailjet.api-secretKey}")
    @NonFinal
    private String apiSecretKey;

    public void sendMailWithMailjet(String toEmail,
                                    String toName,
                                    String subject,
                                    String body,
                                    String customId) {
        ClientOptions options = ClientOptions.builder()
                .apiKey(apiKey)
                .apiSecretKey(apiSecretKey)
                .build();

        MailjetClient client = new MailjetClient(options);
        TransactionalEmail message = TransactionalEmail
                .builder()
                .from(new SendContact("tranquocdat30122004@gmail.com", "Tran Quoc Dat"))
                .to(new SendContact(toEmail, toName))
                .htmlPart(body)
                .subject(subject)
                .header("test-header-key", "test-value")
                .customID(customId)
                .build();

        SendEmailsRequest request = SendEmailsRequest
                .builder()
                .message(message) // you can add up to 50 messages per request
                .build();

        SendEmailsResponse response = null;
        try {
            response = request.sendWith(client);
        } catch (MailjetException e) {
            throw new RuntimeException(e);
        }
        log.info("Info : {}", response);
    }

}