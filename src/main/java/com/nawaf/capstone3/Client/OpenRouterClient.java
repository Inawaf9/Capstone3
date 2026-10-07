package com.nawaf.capstone3.Client;


import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class OpenRouterClient {
    private static final Logger log = LoggerFactory.getLogger(OpenRouterClient.class);
    private static final int MAX_ATTEMPTS = 3;

    private final ChatClient chatClient;

    public OpenRouterClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String sendPrompt(String prompt) {
        return executeWithRetry(
                () -> chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content(),
                "String",
                "TEXT"
        );
    }

    public <T> T analyzeText(String systemPrompt, String userPrompt, Class<T> responseType) {
        return executeWithRetry(
                () -> chatClient.prompt()
                        .system(systemPrompt)
                        .user(userPrompt)
                        .call()
                        .entity(responseType),
                responseType.getSimpleName(),
                "STRUCTURED_TEXT"
        );
    }

    private <T> T executeWithRetry(AiCall<T> call, String outputType, String mode) {
        long backoff = 2000;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            long started = System.currentTimeMillis();

            try {
                log.info(
                        "OpenRouter {} request started: output={}, attempt={}/{}",
                        mode,
                        outputType,
                        attempt,
                        MAX_ATTEMPTS
                );

                T result = call.execute();

                log.info(
                        "OpenRouter {} request completed: output={}, duration={}ms",
                        mode,
                        outputType,
                        System.currentTimeMillis() - started
                );

                return result;

            } catch (RuntimeException exception) {
                log.warn(
                        "OpenRouter {} request failed: output={}, attempt={}/{}, error={}",
                        mode,
                        outputType,
                        attempt,
                        MAX_ATTEMPTS,
                        exception.getMessage()
                );

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

    @FunctionalInterface
    private interface AiCall<T> {
        T execute();
    }

    public <T> T analyzeImage(MultipartFile image, String prompt, Class<T> responseType) {

        return executeWithRetry(
                () -> {
                    try {
                        Media media = new Media(
                                MimeTypeUtils.parseMimeType(image.getContentType()),
                                image.getResource()
                        );

                        return chatClient.prompt()
                                .user(user -> user
                                        .text(prompt)
                                        .media(media)
                                )
                                .call()
                                .entity(responseType);

                    }  catch (Exception e) {
            throw new RuntimeException(
                    "Failed to analyze image: " + e.getMessage(), e
            );
        }
                },
                responseType.getSimpleName(),
                "IMAGE"
        );
    }
}