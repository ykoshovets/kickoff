package com.kickoff.match_service.controller;

import com.kickoff.match_service.dto.PlayerResponse;
import com.kickoff.match_service.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Players", description = "Premier League player data — used by card-service for card generation")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    @Operation(summary = "Get all players", description = "Returns all 300 Premier League players with rarity weights. Called by card-service and cached in Redis for 30 days")
    public List<PlayerResponse> getAll() {
        return playerService.getAll();
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get player by external ID", description = "Returns a single player using the football-data.org external ID")
    public ResponseEntity<PlayerResponse> getByExternalId(@PathVariable Integer externalId) {
        return playerService.getByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}