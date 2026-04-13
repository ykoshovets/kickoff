package com.kickoff.trade_service.dto;

import java.util.UUID;

public record CardDto(
        UUID id,
        UUID userId,
        Integer playerId,
        String playerName,
        String tier
) {
}