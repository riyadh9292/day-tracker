package com.example.tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${resend.from-address}")
    private String fromAddress;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/api/auth/verify?token=" + token;

        Map<String, Object> body = Map.of(
                "to", toEmail,
                "from", fromAddress,
                "subject", "Verification Email",
                "html", "<p>Welcome! Click the link below to verify your email and start tracking:</p>"
                        + "<p><a href=\"" + link + "\">" + link + "</a></p>"
                        + "<p>If you didn't request this, you can ignore this email.</p>"
        );
        restClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + resendApiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
