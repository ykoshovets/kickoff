package com.kickoff.notification_service.kafka;

import com.kickoff.notification_service.event.CoinsAwardedEvent;
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
    @RetryableTopic(
            attempts = "2",
            backOff = @BackOff(delay = 5000),
            dltTopicSuffix = ".DLT",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC
    )
    public void consume(CoinsAwardedEvent event) {
        log.info("Received CoinsAwardedEvent for user {}", event.userId());
        notificationService.handleCoinsAwarded(event);
    }
}