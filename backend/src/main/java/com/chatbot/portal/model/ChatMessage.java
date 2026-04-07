package com.chatbot.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String userMessage;

    @Column(nullable = false, length = 2000)
    private String botResponse;

    private LocalDateTime timestamp = LocalDateTime.now();
    private String intent;

    public ChatMessage() {}

    public ChatMessage(String sessionId, String userMessage, String botResponse, String intent) {
        this.sessionId = sessionId;
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.intent = intent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    public String getBotResponse() { return botResponse; }
    public void setBotResponse(String botResponse) { this.botResponse = botResponse; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
}
