package com.kickoff.notification_service.kafka;

import com.kickoff.notification_service.event.TradeStatusChangedEvent;
import com.kickoff.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
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
    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 5000),
            dltTopicSuffix = ".DLT",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC
    )
    public void consume(TradeStatusChangedEvent event) {
        log.info("Received TradeStatusChangedEvent: trade {} status {}",
                event.tradeId(), event.newStatus());
        notificationService.handleTradeStatusChanged(event);
    }
}