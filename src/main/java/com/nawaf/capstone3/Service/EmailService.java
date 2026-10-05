package com.nawaf.capstone3.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendMonthlyReport(
            String to,
            String userName,
            String vehicleName,
            String report
    ) {

        String html = loadTemplate(
                "templates/email/monthly-report.html"
        );

        html = html
                .replace("{{userName}}", userName)
                .replace("{{vehicleName}}", vehicleName)
                .replace("{{report}}", report);

        sendHtmlEmail(
                to,
                "Monthly Vehicle Report - " + vehicleName,
                html
        );
    }

    private void sendHtmlEmail(String to, String subject, String html) {

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String loadTemplate(String path) {

        try {
            ClassPathResource resource = new ClassPathResource(path);

            return new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }
    }
}