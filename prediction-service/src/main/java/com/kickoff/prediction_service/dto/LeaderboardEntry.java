package com.kickoff.prediction_service.dto;

public record LeaderboardEntry(
        String username,
        Long totalCoins,
        Long correctResults,
        Long correctScores
) {
}