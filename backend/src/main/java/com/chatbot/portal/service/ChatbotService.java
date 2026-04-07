package com.chatbot.portal.service;

import com.chatbot.portal.model.ChatMessage;
import com.chatbot.portal.model.FAQ;
import com.chatbot.portal.repository.ChatMessageRepository;
import com.chatbot.portal.repository.FAQRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatbotService {

    private final FAQRepository faqRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NebiusService nebiusService;

    public ChatbotService(FAQRepository faqRepository, ChatMessageRepository chatMessageRepository, NebiusService nebiusService) {
        this.faqRepository = faqRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.nebiusService = nebiusService;
    }

    public String processMessage(String sessionId, String userMessage) {
        String response = generateResponse(userMessage);
        String intent = detectIntent(userMessage);
        chatMessageRepository.save(new ChatMessage(sessionId, userMessage, response, intent));
        return response;
    }

    private String generateResponse(String message) {
        String faqResponse = searchFAQs(message);
        if (faqResponse != null) return faqResponse;
        return nebiusService.generateCustomerSupportResponse(message,
                "You are a helpful customer support assistant. Answer any question: " + message);
    }

    private String searchFAQs(String message) {
        List<FAQ> faqs = faqRepository.findAll();
        String lower = message.toLowerCase();
        for (FAQ faq : faqs) {
            if (lower.contains(faq.getQuestion().toLowerCase()) || faq.getQuestion().toLowerCase().contains(lower))
                return faq.getAnswer();
        }
        return null;
    }

    private String detectIntent(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("create ticket") || lower.contains("report issue")) return "CREATE_TICKET";
        if (lower.contains("ticket status")) return "CHECK_STATUS";
        if (lower.contains("speak to agent")) return "ESCALATE";
        return "FAQ_QUERY";
    }

    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
}
