package com.kickoff.card_service.client;

import com.kickoff.card_service.dto.PlayerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class MatchServiceClient {

    private static final Duration CACHE_TTL = Duration.ofDays(30);
    private static final String PLAYERS_CACHE_KEY = "players:all";

    private final RestClient restClient;
    private final RedisTemplate<String, List<PlayerDto>> playerCacheTemplate;


    public MatchServiceClient(
            @Value("${match-service.url}") String matchServiceUrl,
            RedisTemplate<String, List<PlayerDto>> playerCacheTemplate) {
        this.restClient = RestClient.builder()
                .baseUrl(matchServiceUrl)
                .build();
        this.playerCacheTemplate = playerCacheTemplate;
    }

    public List<PlayerDto> getAllPlayers() {
        List<PlayerDto> cached = playerCacheTemplate.opsForValue().get(PLAYERS_CACHE_KEY);
        if (cached != null) {
            log.debug("Players found in Redis cache");
            return cached;
        }

        log.info("Cache miss — fetching players from match-service");

        List<PlayerDto> players = restClient.get()
                .uri("/api/v1/players")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (players != null && !players.isEmpty()) {
            playerCacheTemplate.opsForValue().set(PLAYERS_CACHE_KEY, players, CACHE_TTL);
            log.info("Cached {} players in Redis", players.size());
        }

        return players != null ? players : List.of();
    }
}