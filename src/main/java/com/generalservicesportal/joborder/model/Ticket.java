package com.generalservicesportal.joborder.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "tickets")
public class Ticket {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Lob  // This annotation is used to denote that this field should be treated as a Large Object
    @Column(name = "image", columnDefinition = "LONGBLOB")  // Optional: Ensures the column is treated as a BLOB
    private byte[] image;

    private String priority;
    private String workType;
    private String requestType;
    private String location;
    private String datetime;
    private String description;
    private String assignedPersonnel;
    private String status = "Pending";
    private String scheduledRepairDate; 
    private String feedback;
    private String userFeedback;
    private String resolvedDatetime;
    @Column(nullable = false)
    private boolean archived = false;    
    
    @ElementCollection
    @CollectionTable(name = "personnel_feedbacks", joinColumns = @JoinColumn(name = "ticket_id"))
    @MapKeyColumn(name = "personnel_username")
    @Column(name = "feedback")
    private Map<String, String> personnelFeedbacks = new HashMap<>();
    

    
    @Transient
    private String imageBase64;
    
    
    public Ticket() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDescription() {
        return description;
    }

    public String getResolvedDatetime() {
        return resolvedDatetime;
    }

    public void setResolvedDatetime(String resolvedDatetime) {
        this.resolvedDatetime = resolvedDatetime;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAssignedPersonnel() {
        return assignedPersonnel;
    }
    
    public void setAssignedPersonnel(String assignedPersonnel) {
        this.assignedPersonnel = assignedPersonnel;
    }
    
    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    public String getScheduledRepairDate() {
        return scheduledRepairDate;
    }

    public void setScheduledRepairDate(String scheduledRepairDate) {
        this.scheduledRepairDate = scheduledRepairDate;
    }
  
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public String getUserFeedback() {
        return userFeedback;
    }

    public void setUserFeedback(String userFeedback) {
        this.userFeedback = userFeedback;
    }
    
    public Map<String, String> getPersonnelFeedbacks() {
        return personnelFeedbacks;
    }

    public void setPersonnelFeedbacks(Map<String, String> personnelFeedbacks) {
        this.personnelFeedbacks = personnelFeedbacks;
    }

    public void addPersonnelFeedback(String personnelUsername, String feedback) {
        this.personnelFeedbacks.put(personnelUsername, feedback);
    }

    public boolean isArchived() {
        return archived;
    }
    
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

}
