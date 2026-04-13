package com.kickoff.notification_service.mapper;

import com.kickoff.notification_service.dto.NotificationResponse;
import com.kickoff.notification_service.model.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponse(List<Notification> notifications);
}