package com.nawaf.capstone3.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@Component
public class GeminiCaller {

    private static final Logger log = LoggerFactory.getLogger(GeminiCaller.class);
    private static final MimeType PDF = MimeTypeUtils.parseMimeType("application/pdf");
    private static final int MAX_ATTEMPTS = 3;
    private static final long PAUSE_BETWEEN_CALLS_MS = 4000;

    private final ChatClient chatClient;

    public GeminiCaller(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public <T> T extract(String system, String userText, byte[] pdf, Class<T> type) {
        long backoff = 6000;
        for (int attempt = 1; ; attempt++) {
            try {
                T result = chatClient.prompt()
                        .system(system)
                        .user(u -> u.text(userText).media(PDF, new ByteArrayResource(pdf)))
                        .call()
                        .entity(type);
                pause(PAUSE_BETWEEN_CALLS_MS);
                return result;
            } catch (RuntimeException e) {
                if (attempt >= MAX_ATTEMPTS) throw e;
                log.warn("Gemini call failed (attempt {}/{}): {}", attempt, MAX_ATTEMPTS, e.getMessage());
                pause(backoff);
                backoff *= 2;
            }
        }
    }

    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}