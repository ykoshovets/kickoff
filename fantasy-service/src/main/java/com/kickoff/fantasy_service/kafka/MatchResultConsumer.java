package com.kickoff.fantasy_service.kafka;

import com.kickoff.fantasy_service.event.MatchCompletedEvent;
import com.kickoff.fantasy_service.service.FantasyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MatchResultConsumer {

    private final FantasyService fantasyService;

    public MatchResultConsumer(FantasyService fantasyService) {
        this.fantasyService = fantasyService;
    }

    @KafkaListener(topics = "${kafka.topics.match-results}", groupId = "fantasy-group")
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 5000),
            dltTopicSuffix = ".DLT",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC
    )
    public void consume(MatchCompletedEvent event) {
        log.info("Received MatchCompletedEvent for gameweek {}", event.gameweek());
        fantasyService.scoreGameweek(event);
    }
}