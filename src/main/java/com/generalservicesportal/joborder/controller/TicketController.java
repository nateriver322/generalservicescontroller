package com.generalservicesportal.joborder.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.generalservicesportal.joborder.model.Ticket;
import com.generalservicesportal.joborder.service.NotificationService;
import com.generalservicesportal.joborder.service.TicketService;


@RestController
@RequestMapping("/api")
@CrossOrigin
public class TicketController {

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/tickets")
    @Transactional
    public ResponseEntity<?> uploadTicket(@RequestParam("image") Optional<MultipartFile> optionalFile,
                                          @RequestParam("username") String username,
                                          @RequestParam("priority") String priority,
                                          @RequestParam("latestDateNeeded") String latestDateNeeded,
                                          @RequestParam("workType") String workType,
                                          @RequestParam("requestType") String requestType,
                                          @RequestParam("location") String location,
                                          @RequestParam("description") String description,
                                      @RequestParam("datetime") String clientDatetime)  {
        try {
            Ticket ticket = new Ticket();
            ticket.setUsername(username);
            ticket.setPriority(priority);
            ticket.setLatestDateNeeded(latestDateNeeded.trim().split(",")[0]);
            ticket.setWorkType(workType);
            ticket.setRequestType(requestType);
            ticket.setLocation(location);

           // Use the client-side datetime instead of server time
        ticket.setDatetime(clientDatetime);


            ticket.setDescription(description);
            ticket.setStatus("Pending");

            // Check if image is present and not empty
            if (optionalFile.isPresent() && !optionalFile.get().isEmpty()) {
                MultipartFile imageFile = optionalFile.get();
                // Ensure the image is less than or equal to 10MB
                if (imageFile.getSize() > 10 * 1024 * 1024) { // 10MB in bytes
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                         .body("File size exceeds the maximum limit of 10MB");
                }

                byte[] imageBytes = imageFile.getBytes();
                ticket.setImage(imageBytes);
            }

            ticketService.saveTicket(ticket);
            return ResponseEntity.ok("Ticket successfully submitted");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error handling image file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving ticket: " + e.getMessage());
        }
    }
    



    @GetMapping("/tickets/user/{username}")
public ResponseEntity<List<Ticket>> getTicketsByUsername(@PathVariable String username) {
    try {
        List<Ticket> tickets = ticketService.findTicketsByUsername(username).stream()
            .filter(ticket -> !ticket.isArchived())  // Filter out archived tickets
            .peek(ticket -> {
                if (ticket.getImage() != null) {
                    String imageBase64 = Base64.getEncoder().encodeToString(ticket.getImage());
                    ticket.setImageBase64(imageBase64);
                    ticket.setImage(null);
                }
            })
            .collect(Collectors.toList());
        if (!tickets.isEmpty()) {
            return ResponseEntity.ok(tickets);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }
}
    
@GetMapping("/tickets")
public ResponseEntity<List<Ticket>> getAllTickets() {
    try {
        List<Ticket> tickets = ticketService.findAllTickets().stream()
            .filter(ticket -> !ticket.isArchived())  // Filter out archived tickets
            .peek(ticket -> {
                if (ticket.getImage() != null) {
                    String imageBase64 = Base64.getEncoder().encodeToString(ticket.getImage());
                    ticket.setImageBase64(imageBase64);
                    ticket.setImage(null);
                }
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }
}
    
    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            Optional<Ticket> ticket = ticketService.getTicketById(id);
            if (ticket.isPresent()) {
                ticketService.deleteTicket(id);
                return ResponseEntity.ok("Ticket successfully deleted");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting ticket: " + e.getMessage());
        }
    }
    
    @PostMapping("/tickets/assign")
    public ResponseEntity<?> assignTicketToPersonnel(
        @RequestParam Long ticketId,
        @RequestParam List<String> personnelUsernames,
        @RequestParam String scheduledRepairDate) {
        try {
            Optional<Ticket> optionalTicket = ticketService.getTicketById(ticketId);
            if (optionalTicket.isPresent()) {
                Ticket ticket = optionalTicket.get();
    
                // Check if work type is "Others"
                if ("Others".equalsIgnoreCase(ticket.getWorkType())) {
                    // Enable manual assignment
                    ticket.setAssignedPersonnel(String.join(", ", personnelUsernames));
                    ticket.setStatus("Ongoing");
    
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy");
                    Date date = inputFormat.parse(scheduledRepairDate.split("T")[0]);
                    String formattedDate = outputFormat.format(date);
    
                    ticket.setScheduledRepairDate(formattedDate);
    
                    ticketService.saveTicket(ticket);
                    return ResponseEntity.ok("Ticket with work type 'Others' successfully assigned manually");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                         .body("Manual assignment is only allowed for tickets with work type 'Others'");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error assigning ticket: " + e.getMessage());
        }
    }
    

    @GetMapping("/tickets/personnel/{personnelUsername}")
public ResponseEntity<List<Ticket>> getTicketsByPersonnel(@PathVariable String personnelUsername) {
    try {
        List<Ticket> tickets = ticketService.findTicketsByAssignedPersonnel(personnelUsername).stream()
            .filter(ticket -> !ticket.isArchived())  // Filter out archived tickets
            .filter(ticket -> {
                String[] assignedPersonnelArray = ticket.getAssignedPersonnel().split(", ");
                return Arrays.asList(assignedPersonnelArray).contains(personnelUsername);
            })
            .peek(ticket -> {
                if (ticket.getImage() != null) {
                    String imageBase64 = Base64.getEncoder().encodeToString(ticket.getImage());
                    ticket.setImageBase64(imageBase64);
                    ticket.setImage(null);
                }
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }
}
    
    @PostMapping("/tickets/{ticketId}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Long ticketId, @RequestParam String feedback) {
        try {
            Optional<Ticket> optionalTicket = ticketService.getTicketById(ticketId);
            if (optionalTicket.isPresent()) {
                Ticket ticket = optionalTicket.get();
                ticket.setFeedback(feedback);
                ticket.setStatus("Resolved");
                ticketService.saveTicket(ticket);
                
                // Create a notification
                notificationService.createNotification(ticket.getUsername(), 
                    "Your ticket (ID: " + ticketId + ") has been marked as Done and received feedback from personnel.");
                
                return ResponseEntity.ok("Feedback submitted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting feedback: " + e.getMessage());
        }
    }
    
    @PostMapping("/tickets/{ticketId}/user-feedback")
    public ResponseEntity<?> submitUserFeedback(@PathVariable Long ticketId, @RequestBody Map<String, String> payload) {
        try {
            String feedback = payload.get("feedback");
            Optional<Ticket> optionalTicket = ticketService.getTicketById(ticketId);
            if (optionalTicket.isPresent()) {
                Ticket ticket = optionalTicket.get();
                
                if (ticket.getUserFeedback() != null && !ticket.getUserFeedback().isEmpty()) {
                    return ResponseEntity.badRequest().body("User feedback has already been submitted for this ticket");
                }
                
                ticket.setUserFeedback(feedback);
                ticketService.saveTicket(ticket);

                // Create a notification for staff
                notificationService.createNotification("staff", 
                    "New user feedback received for ticket (ID: " + ticketId + ")");

                return ResponseEntity.ok("User feedback submitted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting user feedback: " + e.getMessage());
        }
    }
    
    @PostMapping("/tickets/{ticketId}/staff-feedback")
    public ResponseEntity<?> submitStaffFeedback(@PathVariable Long ticketId, @RequestBody Map<String, String> payload) {
        try {
            String feedback = payload.get("feedback");
            String clientResolvedDatetime = payload.get("resolvedDatetime");
            
            Optional<Ticket> optionalTicket = ticketService.getTicketById(ticketId);
            if (optionalTicket.isPresent()) {
                Ticket ticket = optionalTicket.get();
                ticket.setFeedback(feedback);
                ticket.setStatus("Resolved"); // Change status to "Done"

                if (clientResolvedDatetime != null && !clientResolvedDatetime.isEmpty()) {
                    ticket.setResolvedDatetime(clientResolvedDatetime);
                } else {
                    // Fallback to server time if client datetime is missing
                    String fallbackDatetime = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm").format(new Date());
                    ticket.setResolvedDatetime(fallbackDatetime);
                }
                
                ticketService.saveTicket(ticket);

                // Create a notification for the user
                notificationService.createNotification(ticket.getUsername(), 
                    "Your ticket (ID: " + ticketId + ") has been marked as Done and received feedback from staff.");

                return ResponseEntity.ok("Staff feedback submitted successfully and ticket marked as Done");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting staff feedback: " + e.getMessage());
        }
    }
    

    @PostMapping("/tickets/{ticketId}/personnel-feedback")
public ResponseEntity<?> submitPersonnelFeedback(
        @PathVariable Long ticketId,
        @RequestParam String personnelUsername,
        @RequestBody Map<String, String> payload) {
    try {
        String feedback = payload.get("feedback");
        Optional<Ticket> optionalTicket = ticketService.getTicketById(ticketId);
        if (optionalTicket.isPresent()) {
            Ticket ticket = optionalTicket.get();
            
            if (!ticket.getAssignedPersonnel().contains(personnelUsername)) {
                return ResponseEntity.badRequest().body("This personnel is not assigned to the ticket");
            }
            
            ticket.addPersonnelFeedback(personnelUsername, feedback);
            ticketService.saveTicket(ticket);

            // Create a notification for staff
            notificationService.createNotification("staff", 
                "New personnel feedback received for ticket (ID: " + ticketId + ")");

            return ResponseEntity.ok("Personnel feedback submitted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting personnel feedback: " + e.getMessage());
    }
}

    
    @GetMapping("/tickets/{ticketId}/personnel-feedback/status")
    public ResponseEntity<?> checkPersonnelFeedbackStatus(
            @PathVariable Long ticketId,
            @RequestParam String personnelUsername) {
        try {
            Optional<Ticket> optionalTicket = ticketService.getTicketById(ticketId);
            if (optionalTicket.isPresent()) {
                Ticket ticket = optionalTicket.get();
                
                // Check if feedback already exists for this personnel
                if (ticket.getPersonnelFeedbacks().containsKey(personnelUsername)) {
                    return ResponseEntity.ok("Feedback already submitted");
                } else {
                    return ResponseEntity.ok("No feedback submitted");
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error checking feedback status: " + e.getMessage());
        }
    }

   @GetMapping("/personnel/workload")
public ResponseEntity<Map<String, Integer>> getPersonnelWorkload() {
    try {
        List<Ticket> allTickets = ticketService.findAllTickets();
        Map<String, Integer> workload = new HashMap<>();

        for (Ticket ticket : allTickets) {
            if (ticket.getAssignedPersonnel() != null && !ticket.getAssignedPersonnel().isEmpty()) {
                String[] personnel = ticket.getAssignedPersonnel().split(", ");
                int workloadChange = ticket.getStatus().equalsIgnoreCase("Resolved") ? 0 : 1;
                for (String person : personnel) {
                    workload.put(person, workload.getOrDefault(person, 0) + workloadChange);
                }
            }
        }

        return ResponseEntity.ok(workload);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
    
@PostMapping("/tickets/{id}/archive")
public ResponseEntity<?> archiveTicket(@PathVariable Long id) {
    try {
        Optional<Ticket> optionalTicket = ticketService.getTicketById(id);
        if (optionalTicket.isPresent()) {
            Ticket ticket = optionalTicket.get();
            ticket.setArchived(true);
            ticketService.saveTicket(ticket);
            return ResponseEntity.ok("Ticket successfully archived");
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error archiving ticket: " + e.getMessage());
    }
}

@PostMapping("/tickets/{id}/unarchive")
public ResponseEntity<?> unarchiveTicket(@PathVariable Long id) {
    try {
        Optional<Ticket> optionalTicket = ticketService.getTicketById(id);
        if (optionalTicket.isPresent()) {
            Ticket ticket = optionalTicket.get();
            ticket.setArchived(false);
            ticketService.saveTicket(ticket);
            return ResponseEntity.ok("Ticket successfully unarchived");
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error unarchiving ticket: " + e.getMessage());
    }
}

@GetMapping("/tickets/archived")
public ResponseEntity<List<Ticket>> getArchivedTickets() {
    try {
        List<Ticket> tickets = ticketService.findAllTickets().stream()
            .filter(Ticket::isArchived)
            .peek(ticket -> {
                if (ticket.getImage() != null) {
                    String imageBase64 = Base64.getEncoder().encodeToString(ticket.getImage());
                    ticket.setImageBase64(imageBase64);
                    ticket.setImage(null);
                }
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
    }
}



}
