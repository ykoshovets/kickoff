package com.kickoff.match_service.client;

import com.kickoff.match_service.dto.MatchesResponseDto;
import com.kickoff.match_service.dto.TeamResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class FootballClient {

    private final RestClient restClient;
    private final String competition;
    private final String season;

    public FootballClient(RestClient restClient,
                          @Value("${football.competition}") String competition,
                          @Value("${football.season}") String season) {
        this.restClient = restClient;
        this.competition = competition;
        this.season = season;
    }

    public TeamResponseDto getTeams() {
        return restClient
                .get()
                .uri("/competitions/{competition}/teams?season={season}", competition, season)
                .retrieve()
                .body(TeamResponseDto.class);
    }

    public MatchesResponseDto getAllMatches() {
        return restClient
                .get()
                .uri("/competitions/{competition}/matches?season={season}", competition, season)
                .retrieve()
                .body(MatchesResponseDto.class);
    }

    public MatchesResponseDto getMatchesByGameweek(Integer gameweek) {
        return restClient
                .get()
                .uri("/competitions/{competition}/matches?matchday={gameweek}&season={season}",
                        competition, gameweek, season)
                .retrieve()
                .body(MatchesResponseDto.class);
    }
}
