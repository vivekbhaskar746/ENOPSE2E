package com.chatbot.portal.controller;

import com.chatbot.portal.model.Ticket;
import com.chatbot.portal.model.User;
import com.chatbot.portal.repository.TicketRepository;
import com.chatbot.portal.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public AdminController(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/tickets/{id}/solution")
    public ResponseEntity<?> updateTicketSolution(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
            ticket.setSolution(request.get("solution"));
            ticket.setStatus(Ticket.TicketStatus.valueOf(request.get("status")));
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
            return ResponseEntity.ok(Map.of("message", "Ticket updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/tickets/{id}/assign")
    public ResponseEntity<?> assignTicket(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        try {
            Ticket ticket = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
            ticket.setAssignedAdminId(request.get("adminId"));
            ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
            return ResponseEntity.ok(Map.of("message", "Ticket assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
