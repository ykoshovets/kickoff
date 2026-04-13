package com.kickoff.prediction_service.event;

import com.kickoff.prediction_service.model.PredictionResult;

import java.util.UUID;

public record CoinsAwardedEvent(
        UUID userId,
        Integer amount,
        PredictionResult reason,
        UUID referenceId
) {
}