package com.chatbot.portal.service;

import com.chatbot.portal.model.FAQ;
import com.chatbot.portal.repository.FAQRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FAQService {

    private final FAQRepository faqRepository;

    public FAQService(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<FAQ> getAllFAQs() { return faqRepository.findAll(); }
    public List<FAQ> getFAQsByCategory(String category) { return faqRepository.findByCategory(category); }
    public List<FAQ> searchFAQs(String keyword) { return faqRepository.findByKeyword(keyword); }
    public FAQ createFAQ(FAQ faq) { return faqRepository.save(faq); }
}
