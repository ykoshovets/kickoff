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
@Tag(name = "Games", description = "Premier League match data")
public class GameController {

    private final GameCalendarService gameCalendarService;
    private final GameMapper gameMapper;
    public GameController(GameMapper gameMapper,
                          GameCalendarService gameCalendarService) {
        this.gameCalendarService = gameCalendarService;
        this.gameMapper = gameMapper;
    }

    @GetMapping
    public List<GameResponseDto> getByGameweek(@RequestParam Integer gameweek) {
        return gameMapper.toResponse(gameCalendarService.findByGameweek(gameweek));
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<GameResponseDto> getByExternalId(@PathVariable Integer externalId) {
        return gameCalendarService.findByExternalId(externalId)
                .map(gameMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fetch/{gameweek}")
    @Operation(summary = "Manually trigger result fetch for a gameweek")
    public ResponseEntity<String> fetchGameweek(@PathVariable Integer gameweek) {
        gameCalendarService.fetchAndProcessGameweek(gameweek);
        return ResponseEntity.ok("Fetch triggered for gameweek " + gameweek);
    }
}