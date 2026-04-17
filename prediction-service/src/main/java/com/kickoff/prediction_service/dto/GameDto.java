package com.kickoff.prediction_service.dto;

import java.time.OffsetDateTime;

public record GameDto(
        Integer externalId,
        String status,
        OffsetDateTime kickoffTime
) {
}