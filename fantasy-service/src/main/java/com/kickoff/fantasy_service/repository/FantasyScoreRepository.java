package com.kickoff.fantasy_service.repository;

import com.kickoff.fantasy_service.model.FantasyScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FantasyScoreRepository extends JpaRepository<FantasyScore, UUID> {
    Optional<FantasyScore> findByUserIdAndGameweek(UUID userId, Integer gameweek);

    List<FantasyScore> findByGameweek(Integer gameweek);
}