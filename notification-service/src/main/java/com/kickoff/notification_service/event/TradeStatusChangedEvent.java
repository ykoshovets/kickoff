package com.kickoff.notification_service.event;

import java.util.UUID;

public record TradeStatusChangedEvent(
        UUID tradeId,
        UUID initiatorId,
        UUID receiverId,
        UUID offeredCardId,
        UUID requestedCardId,
        String newStatus
) {
}