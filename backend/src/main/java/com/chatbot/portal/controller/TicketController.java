package com.chatbot.portal.controller;

import com.chatbot.portal.model.Ticket;
import com.chatbot.portal.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Map<String, Object> request) {
        Ticket ticket = new Ticket();
        ticket.setCustomerName((String) request.get("customerName"));
        ticket.setEmail((String) request.get("email"));
        ticket.setSubject((String) request.get("subject"));
        ticket.setDescription((String) request.get("description"));
        ticket.setPriority(Ticket.Priority.valueOf((String) request.get("priority")));
        if (request.get("userId") != null) ticket.setUserId(Long.valueOf(request.get("userId").toString()));
        return ResponseEntity.ok(ticketService.createTicket(ticket));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<Ticket>> getTicketsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ticketService.getTicketsByEmail(email));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Ticket updated = ticketService.updateTicketStatus(id, Ticket.TicketStatus.valueOf(request.get("status")));
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }
}
