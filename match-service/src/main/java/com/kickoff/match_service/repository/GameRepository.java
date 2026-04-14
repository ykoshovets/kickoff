package com.kickoff.match_service.repository;

import com.kickoff.match_service.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByExternalId(Integer externalId);

    @Query("SELECT g FROM Game g JOIN FETCH g.homeTeam JOIN FETCH g.awayTeam WHERE g.gameweek = :gameweek")
    List<Game> findByGameweekWithTeams(@Param("gameweek") Integer gameweek);
}