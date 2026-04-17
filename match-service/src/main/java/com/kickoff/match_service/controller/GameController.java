package com.kickoff.match_service.controller;

import com.kickoff.match_service.dto.GameResponseDto;
import com.kickoff.match_service.mapper.GameMapper;
import com.kickoff.match_service.service.GameCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
@Tag(name = "Games", description = "Premier League fixture and result data")
public class GameController {

    private final GameCalendarService gameCalendarService;
    private final GameMapper gameMapper;

    public GameController(GameMapper gameMapper,
                          GameCalendarService gameCalendarService) {
        this.gameCalendarService = gameCalendarService;
        this.gameMapper = gameMapper;
    }

    @GetMapping
    @Operation(summary = "Get games by gameweek", description = "Returns all 10 Premier League fixtures for the specified gameweek with scores and team details")
    public List<GameResponseDto> getByGameweek(@RequestParam Integer gameweek) {
        return gameMapper.toResponse(gameCalendarService.findByGameweek(gameweek));
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get game by external ID", description = "Returns a single game using the football-data.org external ID. Used by prediction-service for kickoff time validation")
    public ResponseEntity<GameResponseDto> getByExternalId(@PathVariable Integer externalId) {
        return gameCalendarService.findByExternalId(externalId)
                .map(gameMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fetch/{gameweek}")
    @Operation(summary = "Manually trigger gameweek fetch", description = "Fetches and processes results for a specific gameweek from football-data.org. Publishes match.results Kafka events for each finished game. Automated daily at 1AM")
    public ResponseEntity<String> fetchGameweek(@PathVariable Integer gameweek) {
        gameCalendarService.fetchAndProcessGameweek(gameweek);
        return ResponseEntity.ok("Fetch triggered for gameweek " + gameweek);
    }
}