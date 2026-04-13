package com.kickoff.match_service.client;

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
}
