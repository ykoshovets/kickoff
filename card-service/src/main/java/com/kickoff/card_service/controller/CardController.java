package com.kickoff.card_service.controller;

import com.kickoff.card_service.dto.CollectionProgressResponse;
import com.kickoff.card_service.dto.PlayerCardResponse;
import com.kickoff.card_service.mapper.CardMapper;
import com.kickoff.card_service.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Cards", description = "Card collection endpoints")
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    public CardController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @GetMapping("/collection/{userId}")
    @Operation(summary = "Get user card collection")
    public List<PlayerCardResponse> getCollection(@PathVariable UUID userId) {
        return cardMapper.toResponse(cardService.getCollection(userId));
    }

    @GetMapping("/collection/{userId}/progress")
    @Operation(summary = "Get collection progress summary")
    public CollectionProgressResponse getProgress(@PathVariable UUID userId) {
        return cardMapper.toResponse(cardService.getCollectionProgress(userId));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Upgrade two cards of same tier")
    public ResponseEntity<Void> upgrade(
            @RequestParam UUID userId,
            @RequestParam Integer playerId) {
        cardService.upgradeCard(userId, playerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}/sell")
    @Operation(summary = "Sell a card for coins")
    public ResponseEntity<Void> sell(
            @PathVariable UUID cardId,
            @RequestParam UUID userId) {
        cardService.sellCard(cardId, userId);
        return ResponseEntity.ok().build();
    }
}