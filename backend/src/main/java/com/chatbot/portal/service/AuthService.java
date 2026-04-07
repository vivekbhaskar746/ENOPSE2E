package com.chatbot.portal.service;

import com.chatbot.portal.model.User;
import com.chatbot.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String password, String name, User.Role role) {
        if (userRepository.existsByEmail(email)) throw new RuntimeException("Email already exists");
        return userRepository.save(new User(email, passwordEncoder.encode(password), name, role));
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) throw new RuntimeException("Invalid credentials");
        return user;
    }
}
