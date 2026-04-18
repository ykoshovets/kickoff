package com.kickoff.prediction_service.service;

import com.kickoff.prediction_service.client.MatchServiceClient;
import com.kickoff.prediction_service.dto.LeaderboardEntry;
import com.kickoff.prediction_service.dto.PredictionRequest;
import com.kickoff.prediction_service.dto.PredictionResponse;
import com.kickoff.prediction_service.event.CoinsAwardedEvent;
import com.kickoff.prediction_service.event.MatchCompletedEvent;
import com.kickoff.prediction_service.mapper.PredictionMapper;
import com.kickoff.prediction_service.model.Prediction;
import com.kickoff.prediction_service.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final PredictionMapper predictionMapper;
    private final PredictionEvaluator predictionEvaluator;
    private final MatchServiceClient matchServiceClient;
    private final KafkaTemplate<String, CoinsAwardedEvent> kafkaTemplate;

    @Value("${kafka.topics.coins-award}")
    private String coinsAwardTopic;

    public PredictionService(PredictionRepository predictionRepository,
                             PredictionMapper predictionMapper,
                             PredictionEvaluator predictionEvaluator,
                             KafkaTemplate<String, CoinsAwardedEvent> kafkaTemplate,
                             MatchServiceClient matchServiceClient) {
        this.predictionRepository = predictionRepository;
        this.predictionMapper = predictionMapper;
        this.predictionEvaluator = predictionEvaluator;
        this.kafkaTemplate = kafkaTemplate;
        this.matchServiceClient = matchServiceClient;
    }

    public PredictionResponse createPrediction(PredictionRequest request, UUID userId, String username) {
        validatePredictionAllowed(request.gameExternalId());

        Prediction prediction = predictionRepository
                .findByUserIdAndGameExternalId(userId, request.gameExternalId())
                .orElseGet(Prediction::new);

        predictionMapper.map(request, prediction);
        prediction.setUserId(userId);
        prediction.setUsername(username);
        return predictionMapper.toResponse(predictionRepository.save(prediction));
    }

    public List<PredictionResponse> getPredictions(UUID userId, Integer gameweek) {
        return predictionMapper.toResponse(
                predictionRepository.findByUserIdAndGameweek(userId, gameweek)
        );
    }

    @Transactional
    public void evaluatePredictions(MatchCompletedEvent event) {
        log.info("Evaluating predictions for game {}", event.externalId());

        List<Prediction> predictions = predictionRepository
                .findByGameExternalId(event.externalId());

        if (predictions.isEmpty()) {
            log.info("No predictions found for game {}", event.externalId());
            return;
        }

        // Virtual Threads — evaluate all users in parallel
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            predictions.forEach(prediction ->
                    executor.submit(() -> {
                        predictionEvaluator.evaluate(prediction, event);
                        predictionRepository.save(prediction);
                        if (prediction.getCoinsAwarded() > 0) {
                            publishCoinsAwarded(prediction);
                        }
                    })

            );
        }

        log.info("Evaluated {} predictions for game {}", predictions.size(), event.externalId());
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return predictionRepository.findLeaderboard();
    }

    private void publishCoinsAwarded(Prediction prediction) {
        CoinsAwardedEvent event = new CoinsAwardedEvent(
                prediction.getUserId(),
                prediction.getCoinsAwarded(),
                prediction.getResult()
        );

        kafkaTemplate.send(coinsAwardTopic,
                prediction.getUserId().toString(),
                event);

        log.info("Published CoinsAwardedEvent: {} coins for user {} reason {}",
                prediction.getCoinsAwarded(), prediction.getUserId(), prediction.getResult().name());
    }

    private void validatePredictionAllowed(Integer gameExternalId) {
        OffsetDateTime kickoffTime = matchServiceClient.getKickoffTime(gameExternalId);
        if (kickoffTime != null && OffsetDateTime.now().isAfter(kickoffTime)) {
            throw new IllegalArgumentException(
                    "Cannot predict for a match that has already started");
        }
    }
}