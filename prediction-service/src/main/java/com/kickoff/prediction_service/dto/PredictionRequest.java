package com.kickoff.prediction_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PredictionRequest(
        @NotNull UUID userId,
        @NotNull Integer gameExternalId,
        @NotNull @Min(1) @Max(38) Integer gameweek,
        @NotNull @Min(0) @Max(20) Integer predictedHomeScore,
        @NotNull @Min(0) @Max(20) Integer predictedAwayScore
) {
}