package com.kickoff.coin_service.kafka;

import com.kickoff.coin_service.event.CoinsAwardedEvent;
import com.kickoff.coin_service.model.TransactionReason;
import com.kickoff.coin_service.service.CoinService;
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

    private final CoinService coinService;

    public CoinsAwardConsumer(CoinService coinService) {
        this.coinService = coinService;
    }

    @KafkaListener(
            topics = "${kafka.topics.coins-award}",
            groupId = "coin-group",
            properties = {
                    "spring.json.value.default.type=com.kickoff.coin_service.event.CoinsAwardedEvent"
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
        coinService.processTransaction(
                event.userId(),
                event.amount(),
                TransactionReason.valueOf(event.reason())
        );
    }
}