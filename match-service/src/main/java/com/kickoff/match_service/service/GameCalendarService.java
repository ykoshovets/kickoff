package com.kickoff.match_service.service;

import com.kickoff.match_service.client.FootballClient;
import com.kickoff.match_service.dto.MatchDto;
import com.kickoff.match_service.dto.MatchesResponseDto;
import com.kickoff.match_service.mapper.GameMapper;
import com.kickoff.match_service.model.Game;
import com.kickoff.match_service.model.Team;
import com.kickoff.match_service.repository.GameRepository;
import com.kickoff.match_service.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GameCalendarService {

    private static final String FINISHED = "FINISHED";
    private final FootballClient footballClient;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final GameMapper gameMapper;
    private final GameResultProcessor gameResultProcessor;

    public GameCalendarService(FootballClient footballClient,
                               GameRepository gameRepository,
                               TeamRepository teamRepository,
                               GameMapper gameMapper,
                               GameResultProcessor gameResultProcessor) {
        this.footballClient = footballClient;
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.gameMapper = gameMapper;
        this.gameResultProcessor = gameResultProcessor;
    }

    @Transactional
    public void seedCalendar() {
        log.info("Seeding match calendar...");

        MatchesResponseDto response = footballClient.getAllMatches();

        if (response == null || response.matches() == null) {
            log.error("No matches returned from API");
            return;
        }

        Map<Integer, Team> teamsByExternalId = teamRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Team::getExternalId, t -> t));

        List<Game> games = response.matches().stream()
                .map(dto -> gameMapper.mapCreate(
                        dto,
                        teamsByExternalId.get(dto.homeTeam().id()),
                        teamsByExternalId.get(dto.awayTeam().id())
                ))
                .toList();

        gameRepository.saveAll(games);

        log.info("Calendar seeded with {} matches", games.size());
    }

    public void fetchAndProcessGameweek(Integer gameweek) {
        log.info("Fetching results for gameweek {}", gameweek);

        MatchesResponseDto response = footballClient.getMatchesByGameweek(gameweek);

        if (response == null || response.matches() == null) {
            log.warn("No matches returned for gameweek {}", gameweek);
            return;
        }

        response.matches().forEach(gameResultProcessor::processResult);
        log.info("Processed gameweek {}", gameweek);
    }

    public void fetchAndProcessFinished() {
        MatchesResponseDto response = footballClient.getAllMatches();

        if (response == null || response.matches() == null) {
            log.warn("No matches returned from API");
            return;
        }

        List<MatchDto> finishedMatches = response.matches().stream()
                .filter(m -> FINISHED.equals(m.status()))
                .toList();

        log.info("Found {} finished matches — processing", finishedMatches.size());
        finishedMatches.forEach(gameResultProcessor::processResult);
    }

    public List<Game> findByGameweek(Integer gameweek) {
        return gameRepository.findByGameweekWithTeams(gameweek);
    }

    public Optional<Game> findByExternalId(Integer externalId) {
        return gameRepository.findByExternalIdWithTeams(externalId);
    }
}