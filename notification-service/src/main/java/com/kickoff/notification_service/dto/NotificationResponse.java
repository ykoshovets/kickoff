package com.kickoff.notification_service.dto;

import com.kickoff.notification_service.model.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String message,
        Boolean isRead,
        OffsetDateTime createdAt
) {
}