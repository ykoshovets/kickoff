package com.kickoff.trade_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TradeOfferRequest(
        @NotNull UUID initiatorId,
        @NotNull UUID receiverId,
        @NotNull UUID offeredCardId,
        @NotNull UUID requestedCardId
) {
}