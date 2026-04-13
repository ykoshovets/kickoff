package com.kickoff.match_service.repository;

import com.kickoff.match_service.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByExternalId(Integer externalId);

    List<Game> findByGameweek(Integer gameweek);
}