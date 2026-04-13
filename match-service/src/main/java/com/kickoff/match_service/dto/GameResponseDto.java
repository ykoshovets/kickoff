package com.kickoff.match_service.dto;

import java.time.OffsetDateTime;

public record GameResponseDto(
        Integer externalId,
        String homeTeamTla,
        String awayTeamTla,
        Integer homeScore,
        Integer awayScore,
        Integer gameweek,
        String status,
        OffsetDateTime kickoffTime
) {}
