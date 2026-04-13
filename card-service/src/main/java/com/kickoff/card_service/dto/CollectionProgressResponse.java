package com.kickoff.card_service.dto;

import java.util.UUID;

public record CollectionProgressResponse(
        UUID userId,
        Integer totalCards,
        Integer uniquePlayers,
        Integer bronzeCount,
        Integer silverCount,
        Integer goldCount
) {
}