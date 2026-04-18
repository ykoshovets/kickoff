package com.kickoff.prediction_service.service;

import com.kickoff.prediction_service.event.MatchCompletedEvent;
import com.kickoff.prediction_service.model.Prediction;
import com.kickoff.prediction_service.model.PredictionResult;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class PredictionEvaluator {
    private static final int CORRECT_RESULT_COINS = 5;
    private static final int CORRECT_SCORE_COINS = 25;

    public void evaluate(Prediction prediction, MatchCompletedEvent event) {
        prediction.setResult(determineResult(prediction, event));
        prediction.setEvaluatedAt(OffsetDateTime.now());

        int coins = calculateCoins(prediction.getResult());
        prediction.setCoinsAwarded(coins);
    }

    private PredictionResult determineResult(Prediction prediction, MatchCompletedEvent event) {
        if (isScoreCorrect(prediction, event)) return PredictionResult.CORRECT_SCORE;
        if (isResultCorrect(prediction, event)) return PredictionResult.CORRECT_RESULT;
        return PredictionResult.INCORRECT;
    }

    private int calculateCoins(PredictionResult result) {
        return switch (result) {
            case CORRECT_SCORE -> CORRECT_SCORE_COINS;
            case CORRECT_RESULT -> CORRECT_RESULT_COINS;
            default -> 0;
        };
    }

    private boolean isScoreCorrect(Prediction prediction, MatchCompletedEvent event) {
        return prediction.getPredictedHomeScore().equals(event.homeScore())
                && prediction.getPredictedAwayScore().equals(event.awayScore());
    }

    private boolean isResultCorrect(Prediction prediction, MatchCompletedEvent event) {
        int predictedResult = Integer.compare(
                prediction.getPredictedHomeScore(),
                prediction.getPredictedAwayScore()
        );
        int actualResult = Integer.compare(event.homeScore(), event.awayScore());
        return predictedResult == actualResult;
    }
}