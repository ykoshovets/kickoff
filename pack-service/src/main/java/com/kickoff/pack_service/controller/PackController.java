package com.kickoff.pack_service.controller;

import com.kickoff.pack_service.dto.BuyPackResponse;
import com.kickoff.pack_service.dto.PackDefinitionResponse;
import com.kickoff.pack_service.mapper.PackMapper;
import com.kickoff.pack_service.model.PackDefinition;
import com.kickoff.pack_service.model.PackType;
import com.kickoff.pack_service.service.PackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/packs")
@Tag(name = "Packs", description = "Card pack catalogue and purchase flow")
public class PackController {

    private final PackService packService;
    private final PackMapper packMapper;

    public PackController(PackService packService, PackMapper packMapper) {
        this.packService = packService;
        this.packMapper = packMapper;
    }

    @GetMapping
    @Operation(summary = "Get available packs", description = "Returns all pack types with card count, coin cost and weekly purchase limits. Scout Pack is unlimited, others have weekly restrictions")
    public List<PackDefinitionResponse> getAvailablePacks() {
        return packMapper.toResponse(packService.getAvailablePacks());
    }

    @PostMapping("/{packType}/buy")
    @Operation(summary = "Purchase a pack", description = "Deducts coins via coin-service REST call, records purchase and publishes pack.opened Kafka event for card generation. Pack types: SCOUT_PACK (3 cards, 5 coins), TRANSFER_PACK (5 cards, 30 coins, 3/week), GOLDEN_PACK (3 cards, 150 coins, 1/week), GAMEWEEK_PACK (4 cards, 80 coins, 1/week)")
    public ResponseEntity<BuyPackResponse> buyPack(
            @PathVariable PackType packType,
            @RequestParam UUID userId) {
        UUID purchaseId = packService.buyPack(userId, packType);
        PackDefinition definition = packService.getAvailablePacks().stream()
                .filter(p -> p.getPackType() == packType)
                .findFirst()
                .orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BuyPackResponse(purchaseId, packType, definition.getCardCount()));
    }
}