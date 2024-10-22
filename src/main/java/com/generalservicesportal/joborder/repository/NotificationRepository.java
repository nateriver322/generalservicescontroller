package com.generalservicesportal.joborder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.generalservicesportal.joborder.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameAndReadFalse(String username);
}
