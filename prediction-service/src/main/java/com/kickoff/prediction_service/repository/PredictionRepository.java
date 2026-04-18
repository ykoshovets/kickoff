package com.kickoff.prediction_service.repository;

import com.kickoff.prediction_service.dto.LeaderboardEntry;
import com.kickoff.prediction_service.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PredictionRepository extends JpaRepository<Prediction, UUID> {

    List<Prediction> findByGameExternalId(Integer gameExternalId);

    List<Prediction> findByUserIdAndGameweek(UUID userId, Integer gameweek);

    @Query("""
            SELECT new com.kickoff.prediction_service.dto.LeaderboardEntry(
                p.username,
                SUM(p.coinsAwarded),
                SUM(CASE WHEN p.result = 'CORRECT_RESULT' THEN 1 ELSE 0 END),
                SUM(CASE WHEN p.result = 'CORRECT_SCORE' THEN 1 ELSE 0 END)
            )
            FROM Prediction p
            GROUP BY p.username
            ORDER BY SUM(p.coinsAwarded) DESC
            """)
    List<LeaderboardEntry> findLeaderboard();

    Optional<Prediction> findByUserIdAndGameExternalId(UUID userId, Integer gameExternalId);

}