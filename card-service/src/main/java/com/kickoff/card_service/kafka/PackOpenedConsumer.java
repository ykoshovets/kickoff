package com.kickoff.card_service.kafka;

import com.kickoff.card_service.event.PackOpenedEvent;
import com.kickoff.card_service.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PackOpenedConsumer {

    private final CardService cardService;

    public PackOpenedConsumer(CardService cardService) {
        this.cardService = cardService;
    }

    @KafkaListener(topics = "${kafka.topics.pack-opened}", groupId = "card-group")
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 5000),
            dltTopicSuffix = ".DLT",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC
    )
    public void consume(PackOpenedEvent event) {
        log.info("Received PackOpenedEvent for user {}", event.userId());
        cardService.processPackOpened(event);
    }
}