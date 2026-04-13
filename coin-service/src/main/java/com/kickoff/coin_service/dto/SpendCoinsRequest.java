package com.kickoff.coin_service.dto;

import com.kickoff.coin_service.model.TransactionReason;

import java.util.UUID;

public record SpendCoinsRequest(UUID userId, Integer amount, TransactionReason reason) {
}