package com.kickoff.prediction_service.service;

import com.kickoff.prediction_service.event.MatchCompletedEvent;
import com.kickoff.prediction_service.model.Prediction;
import com.kickoff.prediction_service.model.PredictionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PredictionEvaluatorTest {

    private PredictionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new PredictionEvaluator();
    }

    @Test
    void testEvaluateWithCorrectScoreResult() {
        Prediction prediction = predictionWith(2, 1);
        MatchCompletedEvent event = eventWith(2, 1);

        evaluator.evaluate(prediction, event);

        assertEquals(PredictionResult.CORRECT_SCORE, prediction.getResult());
        assertEquals(25, prediction.getCoinsAwarded());
        assertNotNull(prediction.getEvaluatedAt());
    }

    @Test
    void testEvaluateWithCorrectOutcomeResult() {
        Prediction prediction = predictionWith(2, 0);
        MatchCompletedEvent event = eventWith(3, 1);

        evaluator.evaluate(prediction, event);

        assertEquals(PredictionResult.CORRECT_RESULT, prediction.getResult());
        assertEquals(5, prediction.getCoinsAwarded());
        assertNotNull(prediction.getEvaluatedAt());
    }

    @Test
    void testEvaluateWithIncorrectPrediction() {
        Prediction prediction = predictionWith(2, 0);
        MatchCompletedEvent event = eventWith(0, 1);

        evaluator.evaluate(prediction, event);

        assertEquals(PredictionResult.INCORRECT, prediction.getResult());
        assertEquals(0, prediction.getCoinsAwarded());
        assertNotNull(prediction.getEvaluatedAt());
    }

    @Test
    void testEvaluateWithCorrectScoreResultWhenDraw() {
        Prediction prediction = predictionWith(0, 0);
        MatchCompletedEvent event = eventWith(0, 0);

        evaluator.evaluate(prediction, event);

        assertEquals(PredictionResult.CORRECT_SCORE, prediction.getResult());
        assertEquals(25, prediction.getCoinsAwarded());
    }

    @Test
    void testEvaluateWithCorrectMatchOutcomeResultWhenDraw() {
        Prediction prediction = predictionWith(1, 1);
        MatchCompletedEvent event = eventWith(2, 2);

        evaluator.evaluate(prediction, event);

        assertEquals(PredictionResult.CORRECT_RESULT, prediction.getResult());
        assertEquals(5, prediction.getCoinsAwarded());
    }


    private Prediction predictionWith(int home, int away) {
        Prediction p = new Prediction();
        p.setPredictedHomeScore(home);
        p.setPredictedAwayScore(away);
        return p;
    }

    private MatchCompletedEvent eventWith(int home, int away) {
        return new MatchCompletedEvent(537786, "HOM", "AWY", home, away, 1);
    }
}