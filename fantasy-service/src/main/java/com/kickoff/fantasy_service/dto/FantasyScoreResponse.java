package com.kickoff.fantasy_service.dto;

import java.util.Map;
import java.util.UUID;

public record FantasyScoreResponse(
        UUID userId,
        Integer gameweek,
        Integer totalPoints,
        Map<String, Integer> breakdown
) {
}