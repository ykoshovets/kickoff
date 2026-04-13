package com.kickoff.trade_service.client;

import com.kickoff.trade_service.dto.CardDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    public void transferCard(UUID cardId, UUID newOwnerId) {
        try {
            restClient.patch()
                    .uri("/api/v1/cards/{cardId}/transfer?newOwnerId={newOwnerId}",
                            cardId, newOwnerId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to transfer card {} to user {}: {}",
                    cardId, newOwnerId, e.getMessage());
            throw new IllegalStateException("Card transfer failed: " + e.getMessage());
        }
    }
}