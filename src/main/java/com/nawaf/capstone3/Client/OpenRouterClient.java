package com.nawaf.capstone3.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Component
public class OpenRouterClient {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterClient.class);
    private static final MimeType PDF = MimeTypeUtils.parseMimeType("application/pdf");
    private static final int MAX_ATTEMPTS = 3;

    private final ChatClient chatClient;

    public OpenRouterClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String sendPrompt(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public <T> T analyzePdf(String systemPrompt, String userPrompt, byte[] pdf, Class<T> responseType) {
        long backoff = 2000;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                log.info("OpenRouter PDF analysis started: output={}, attempt={}/{}", responseType.getSimpleName(), attempt, MAX_ATTEMPTS);

                T result = chatClient.prompt()
                        .system(systemPrompt)
                        .user(user -> user
                                .text(userPrompt)
                                .media(PDF, new ByteArrayResource(pdf)))
                        .call()
                        .entity(responseType);

                log.info("OpenRouter PDF analysis completed: output={}", responseType.getSimpleName());

                return result;

            } catch (RuntimeException exception) {
                log.warn("OpenRouter request failed: attempt={}/{}, error={}", attempt, MAX_ATTEMPTS, exception.getMessage());

                if (attempt == MAX_ATTEMPTS) throw exception;

                sleep(backoff);
                backoff *= 2;
            }
        }

        throw new IllegalStateException("OpenRouter attempts exhausted");
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenRouter retry interrupted", exception);
        }
    }
}