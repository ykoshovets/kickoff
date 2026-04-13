package com.kickoff.prediction_service.controller;

import com.kickoff.prediction_service.dto.LeaderboardEntry;
import com.kickoff.prediction_service.dto.PredictionRequest;
import com.kickoff.prediction_service.dto.PredictionResponse;
import com.kickoff.prediction_service.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/predictions")
@Tag(name = "Predictions", description = "Match prediction endpoints")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    @Operation(summary = "Create a prediction for a match")
    public ResponseEntity<PredictionResponse> create(@RequestBody PredictionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(predictionService.createPrediction(request));
    }

    @GetMapping
    @Operation(summary = "Get predictions for a user and gameweek")
    public List<PredictionResponse> getPredictions(
            @RequestParam UUID userId,
            @RequestParam Integer gameweek) {
        return predictionService.getPredictions(userId, gameweek);
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get prediction leaderboard")
    public List<LeaderboardEntry> getLeaderboard() {
        return predictionService.getLeaderboard();
    }
}