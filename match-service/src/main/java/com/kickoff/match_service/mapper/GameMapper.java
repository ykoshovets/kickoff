package com.kickoff.match_service.mapper;

import com.kickoff.match_service.dto.GameResponseDto;
import com.kickoff.match_service.dto.MatchDto;
import com.kickoff.match_service.model.Game;
import com.kickoff.match_service.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "gameweek", ignore = true)
    @Mapping(target = "homeTeam", ignore = true)
    @Mapping(target = "awayTeam", ignore = true)
    @Mapping(target = "kickoffTime", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "homeScore", source = "score.fullTime.home")
    @Mapping(target = "awayScore", source = "score.fullTime.away")
    void mapUpdate(MatchDto matchDto, @MappingTarget Game game);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", source = "matchDto.id")
    @Mapping(target = "gameweek", source = "matchDto.matchday")
    @Mapping(target = "homeScore", source = "matchDto.score.fullTime.home")
    @Mapping(target = "awayScore", source = "matchDto.score.fullTime.away")
    @Mapping(target = "status", source = "matchDto.status")
    @Mapping(target = "kickoffTime", source = "matchDto.utcDate")
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "homeTeam", source = "homeTeam")
    @Mapping(target = "awayTeam", source = "awayTeam")
    Game mapCreate(MatchDto matchDto, Team homeTeam, Team awayTeam);

    @Mapping(target = "homeTeamTla", source = "homeTeam.tla")
    @Mapping(target = "awayTeamTla", source = "awayTeam.tla")
    GameResponseDto toResponse(Game game);

    List<GameResponseDto> toResponse(List<Game> games);

    default OffsetDateTime map(String date) {
        return date != null ? OffsetDateTime.parse(date) : null;
    }
}