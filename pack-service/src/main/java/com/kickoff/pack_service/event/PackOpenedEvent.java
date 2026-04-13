package com.kickoff.pack_service.event;

import java.util.UUID;

public record PackOpenedEvent(
        UUID userId,
        UUID packId,
        Integer numberOfCards,
        Integer guaranteedTeamId
) {
}