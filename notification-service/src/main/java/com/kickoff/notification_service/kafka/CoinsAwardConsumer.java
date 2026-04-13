package com.kickoff.notification_service.kafka;

import com.kickoff.notification_service.event.CoinsAwardedEvent;
import com.kickoff.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CoinsAwardConsumer {

    private final NotificationService notificationService;

    public CoinsAwardConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${kafka.topics.coins-award}",
            groupId = "notification-coins-group",
            properties = {
                    "spring.json.value.default.type=com.kickoff.notification_service.event.CoinsAwardedEvent"
            }
    )
    public void consume(CoinsAwardedEvent event) {
        log.info("Received CoinsAwardedEvent for user {}", event.userId());
        notificationService.handleCoinsAwarded(event);
    }
}