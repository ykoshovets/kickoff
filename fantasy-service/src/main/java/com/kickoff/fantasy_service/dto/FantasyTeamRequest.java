package com.kickoff.fantasy_service.dto;

import java.util.List;
import java.util.UUID;

public record FantasyTeamRequest(
        UUID userId,
        Integer gameweek,
        List<UUID> playerCardIds
) {
}