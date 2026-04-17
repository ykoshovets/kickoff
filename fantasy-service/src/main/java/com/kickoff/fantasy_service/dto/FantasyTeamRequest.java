package com.kickoff.fantasy_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record FantasyTeamRequest(
        @NotNull UUID userId,
        @NotNull @Min(1) @Max(38) Integer gameweek,
        @NotNull @Size(min = 11, max = 11, message = "Fantasy team must have exactly 11 players")
        List<UUID> playerCardIds
) {
}