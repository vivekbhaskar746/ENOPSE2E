package com.chatbot.portal.controller;

import com.chatbot.portal.model.User;
import com.chatbot.portal.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            User.Role role = User.Role.valueOf(request.getOrDefault("role", "USER").toUpperCase());
            User user = authService.register(request.get("email"), request.get("password"), request.get("name"), role);
            return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "user", Map.of("id", user.getId(), "email", user.getEmail(), "name", user.getName(), "role", user.getRole())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            User user = authService.login(request.get("email"), request.get("password"));
            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", Map.of("id", user.getId(), "email", user.getEmail(), "name", user.getName(), "role", user.getRole())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
