package com.kickoff.fantasy_service.controller;

import com.kickoff.fantasy_service.dto.FantasyScoreResponse;
import com.kickoff.fantasy_service.dto.FantasyTeamRequest;
import com.kickoff.fantasy_service.dto.FantasyTeamResponse;
import com.kickoff.fantasy_service.dto.LeaderboardEntry;
import com.kickoff.fantasy_service.service.FantasyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fantasy")
@Tag(name = "Fantasy", description = "Fantasy team endpoints")
public class FantasyController {

    private final FantasyService fantasyService;

    public FantasyController(FantasyService fantasyService) {
        this.fantasyService = fantasyService;
    }

    @PostMapping("/team")
    @Operation(summary = "Submit fantasy team for a gameweek")
    public ResponseEntity<FantasyTeamResponse> submitTeam(@Valid @RequestBody FantasyTeamRequest request) {
        return ResponseEntity.ok(fantasyService.submitTeam(request));
    }

    @GetMapping("/team")
    @Operation(summary = "Get fantasy team for a gameweek")
    public ResponseEntity<FantasyTeamResponse> getTeam(
            @RequestParam UUID userId,
            @RequestParam Integer gameweek) {
        return ResponseEntity.ok(fantasyService.getTeam(userId, gameweek));
    }

    @GetMapping("/scores")
    @Operation(summary = "Get scores for a gameweek")
    public List<FantasyScoreResponse> getScores(@RequestParam Integer gameweek) {
        return fantasyService.getScores(gameweek);
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get leaderboard for a gameweek")
    public List<LeaderboardEntry> getLeaderboard(@RequestParam Integer gameweek) {
        return fantasyService.getLeaderboard(gameweek);
    }
}