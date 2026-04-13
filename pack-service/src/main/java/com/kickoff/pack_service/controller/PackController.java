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
@Tag(name = "Packs", description = "Pack purchase endpoints")
public class PackController {

    private final PackService packService;
    private final PackMapper packMapper;

    public PackController(PackService packService, PackMapper packMapper) {
        this.packService = packService;
        this.packMapper = packMapper;
    }

    @GetMapping
    @Operation(summary = "Get all available pack types")
    public List<PackDefinitionResponse> getAvailablePacks() {
        return packMapper.toResponse(packService.getAvailablePacks());
    }

    @PostMapping("/{packType}/buy")
    @Operation(summary = "Buy a pack")
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