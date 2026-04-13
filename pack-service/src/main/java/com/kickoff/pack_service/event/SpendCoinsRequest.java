package com.kickoff.pack_service.event;

import java.util.UUID;

public record SpendCoinsRequest(
        UUID userId,
        Integer amount,
        String reason
) {
}