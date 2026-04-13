package com.kickoff.card_service.mapper;

import com.kickoff.card_service.dto.CollectionProgressResponse;
import com.kickoff.card_service.dto.PlayerCardResponse;
import com.kickoff.card_service.model.CollectionProgress;
import com.kickoff.card_service.model.PlayerCard;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    PlayerCardResponse toResponse(PlayerCard card);

    List<PlayerCardResponse> toResponse(List<PlayerCard> cards);

    CollectionProgressResponse toResponse(CollectionProgress progress);
}