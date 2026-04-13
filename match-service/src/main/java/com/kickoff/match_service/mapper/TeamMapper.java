package com.kickoff.match_service.mapper;

import com.kickoff.match_service.dto.TeamDto;
import com.kickoff.match_service.model.Team;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = PlayerMapper.class)
public interface TeamMapper {
    @Mapping(target = "externalId", source = "id")
    @Mapping(target = "id", ignore = true)
    Team map(TeamDto teamDto);

    List<Team> map(List<TeamDto> teamDtos);

    @AfterMapping
    default void linkPlayers(@MappingTarget Team team) {
        if (team.getSquad() != null) {
            team.getSquad().forEach(player -> player.setTeam(team));
        }
    }
}
