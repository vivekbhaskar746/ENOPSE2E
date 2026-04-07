package com.chatbot.portal.controller;

import com.chatbot.portal.model.FAQ;
import com.chatbot.portal.service.FAQService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@CrossOrigin(origins = "*")
public class FAQController {

    private final FAQService faqService;

    public FAQController(FAQService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public ResponseEntity<List<FAQ>> getAllFAQs() {
        return ResponseEntity.ok(faqService.getAllFAQs());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FAQ>> getFAQsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(faqService.getFAQsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FAQ>> searchFAQs(@RequestParam String keyword) {
        return ResponseEntity.ok(faqService.searchFAQs(keyword));
    }

    @PostMapping
    public ResponseEntity<FAQ> createFAQ(@RequestBody FAQ faq) {
        return ResponseEntity.ok(faqService.createFAQ(faq));
    }
}
