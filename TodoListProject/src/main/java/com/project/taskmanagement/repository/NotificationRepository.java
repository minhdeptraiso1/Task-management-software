package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    boolean existsByDedupKey(
            String dedupKey
    );

    boolean existsByTypeAndDedupKey(
            NotificationType type,
            String dedupKey
    );
}
