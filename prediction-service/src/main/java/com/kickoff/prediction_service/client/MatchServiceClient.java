package com.kickoff.prediction_service.client;

import com.kickoff.prediction_service.dto.GameDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class MatchServiceClient {

    private final RestClient restClient;

    public MatchServiceClient(@Value("${match-service.url}") String matchServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(matchServiceUrl)
                .build();
    }

    public OffsetDateTime getKickoffTime(Integer gameExternalId) {
        try {
            GameDto game = restClient.get()
                    .uri("/api/v1/games/{externalId}", gameExternalId)
                    .retrieve()
                    .body(GameDto.class);
            return game != null ? game.kickoffTime() : null;
        } catch (Exception e) {
            log.warn("Could not fetch kickoff time for game {}, allowing prediction", gameExternalId);
            return null;
        }
    }
}