package com.kickoff.card_service.dto;

public record PlayerDto(
        Integer externalId,
        String name,
        Integer teamExternalId,
        String teamName,
        Integer teamRarityWeight,
        Integer playerRarityWeight
) {
}