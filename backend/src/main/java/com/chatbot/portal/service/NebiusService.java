package com.chatbot.portal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NebiusService {

    @Value("${nebius.api.token}")
    private String apiToken;

    @Value("${nebius.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateCustomerSupportResponse(String userMessage, String context) {
        try {
            String prompt = String.format(
                "You are a helpful customer support assistant. Context: %s\nCustomer Query: %s\nResponse:",
                context != null ? context : "General customer support", userMessage);

            String requestBody = String.format(
                "{\"model\": \"meta-llama/Llama-3.3-70B-Instruct\", " +
                "\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], " +
                "\"temperature\": 0.7, \"max_tokens\": 1024}",
                prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "I apologize, but I'm experiencing technical difficulties. Please try again or contact our support team directly.";
        }
    }
}
