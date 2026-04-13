package com.kickoff.card_service.dto;

import com.kickoff.card_service.model.CardTier;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PlayerCardResponse(
        UUID id,
        Integer playerId,
        String playerName,
        Integer teamId,
        String teamName,
        CardTier tier,
        OffsetDateTime obtainedAt
) {
}