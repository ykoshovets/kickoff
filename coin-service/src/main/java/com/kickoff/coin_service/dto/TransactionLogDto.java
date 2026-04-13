package com.kickoff.coin_service.dto;

import com.kickoff.coin_service.model.TransactionType;

import java.time.OffsetDateTime;

public record TransactionLogDto(
        Integer amount,
        TransactionType transactionType,
        OffsetDateTime createdAt
) {
}