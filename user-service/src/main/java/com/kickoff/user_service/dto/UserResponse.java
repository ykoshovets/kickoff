package com.kickoff.user_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        OffsetDateTime createdAt
) {
}