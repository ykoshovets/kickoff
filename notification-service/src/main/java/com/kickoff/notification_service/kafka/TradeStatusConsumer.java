package com.kickoff.notification_service.kafka;

import com.kickoff.notification_service.event.TradeStatusChangedEvent;
import com.kickoff.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TradeStatusConsumer {

    private final NotificationService notificationService;

    public TradeStatusConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "${kafka.topics.trade-status-changed}",
            groupId = "notification-trade-group",
            properties = {
                    "spring.json.value.default.type=com.kickoff.notification_service.event.TradeStatusChangedEvent"
            }
    )
    public void consume(TradeStatusChangedEvent event) {
        log.info("Received TradeStatusChangedEvent: trade {} status {}",
                event.tradeId(), event.newStatus());
        notificationService.handleTradeStatusChanged(event);
    }
}