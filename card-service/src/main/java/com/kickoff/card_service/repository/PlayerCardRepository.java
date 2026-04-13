package com.kickoff.card_service.repository;

import com.kickoff.card_service.model.CardTier;
import com.kickoff.card_service.model.PlayerCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerCardRepository extends JpaRepository<PlayerCard, UUID> {

    Optional<PlayerCard> findById(UUID id);

    List<PlayerCard> findAllByUserId(UUID userId);

    Optional<PlayerCard> findByUserIdAndPlayerId(UUID userId, Integer playerId);

    List<PlayerCard> findByUserIdAndPlayerIdAndTier(UUID userId, Integer playerId, CardTier tier);
}