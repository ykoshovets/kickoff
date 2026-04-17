package com.kickoff.fantasy_service.service;

import com.kickoff.fantasy_service.client.CardServiceClient;
import com.kickoff.fantasy_service.dto.*;
import com.kickoff.fantasy_service.event.MatchCompletedEvent;
import com.kickoff.fantasy_service.mapper.FantasyMapper;
import com.kickoff.fantasy_service.model.FantasyScore;
import com.kickoff.fantasy_service.model.FantasyTeam;
import com.kickoff.fantasy_service.repository.FantasyScoreRepository;
import com.kickoff.fantasy_service.repository.FantasyTeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FantasyService {

    private static final String LEADERBOARD_KEY = "fantasy:leaderboard:";

    private final FantasyTeamRepository fantasyTeamRepository;
    private final FantasyScoreRepository fantasyScoreRepository;
    private final CardServiceClient cardServiceClient;
    private final FantasyMapper fantasyMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public FantasyService(FantasyTeamRepository fantasyTeamRepository,
                          FantasyScoreRepository fantasyScoreRepository,
                          CardServiceClient cardServiceClient,
                          FantasyMapper fantasyMapper,
                          RedisTemplate<String, String> redisTemplate) {
        this.fantasyTeamRepository = fantasyTeamRepository;
        this.fantasyScoreRepository = fantasyScoreRepository;
        this.cardServiceClient = cardServiceClient;
        this.fantasyMapper = fantasyMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public FantasyTeamResponse submitTeam(FantasyTeamRequest request) {
        validateCardsOwnedByUser(request.userId(), request.playerCardIds());

        FantasyTeam team = fantasyTeamRepository
                .findByUserIdAndGameweek(request.userId(), request.gameweek())
                .orElseGet(FantasyTeam::new);

        team.setUserId(request.userId());
        team.setGameweek(request.gameweek());
        team.setPlayerCardIds(request.playerCardIds());

        fantasyTeamRepository.save(team);
        log.info("Fantasy team submitted for user {} gameweek {}",
                request.userId(), request.gameweek());

        return fantasyMapper.toResponse(team);
    }

    @Transactional
    public void scoreGameweek(MatchCompletedEvent event) {
        log.info("Scoring fantasy teams for gameweek {}", event.gameweek());

        List<FantasyTeam> teams = fantasyTeamRepository.findByGameweek(event.gameweek());

        teams.forEach(team -> scoreTeam(team, event));

        log.info("Scored {} teams for gameweek {}", teams.size(), event.gameweek());
    }

    private void scoreTeam(FantasyTeam team, MatchCompletedEvent event) {
        List<CardDto> cards = team.getPlayerCardIds().stream()
                .map(cardServiceClient::getCard)
                .filter(Objects::nonNull)
                .toList();

        Map<String, Integer> breakdown = new HashMap<>();
        int totalPoints = 0;

        for (CardDto card : cards) {
            int points = calculatePoints(card, event);
            if (points > 0) {
                breakdown.put(card.playerName(), points);
                totalPoints += points;
            }
        }

        FantasyScore score = fantasyScoreRepository
                .findByUserIdAndGameweek(team.getUserId(), team.getGameweek())
                .orElseGet(FantasyScore::new);

        score.setUserId(team.getUserId());
        score.setGameweek(team.getGameweek());
        score.setTotalPoints(score.getTotalPoints() + totalPoints);
        score.setBreakdown(breakdown);
        fantasyScoreRepository.save(score);

        String leaderboardKey = LEADERBOARD_KEY + team.getGameweek();
        redisTemplate.opsForZSet().add(leaderboardKey,
                team.getUserId().toString(), score.getTotalPoints());

        log.info("User {} scored {} points for gameweek {}",
                team.getUserId(), totalPoints, team.getGameweek());
    }

    private int calculatePoints(CardDto card, MatchCompletedEvent event) {
        int tierMultiplier = switch (card.tier()) {
            case "GOLD" -> 3;
            case "SILVER" -> 2;
            default -> 1;
        };

        // Award bonus if player's team won
        // We don't have player→team→match data here without extra API calls
        // so just return base tier points for now
        return tierMultiplier;
    }

    public FantasyTeamResponse getTeam(UUID userId, Integer gameweek) {
        return fantasyTeamRepository.findByUserIdAndGameweek(userId, gameweek)
                .map(fantasyMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
    }

    public List<FantasyScoreResponse> getScores(Integer gameweek) {
        return fantasyMapper.toScoreResponse(
                fantasyScoreRepository.findByGameweek(gameweek));
    }

    public List<LeaderboardEntry> getLeaderboard(Integer gameweek) {
        String leaderboardKey = LEADERBOARD_KEY + gameweek;

        var entries = redisTemplate.opsForZSet()
                .reverseRangeWithScores(leaderboardKey, 0, -1);

        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        return entries.stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> new LeaderboardEntry(
                        UUID.fromString(entry.getValue()),
                        entry.getScore()
                ))
                .toList();
    }

    private void validateCardsOwnedByUser(UUID userId, List<UUID> cardIds) {
        List<CardDto> collection = cardServiceClient.getCollection(userId);
        Set<UUID> ownedCardIds = collection.stream()
                .map(CardDto::id)
                .collect(Collectors.toSet());

        cardIds.forEach(cardId -> {
            if (!ownedCardIds.contains(cardId)) {
                throw new IllegalArgumentException(
                        "Card " + cardId + " does not belong to user " + userId);
            }
        });
    }
}