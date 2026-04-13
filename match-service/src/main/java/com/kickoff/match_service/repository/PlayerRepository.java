package com.kickoff.match_service.repository;

import com.kickoff.match_service.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    @Query("SELECT p FROM Player p JOIN FETCH p.team WHERE p.externalId = :externalId")
    Optional<Player> findByExternalIdWithTeam(@Param("externalId") Integer externalId);

    @Query("SELECT p FROM Player p JOIN FETCH p.team")
    List<Player> findAllWithTeam();
}