package com.chatbot.portal.controller;

import com.chatbot.portal.model.ChatMessage;
import com.chatbot.portal.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String message = request.get("message");
        if (sessionId == null || message == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(Map.of("response", chatbotService.processMessage(sessionId, message)));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatbotService.getChatHistory(sessionId));
    }
}
