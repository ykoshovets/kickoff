package com.kickoff.prediction_service.kafka;

import com.kickoff.prediction_service.event.MatchCompletedEvent;
import com.kickoff.prediction_service.service.PredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MatchResultConsumer {

    private final PredictionService predictionService;

    public MatchResultConsumer(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @KafkaListener(
            topics = "${kafka.topics.match-results}",
            groupId = "prediction-group"
    )
    public void consume(MatchCompletedEvent event) {
        log.info("Received MatchCompletedEvent for game {}", event.externalId());
        predictionService.evaluatePredictions(event);
    }
}