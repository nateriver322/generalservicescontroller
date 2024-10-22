package com.generalservicesportal.joborder.repository;

import com.generalservicesportal.joborder.model.Ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	List<Ticket> findByUsername(String username);
	List<Ticket> findByAssignedPersonnel(String assignedPersonnel);
	Optional<Ticket> findById(Long id);
	List<Ticket> findByAssignedPersonnelAndStatus(String assignedPersonnel, String status);
	@Query("SELECT t FROM Ticket t WHERE t.assignedPersonnel LIKE %:personnelUsername%")
	List<Ticket> findTicketsByAssignedPersonnelContaining(@Param("personnelUsername") String personnelUsername);
}
