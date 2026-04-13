package com.kickoff.fantasy_service.repository;

import com.kickoff.fantasy_service.model.FantasyTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyTeamRepository extends JpaRepository<FantasyTeam, UUID> {
    Optional<FantasyTeam> findByUserIdAndGameweek(UUID userId, Integer gameweek);

    List<FantasyTeam> findByGameweek(Integer gameweek);
}