package com.nawaf.capstone3.Service;

import com.nawaf.capstone3.Api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.util.Map;

@Service
public class WhatsAppService {
    private final RestClient client;
    private final int channelId;

    public WhatsAppService(RestClient.Builder builder, @Value("${whatsloop.base-url}") String baseUrl,
            @Value("${whatsloop.token}") String token, @Value("${whatsloop.channel-id}") int channelId) {
        this.client = builder.clone().baseUrl(baseUrl).defaultHeader("Authorization", "Bearer " + token).build();
        this.channelId = channelId;
    }

    public void sendMessage(String phoneNumber, String message) {
        if (phoneNumber == null || !phoneNumber.matches("^(05[0-9]{8}|9665[0-9]{8})$")
                || message == null || message.isBlank()) throw new ApiException("Invalid notification recipient or message");
        String number = phoneNumber.startsWith("0") ? "966" + phoneNumber.substring(1) : phoneNumber;
        // retrieve() throws for HTTP errors. No process arguments, manual JSON escaping, or response logging.
        Map<?, ?> response = client.post().uri("/messages/send-text").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("channel_id", channelId, "to", number, "message", message))
                .retrieve().body(Map.class);
        // WhatsLoop v1 documents a boolean success envelope; fail closed on unknown/empty responses.
        if (response == null || !Boolean.TRUE.equals(response.get("success")))
            throw new ApiException("WhatsLoop rejected the message");
    }
}
