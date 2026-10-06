package com.nawaf.capstone3.Client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class OpenRouterClient {

    private final ChatClient chatClient;

    public OpenRouterClient(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String sendPrompt(String prompt) {
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}