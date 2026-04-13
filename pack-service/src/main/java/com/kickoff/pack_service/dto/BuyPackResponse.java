package com.kickoff.pack_service.dto;

import com.kickoff.pack_service.model.PackType;

import java.util.UUID;

public record BuyPackResponse(
        UUID purchaseId,
        PackType packType,
        Integer cardsToBeGenerated
) {
}