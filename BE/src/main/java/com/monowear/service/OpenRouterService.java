package com.monowear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monowear.exception.BadRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ApplicationScoped
public class OpenRouterService {

    private static final Logger LOG = Logger.getLogger(OpenRouterService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @ConfigProperty(name = "openrouter.api-key")
    String apiKey;

    @ConfigProperty(name = "openrouter.model")
    String model;

    @ConfigProperty(name = "openrouter.api-url")
    String apiUrl;

    public String chat(String systemPrompt, String userMessage) {
        try {
            var body = MAPPER.createObjectNode();
            body.put("model", model.trim());

            var messages = MAPPER.createArrayNode();

            var systemMsg = MAPPER.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            var userMsg = MAPPER.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            body.set("messages", messages);

            String requestBody = MAPPER.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl.trim()))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "https://monowear.io")
                    .header("X-Title", "Mono Wear")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                LOG.errorf("OpenRouter API error [%d]: %s", response.statusCode(), response.body());
                throw new BadRequestException("Lỗi phản hồi từ AI");
            }

            JsonNode json = MAPPER.readTree(response.body());
            JsonNode choices = json.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText("").trim();
            }

            return "Trợ lý ảo hiện tại không thể phản hồi câu hỏi này.";
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("OpenRouter request failed", e);
            throw new BadRequestException("Không thể kết nối trợ lý AI: " + e.getMessage());
        }
    }
}
