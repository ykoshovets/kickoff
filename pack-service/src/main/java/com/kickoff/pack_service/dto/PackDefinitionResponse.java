package com.kickoff.pack_service.dto;

import com.kickoff.pack_service.model.PackType;

import java.util.UUID;

public record PackDefinitionResponse(
        UUID id,
        PackType packType,
        Integer cardCount,
        Integer coinCost,
        Integer weeklyLimit
) {
}