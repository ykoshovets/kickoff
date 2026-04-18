package com.kickoff.prediction_service.controller;

import com.kickoff.prediction_service.dto.LeaderboardEntry;
import com.kickoff.prediction_service.dto.PredictionRequest;
import com.kickoff.prediction_service.dto.PredictionResponse;
import com.kickoff.prediction_service.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/predictions")
@Tag(name = "Predictions", description = "Match score predictions and leaderboard")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<PredictionResponse> create(
            @Valid @RequestBody PredictionRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Username") String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(predictionService.createPrediction(request, userId, username));
    }

    @GetMapping
    @Operation(summary = "Get predictions for gameweek", description = "Returns all predictions made by a user for a specific gameweek including evaluation result and coins awarded")
    public List<PredictionResponse> getPredictions(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam Integer gameweek) {
        return predictionService.getPredictions(userId, gameweek);
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get prediction leaderboard", description = "Returns all users ranked by total coins earned from predictions, with correct result and correct score counts")
    public List<LeaderboardEntry> getLeaderboard() {
        return predictionService.getLeaderboard();
    }
}