package com.kickoff.match_service.client;

import com.kickoff.match_service.dto.CompetitionResponseDto;
import com.kickoff.match_service.dto.MatchesResponseDto;
import com.kickoff.match_service.dto.TeamResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class FootballClient {

    private final RestClient restClient;

    public FootballClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public TeamResponseDto getTeams(String season) {
        return restClient
                .get()
                .uri("/competitions/PL/teams?season={season}", season)
                .retrieve()
                .body(TeamResponseDto.class);
    }

    public MatchesResponseDto getAllMatches() {
        return restClient
                .get()
                .uri("/competitions/PL/matches?season=2025")
                .retrieve()
                .body(MatchesResponseDto.class);
    }

    public MatchesResponseDto getMatchesByGameweek(Integer gameweek) {
        return restClient
                .get()
                .uri("/competitions/PL/matches?matchday={gameweek}&season=2025", gameweek)
                .retrieve()
                .body(MatchesResponseDto.class);
    }

    public Integer getCurrentGameweek() {
        CompetitionResponseDto response = restClient
                .get()
                .uri("/competitions/PL?season=2025")
                .retrieve()
                .body(CompetitionResponseDto.class);
        return response.season().currentMatchday();
    }
}
