package com.contentplatform.controller;

import com.contentplatform.model.User;
import com.contentplatform.service.UserService;
import com.contentplatform.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        User user = userService.findByUsername(username);
        if (user != null && userService.validatePassword(user, password)) {
            String token = jwtService.generateToken(username);
            user.setLastLogin(java.time.LocalDateTime.now());
            userService.saveUser(user);
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                )
            ));
        }
        return ResponseEntity.badRequest().body("Invalid credentials");
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        try {
            User user = userService.createUser(
                userData.get("username"),
                userData.get("email"),
                userData.get("password"),
                User.Role.valueOf(userData.getOrDefault("role", "READER"))
            );
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Status endpoint to check backend health
    @GetMapping("/status")
    public ResponseEntity<?> checkBackendStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Content Platform Backend is running",
            "timestamp", java.time.LocalDateTime.now(),
            "port", 9090
        ));
    }
}