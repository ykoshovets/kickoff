package com.kickoff.pack_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CoinServiceClient {

    private final RestClient restClient;

    public CoinServiceClient(@Value("${coin-service.url}") String coinServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(coinServiceUrl)
                .build();
    }

    public boolean spendCoins(UUID userId, Integer amount, String reason) {
        try {
            restClient.post()
                    .uri("/api/v1/coins/spend")
                    .body(Map.of(
                            "userId", userId.toString(),
                            "amount", amount,
                            "reason", reason
                    ))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("Failed to spend coins for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}