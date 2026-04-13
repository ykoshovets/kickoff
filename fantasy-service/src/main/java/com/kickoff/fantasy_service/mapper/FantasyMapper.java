package com.kickoff.fantasy_service.mapper;

import com.kickoff.fantasy_service.dto.FantasyScoreResponse;
import com.kickoff.fantasy_service.dto.FantasyTeamResponse;
import com.kickoff.fantasy_service.model.FantasyScore;
import com.kickoff.fantasy_service.model.FantasyTeam;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FantasyMapper {
    FantasyTeamResponse toResponse(FantasyTeam team);

    FantasyScoreResponse toResponse(FantasyScore score);

    List<FantasyScoreResponse> toScoreResponse(List<FantasyScore> scores);
}