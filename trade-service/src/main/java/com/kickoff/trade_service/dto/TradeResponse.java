package com.kickoff.trade_service.dto;

import com.kickoff.trade_service.model.TradeStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TradeResponse(
        UUID id,
        UUID initiatorId,
        UUID receiverId,
        UUID offeredCardId,
        UUID requestedCardId,
        TradeStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        OffsetDateTime resolvedAt
) {
}