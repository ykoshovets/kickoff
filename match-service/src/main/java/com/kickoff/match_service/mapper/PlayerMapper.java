package com.kickoff.match_service.mapper;

import com.kickoff.match_service.dto.PlayerApiDto;
import com.kickoff.match_service.dto.PlayerResponse;
import com.kickoff.match_service.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    @Mapping(target="externalId", source = "id")
    @Mapping(target = "id", ignore = true)
    Player map(PlayerApiDto dto);

    List<Player> map(List<PlayerApiDto> dtos);

    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "teamExternalId", source = "team.externalId")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "teamRarityWeight", source = "team.teamRarityWeight")
    @Mapping(target = "playerRarityWeight", source = "playerRarityWeight")
    PlayerResponse toResponse(Player player);

    List<PlayerResponse> toResponse(List<Player> player);
}
