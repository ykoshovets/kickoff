package com.kickoff.prediction_service.service;

import com.kickoff.prediction_service.client.MatchServiceClient;
import com.kickoff.prediction_service.dto.LeaderboardEntry;
import com.kickoff.prediction_service.dto.PredictionRequest;
import com.kickoff.prediction_service.dto.PredictionResponse;
import com.kickoff.prediction_service.event.CoinsAwardedEvent;
import com.kickoff.prediction_service.event.MatchCompletedEvent;
import com.kickoff.prediction_service.mapper.PredictionMapper;
import com.kickoff.prediction_service.model.Prediction;
import com.kickoff.prediction_service.model.PredictionResult;
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

    private static final int CORRECT_RESULT_COINS = 5;
    private static final int CORRECT_SCORE_COINS = 25;

    private final PredictionRepository predictionRepository;
    private final PredictionMapper predictionMapper;
    private final MatchServiceClient matchServiceClient;
    private final KafkaTemplate<String, CoinsAwardedEvent> kafkaTemplate;

    @Value("${kafka.topics.coins-award}")
    private String coinsAwardTopic;

    public PredictionService(PredictionRepository predictionRepository,
                             PredictionMapper predictionMapper,
                             KafkaTemplate<String, CoinsAwardedEvent> kafkaTemplate,
                             MatchServiceClient matchServiceClient) {
        this.predictionRepository = predictionRepository;
        this.predictionMapper = predictionMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.matchServiceClient = matchServiceClient;
    }

    public PredictionResponse createPrediction(PredictionRequest request) {
        validatePredictionAllowed(request.gameExternalId());

        Prediction prediction = predictionRepository
                .findByUserIdAndGameExternalId(request.userId(), request.gameExternalId())
                .orElseGet(Prediction::new);

        predictionMapper.map(request, prediction);
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
                    executor.submit(() -> evaluateSingle(prediction, event))
            );
        }

        log.info("Evaluated {} predictions for game {}", predictions.size(), event.externalId());
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return predictionRepository.findLeaderboard();
    }

    private void evaluateSingle(Prediction prediction, MatchCompletedEvent event) {
        prediction.setResult(evaluate(prediction, event));
        prediction.setEvaluatedAt(OffsetDateTime.now());

        int coins = switch (prediction.getResult()) {
            case CORRECT_SCORE -> CORRECT_SCORE_COINS;
            case CORRECT_RESULT -> CORRECT_RESULT_COINS;
            default -> 0;
        };

        prediction.setCoinsAwarded(coins);
        predictionRepository.save(prediction);

        if (coins > 0) {
            publishCoinsAwarded(prediction);
        }
    }

    private PredictionResult evaluate(Prediction prediction, MatchCompletedEvent event) {
        if (isScoreCorrect(prediction, event)) return PredictionResult.CORRECT_SCORE;
        if (isResultCorrect(prediction, event)) return PredictionResult.CORRECT_RESULT;
        return PredictionResult.INCORRECT;
    }

    private boolean isResultCorrect(Prediction prediction, MatchCompletedEvent event) {
        int predictedResult = Integer.compare(
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore()
        );
        int actualResult = Integer.compare(event.homeScore(), event.awayScore());
        return predictedResult == actualResult;
    }

    private boolean isScoreCorrect(Prediction prediction, MatchCompletedEvent event) {
        return prediction.getPredictedHomeScore().equals(event.homeScore())
                && prediction.getPredictedAwayScore().equals(event.awayScore());
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