package com.kickoff.coin_service.event;

import com.kickoff.coin_service.model.TransactionReason;

import java.util.UUID;

public record CoinsAwardedEvent(
        UUID userId,
        Integer amount,
        TransactionReason reason,
        UUID referenceId
) {
}