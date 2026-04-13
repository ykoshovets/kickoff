package com.kickoff.trade_service.dto;

import java.util.UUID;

public record TradeOfferRequest(
        UUID initiatorId,
        UUID receiverId,
        UUID offeredCardId,
        UUID requestedCardId
) {
}