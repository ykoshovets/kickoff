package com.kickoff.card_service.service;

import com.kickoff.card_service.client.MatchServiceClient;
import com.kickoff.card_service.dto.PlayerDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CardGenerationService {

    private final MatchServiceClient matchServiceClient;
    private static final Random RANDOM = new Random();

    public CardGenerationService(MatchServiceClient matchServiceClient) {
        this.matchServiceClient = matchServiceClient;
    }

    public List<PlayerDto> selectPlayers(int count, Integer guaranteedTeamId) {
        List<PlayerDto> allPlayers = matchServiceClient.getAllPlayers();

        if (allPlayers.isEmpty()) {
            throw new IllegalStateException("No players available");
        }

        List<PlayerDto> selected = new ArrayList<>();
        List<PlayerDto> pool = new ArrayList<>(allPlayers);

        if (guaranteedTeamId != null) {
            List<PlayerDto> teamPlayers = pool.stream()
                    .filter(p -> p.teamExternalId().equals(guaranteedTeamId))
                    .toList();

            if (!teamPlayers.isEmpty()) {
                addSelectedPlayer(selected, pool, weightedRandom(teamPlayers));
            }
        }

        while (selected.size() < count && !pool.isEmpty()) {
            addSelectedPlayer(selected, pool, weightedRandom(pool));
        }

        return selected;
    }

    private void addSelectedPlayer(List<PlayerDto> selected, List<PlayerDto> pool, PlayerDto player) {
        selected.add(player);
        pool.removeIf(p -> p.externalId().equals(player.externalId()));
    }

    private PlayerDto weightedRandom(List<PlayerDto> players) {
        int totalWeight = players.stream()
                .mapToInt(p -> p.teamRarityWeight() * p.playerRarityWeight())
                .sum();

        int random = RANDOM.nextInt(totalWeight);
        int cumulative = 0;

        for (PlayerDto player : players) {
            cumulative += player.teamRarityWeight() * player.playerRarityWeight();
            if (random < cumulative) {
                return player;
            }
        }

        return players.getLast();
    }
}