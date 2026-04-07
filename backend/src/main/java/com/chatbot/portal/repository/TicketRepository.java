package com.chatbot.portal.repository;

import com.chatbot.portal.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEmail(String email);
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    List<Ticket> findByUserId(Long userId);
}