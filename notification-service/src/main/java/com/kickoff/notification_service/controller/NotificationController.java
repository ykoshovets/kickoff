package com.kickoff.notification_service.controller;

import com.kickoff.notification_service.dto.NotificationResponse;
import com.kickoff.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "In-app notifications for trade updates and coin awards")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Returns all notifications for a user ordered by date descending. Includes trade status changes and coin award events")
    public List<NotificationResponse> getNotifications(@RequestParam UUID userId) {
        return notificationService.getNotifications(userId);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Returns number of unread notifications. Cached in Redis — invalidated when new notification arrives or notifications are marked as read")
    public ResponseEntity<Long> getUnreadCount(@RequestParam UUID userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read and invalidates the Redis unread count cache")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Marks all notifications as read for a user and clears the Redis unread count cache")
    public ResponseEntity<Void> markAllAsRead(@RequestParam UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}