package com.kickoff.fantasy_service.dto;

import java.util.UUID;

public record CardDto(
        UUID id,
        UUID userId,
        Integer playerId,
        String playerName,
        Integer teamId,
        String teamName,
        String tier
) {
}