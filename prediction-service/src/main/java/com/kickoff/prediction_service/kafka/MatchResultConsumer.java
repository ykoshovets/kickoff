package com.kickoff.prediction_service.kafka;

import com.kickoff.prediction_service.event.MatchCompletedEvent;
import com.kickoff.prediction_service.service.PredictionService;
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

    private final PredictionService predictionService;

    public MatchResultConsumer(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @KafkaListener(topics = "${kafka.topics.match-results}", groupId = "prediction-group")
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 5000),
            dltTopicSuffix = ".DLT",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC
    )
    public void consume(MatchCompletedEvent event) {
        log.info("Received MatchCompletedEvent for gameweek {}", event.gameweek());
        predictionService.evaluatePredictions(event);
    }
}