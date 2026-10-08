package com.nawaf.capstone3.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class WhatsAppService {

    @Value("${whatsloop.token}")
    private String token;

    @Value("${whatsloop.channel-id}")
    private int channelId;

    public void sendMessage(String phoneNumber, String message) {

        try {

            // Convert Saudi local number to international format
            String whatsappNumber = phoneNumber;

            if (whatsappNumber.startsWith("0")) {
                whatsappNumber = "966" + whatsappNumber.substring(1);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "curl",
                    "-i",
                    "-s",
                    "-X", "POST",
                    "https://api.whatsloop.net/v1/messages/send-text",
                    "-H", "Authorization: Bearer " + token,
                    "-H", "Content-Type: application/json",
                    "-d",
                    """
                    {
                        "channel_id": %d,
                        "to": "%s",
                        "message": "%s"
                    }
                    """.formatted(
                            channelId,
                            whatsappNumber,
                            escapeJson(message)
                    )
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder response = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    ))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            int exitCode = process.waitFor();

            System.out.println("WhatsLoop response: " + response);
            System.out.println("curl exit code: " + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException(
                        "WhatsLoop request failed: " + response
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to send WhatsApp message: " + e.getMessage(),
                    e
            );
        }
    }

    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}