package com.kickoff.fantasy_service.client;

import com.kickoff.fantasy_service.dto.CardDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CardServiceClient {

    private final RestClient restClient;

    public CardServiceClient(@Value("${card-service.url}") String cardServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(cardServiceUrl)
                .build();
    }

    public CardDto getCard(UUID cardId) {
        try {
            return restClient.get()
                    .uri("/api/v1/cards/{cardId}", cardId)
                    .retrieve()
                    .body(CardDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch card {}: {}", cardId, e.getMessage());
            return null;
        }
    }

    public List<CardDto> getCollection(UUID userId) {
        try {
            return restClient.get()
                    .uri("/api/v1/cards/collection/{userId}", userId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            log.error("Failed to fetch collection for user {}: {}", userId, e.getMessage());
            return List.of();
        }
    }
}