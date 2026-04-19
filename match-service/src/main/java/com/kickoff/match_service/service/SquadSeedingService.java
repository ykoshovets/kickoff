package com.kickoff.match_service.service;

import com.kickoff.match_service.client.FootballClient;
import com.kickoff.match_service.dto.TeamResponseDto;
import com.kickoff.match_service.mapper.TeamMapper;
import com.kickoff.match_service.model.Player;
import com.kickoff.match_service.model.Team;
import com.kickoff.match_service.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class SquadSeedingService {

    private static final Set<String> TOP_6 = Set.of("ARS", "CHE", "MCI", "MUN", "LIV", "AVL");

    private static final Random RANDOM = new Random();

    private final FootballClient footballClient;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public SquadSeedingService(FootballClient footballClient, TeamRepository teamRepository, TeamMapper teamMapper) {
        this.footballClient = footballClient;
        this.teamRepository = teamRepository;
        this.teamMapper = teamMapper;
    }

    @Transactional
    public void seedSquads() {
        log.info("Starting squad seeding...");

        TeamResponseDto response = footballClient.getTeams();

        if (response == null || response.teams() == null) {
            log.error("No teams returned from API");
            return;
        }

        List<Team> teams = teamMapper.map(response.teams());
        seedTeams(teams);

        log.info("Squad seeding complete. {} teams processed", response.teams().size());
    }

    private void seedTeams(List<Team> teams) {

        teams.stream().filter(team -> TOP_6.contains(team.getTla()))
                .forEach(team -> team.setTeamRarityWeight(60));

        teams.forEach(this::seedPlayers);
        teamRepository.saveAll(teams);

    }

    private void seedPlayers(Team team) {
        List<Player> shuffled = new ArrayList<>(team.getSquad());
        Collections.shuffle(shuffled);

        List<Player> selected = shuffled.stream()
                .limit(15)
                .toList();

        selected.forEach(p -> {
            p.setPlayerRarityWeight(RANDOM.nextInt(3) == 0 ? 30 : RANDOM.nextInt(2) == 0 ? 60 : 100);
        });

        team.setSquad(selected);
    }
}
