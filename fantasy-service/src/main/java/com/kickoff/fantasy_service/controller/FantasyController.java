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
@Tag(name = "Fantasy", description = "Fantasy XI team selection, scoring and leaderboard")
public class FantasyController {

    private final FantasyService fantasyService;

    public FantasyController(FantasyService fantasyService) {
        this.fantasyService = fantasyService;
    }

    @PostMapping("/team")
    @Operation(summary = "Submit Fantasy XI", description = "Select exactly 11 cards from your collection as your fantasy team for a gameweek. Can be resubmitted to update selection before the gameweek starts")
    public ResponseEntity<FantasyTeamResponse> submitTeam(@Valid @RequestBody FantasyTeamRequest request) {
        return ResponseEntity.ok(fantasyService.submitTeam(request));
    }

    @GetMapping("/team")
    @Operation(summary = "Get Fantasy XI", description = "Returns the submitted fantasy team for a specific user and gameweek")
    public ResponseEntity<FantasyTeamResponse> getTeam(
            @RequestParam UUID userId,
            @RequestParam Integer gameweek) {
        return ResponseEntity.ok(fantasyService.getTeam(userId, gameweek));
    }

    @GetMapping("/scores")
    @Operation(summary = "Get gameweek scores", description = "Returns scores for all users who submitted a fantasy team for the specified gameweek")
    public List<FantasyScoreResponse> getScores(@RequestParam Integer gameweek) {
        return fantasyService.getScores(gameweek);
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get fantasy leaderboard", description = "Returns all users ranked by total points for the gameweek. Powered by Redis sorted set for fast retrieval")
    public List<LeaderboardEntry> getLeaderboard(@RequestParam Integer gameweek) {
        return fantasyService.getLeaderboard(gameweek);
    }
}