package com.chatbot.portal.config;

import com.chatbot.portal.model.FAQ;
import com.chatbot.portal.model.User;
import com.chatbot.portal.repository.FAQRepository;
import com.chatbot.portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FAQRepository faqRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(FAQRepository faqRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.faqRepository = faqRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (faqRepository.count() == 0) initFAQs();
        if (userRepository.count() == 0) initUsers();
    }

    private void initFAQs() {
        faqRepository.save(new FAQ("How do I reset my password?",
                "Click 'Forgot Password' on the login page and follow the email instructions.", "Account"));
        faqRepository.save(new FAQ("How can I contact customer support?",
                "Use this chat, create a support ticket, or call 1-800-SUPPORT.", "Support"));
        faqRepository.save(new FAQ("What are your business hours?",
                "24/7 support. Live agents Mon-Fri 9AM-6PM EST.", "General"));
        faqRepository.save(new FAQ("How do I update my profile?",
                "Go to Settings > Profile and update your information.", "Account"));
        faqRepository.save(new FAQ("What payment methods do you accept?",
                "All major credit cards, PayPal, and bank transfers.", "Billing"));
        faqRepository.save(new FAQ("How do I cancel my subscription?",
                "Settings > Billing > Cancel Subscription.", "Billing"));
    }

    private void initUsers() {
        userRepository.save(new User("admin@support.com", passwordEncoder.encode("admin123"), "Admin User", User.Role.ADMIN));
        userRepository.save(new User("vivek@example.com", passwordEncoder.encode("user123"), "Vivek", User.Role.USER));
    }
}
