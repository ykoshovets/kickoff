package com.kickoff.prediction_service.dto;

import com.kickoff.prediction_service.model.PredictionResult;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PredictionResponse(
        UUID id,
        UUID userId,
        Integer gameExternalId,
        Integer gameweek,
        Integer predictedHomeScore,
        Integer predictedAwayScore,
        PredictionResult predictionResult,
        Integer coinsAwarded,
        OffsetDateTime evaluatedAt,
        OffsetDateTime createdAt
) {
}