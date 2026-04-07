package com.chatbot.portal.service;

import com.chatbot.portal.model.Ticket;
import com.chatbot.portal.repository.TicketRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket createTicket(Ticket ticket) {
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> getTicketById(Long id) { return ticketRepository.findById(id); }
    public List<Ticket> getTicketsByEmail(String email) { return ticketRepository.findByEmail(email); }
    public List<Ticket> getAllTickets() { return ticketRepository.findAll(); }
    public List<Ticket> getTicketsByUserId(Long userId) { return ticketRepository.findByUserId(userId); }

    public Ticket updateTicketStatus(Long id, Ticket.TicketStatus status) {
        return ticketRepository.findById(id).map(t -> {
            t.setStatus(status);
            t.setUpdatedAt(LocalDateTime.now());
            return ticketRepository.save(t);
        }).orElse(null);
    }
}
