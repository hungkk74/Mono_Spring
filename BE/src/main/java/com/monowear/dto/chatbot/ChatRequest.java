package com.monowear.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ChatRequest(
        @JsonProperty("message") String message,
        @JsonProperty("history") List<ChatMessage> history,
        @JsonProperty("userContext") UserContext userContext
) {
    public record ChatMessage(
            @JsonProperty("role") String role,
            @JsonProperty("text") String text
    ) {}

    public record UserContext(
            @JsonProperty("fullName") String fullName,
            @JsonProperty("email") String email,
            @JsonProperty("cartCount") Integer cartCount
    ) {}
}
