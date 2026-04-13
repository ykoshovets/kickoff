package com.kickoff.fantasy_service.dto;

import java.util.UUID;

public record LeaderboardEntry(
        UUID userId,
        Double totalPoints
) {
}