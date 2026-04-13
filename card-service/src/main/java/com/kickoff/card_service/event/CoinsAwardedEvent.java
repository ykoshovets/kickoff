package com.kickoff.card_service.event;

import java.util.UUID;

public record CoinsAwardedEvent(
        UUID userId,
        Integer amount,
        String reason
) {
}