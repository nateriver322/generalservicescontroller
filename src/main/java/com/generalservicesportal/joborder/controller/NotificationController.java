package com.generalservicesportal.joborder.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generalservicesportal.joborder.model.Notification;
import com.generalservicesportal.joborder.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{username}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String username) {
        List<Notification> notifications = notificationService.getUnreadNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/staff")
    public ResponseEntity<List<Notification>> getStaffNotifications() {
        List<Notification> notifications = notificationService.getStaffNotifications();
        return ResponseEntity.ok(notifications);
    }
}
