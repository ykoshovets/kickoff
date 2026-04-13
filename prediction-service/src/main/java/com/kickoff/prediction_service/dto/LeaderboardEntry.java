package com.kickoff.prediction_service.dto;

import java.util.UUID;

public record LeaderboardEntry(
        UUID userId,
        Long totalCoins,
        Long correctResults,
        Long correctScores
) {
}