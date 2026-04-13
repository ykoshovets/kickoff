package com.kickoff.prediction_service.event;

public record MatchCompletedEvent(
        Integer externalId,
        String homeTeamTla,
        String awayTeamTla,
        Integer homeScore,
        Integer awayScore,
        Integer gameweek
) {
}