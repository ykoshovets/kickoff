package com.kickoff.user_service.dto;

public record LoginRequest(
        String username,
        String password
) {
}