package com.kickoff.match_service.dto;

public record PlayerResponse(
        Integer externalId,
        String name,
        Integer teamExternalId,
        String teamName,
        Integer teamRarityWeight,
        Integer playerRarityWeight
) {
}