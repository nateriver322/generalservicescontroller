package com.generalservicesportal.joborder.service;


import com.generalservicesportal.joborder.model.Ticket;
import com.generalservicesportal.joborder.repository.TicketRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public void saveTicket(Ticket ticket) {
        ticketRepository.save(ticket);
    }

    public Ticket findTicketById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public List<Ticket> findTicketsByUsername(String username) {
        return ticketRepository.findByUsername(username);
    }

    public List<Ticket> findTicketsByAssignedPersonnel(String personnelUsername) {
        List<Ticket> tickets = ticketRepository.findTicketsByAssignedPersonnelContaining(personnelUsername);
        // Filter the tickets manually to check exact username match in the comma-separated list
        return tickets.stream()
                      .filter(ticket -> Arrays.asList(ticket.getAssignedPersonnel().split(", "))
                              .contains(personnelUsername))
                      .collect(Collectors.toList());
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> findAllTickets() {
        return ticketRepository.findAll();
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    public void assignTicketToPersonnel(Long ticketId, String personnelUsername, String scheduledRepairDate) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setAssignedPersonnel(personnelUsername);
        ticket.setStatus("Working");
        ticket.setScheduledRepairDate(scheduledRepairDate);  // Ensure this field exists in your Ticket model
        ticketRepository.save(ticket);
    }
    
    public List<Ticket> findResolvedTicketsByAssignedPersonnel(String assignedPersonnel) {
        return ticketRepository.findByAssignedPersonnelAndStatus(assignedPersonnel, "Resolved");
    }
    
    
    
}
