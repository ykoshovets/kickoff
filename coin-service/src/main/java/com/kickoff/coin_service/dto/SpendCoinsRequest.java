package com.kickoff.coin_service.dto;

import java.util.UUID;

public record SpendCoinsRequest(UUID userId, Integer amount, String reason) {
}