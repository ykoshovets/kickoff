package com.kickoff.prediction_service.dto;

import java.util.UUID;

public record PredictionRequest(
        UUID userId,
        Integer gameExternalId,
        Integer gameweek,
        Integer predictedHomeScore,
        Integer predictedAwayScore
) {
}