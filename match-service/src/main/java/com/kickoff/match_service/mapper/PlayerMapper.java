package com.kickoff.match_service.mapper;

import com.kickoff.match_service.dto.PlayerDto;
import com.kickoff.match_service.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    @Mapping(target="externalId", source = "id")
    @Mapping(target = "id", ignore = true)
    Player map(PlayerDto playerDto);
    List<Player> map(List<PlayerDto> playerDtos);
}
