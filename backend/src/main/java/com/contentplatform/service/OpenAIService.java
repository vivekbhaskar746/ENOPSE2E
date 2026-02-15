package com.contentplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpenAIService {
    
    @Value("${nebius.api.token}")
    private String apiToken;
    
    @Value("${nebius.api.url}")
    private String apiUrl;
    
    private final WebClient webClient = WebClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String generateBlogContent(String keywords, String tone, int wordCount) {
        try {
            String prompt = String.format(
                "Write a %s blog article about '%s' in a %s tone. " +
                "The article should be approximately %d words. " +
                "Include SEO-optimized content with proper headings and structure. " +
                "Make it engaging and informative.",
                wordCount > 800 ? "comprehensive" : "concise",
                keywords, tone.toLowerCase(), wordCount
            );
            
            String requestBody = String.format(
                "{\"model\": \"meta-llama/Llama-3.3-70B-Instruct\", " +
                "\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], " +
                "\"temperature\": 0.7, \"max_tokens\": 2048}",
                prompt.replace("\"", "\\\"")
            );
            
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
            return "Error generating content: " + e.getMessage();
        }
    }
    
    public String generateTitle(String keywords, String tone) {
        try {
            String prompt = String.format(
                "Generate an SEO-optimized, engaging title for a blog article about '%s' in a %s tone. " +
                "The title should be catchy and under 60 characters.",
                keywords, tone.toLowerCase()
            );
            
            String requestBody = String.format(
                "{\"model\": \"meta-llama/Meta-Llama-3.1-8B-Instruct-fast\", " +
                "\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], " +
                "\"temperature\": 0.8, \"max_tokens\": 100}",
                prompt.replace("\"", "\\\"")
            );
            
            String response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("choices").get(0).path("message").path("content").asText().trim();
            
        } catch (Exception e) {
            return "Generated Title for: " + keywords;
        }
    }
}