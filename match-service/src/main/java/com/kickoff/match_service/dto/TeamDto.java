package com.kickoff.match_service.dto;

import java.util.List;

public record TeamDto(Integer id, String name, String shortName, String tla, List<PlayerApiDto> squad) {
}