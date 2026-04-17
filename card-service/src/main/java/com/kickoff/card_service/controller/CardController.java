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
@Tag(name = "Cards", description = "Player card collection, upgrades, sells and transfers")
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    public CardController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @GetMapping("/collection/{userId}")
    @Operation(summary = "Get user card collection", description = "Returns all cards owned by the user including player name, team, tier and acquisition date")
    public List<PlayerCardResponse> getCollection(@PathVariable UUID userId) {
        return cardMapper.toResponse(cardService.getCollection(userId));
    }

    @GetMapping("/collection/{userId}/progress")
    @Operation(summary = "Get collection progress", description = "Returns total cards, unique players and breakdown by tier (Bronze/Silver/Gold)")
    public CollectionProgressResponse getProgress(@PathVariable UUID userId) {
        return cardMapper.toResponse(cardService.getCollectionProgress(userId));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Get card by ID", description = "Returns a single card by its UUID — used internally by trade and fantasy services")
    public ResponseEntity<PlayerCardResponse> getCard(@PathVariable UUID cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Upgrade card tier", description = "Combines two cards of the same player and tier into one card of the next tier. Bronze+Bronze→Silver, Silver+Silver→Gold")
    public ResponseEntity<Void> upgrade(
            @RequestParam UUID userId,
            @RequestParam Integer playerId) {
        cardService.upgradeCard(userId, playerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardId}/sell")
    @Operation(summary = "Sell a card for coins", description = "Permanently removes the card and credits the user's wallet. Bronze=1 coin, Silver=3 coins, Gold=7 coins")
    public ResponseEntity<Void> sell(
            @PathVariable UUID cardId,
            @RequestParam UUID userId) {
        cardService.sellCard(cardId, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{cardId}/transfer")
    @Operation(summary = "Transfer card ownership")
    public ResponseEntity<Void> transfer(
            @PathVariable UUID cardId,
            @RequestParam UUID newOwnerId) {
        cardService.transferCard(cardId, newOwnerId);
        return ResponseEntity.ok().build();
    }
}