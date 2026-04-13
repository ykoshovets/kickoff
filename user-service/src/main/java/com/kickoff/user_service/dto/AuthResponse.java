package com.kickoff.user_service.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String token
) {
}