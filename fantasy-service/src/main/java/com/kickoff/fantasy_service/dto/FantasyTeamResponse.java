package com.kickoff.fantasy_service.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FantasyTeamResponse(
        UUID id,
        String username,
        Integer gameweek,
        List<UUID> playerCardIds,
        OffsetDateTime submittedAt
) {
}