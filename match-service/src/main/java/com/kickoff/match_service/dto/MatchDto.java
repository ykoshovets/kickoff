package com.kickoff.match_service.dto;

public record MatchDto(
        Integer id,
        Integer matchday,
        String status,
        String utcDate,
        MatchTeamDto homeTeam,
        MatchTeamDto awayTeam,
        ScoreDto score
) {}