package com.kickoff.trade_service.controller;

import com.kickoff.trade_service.dto.TradeOfferRequest;
import com.kickoff.trade_service.dto.TradeResponse;
import com.kickoff.trade_service.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trades")
@Tag(name = "Trades", description = "Card trading endpoints")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/offer")
    @Operation(summary = "Create a trade offer")
    public ResponseEntity<TradeResponse> createOffer(@Valid @RequestBody TradeOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tradeService.createOffer(request));
    }

    @PostMapping("/{tradeId}/accept")
    @Operation(summary = "Accept a trade offer")
    public ResponseEntity<TradeResponse> accept(
            @PathVariable UUID tradeId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(tradeService.acceptTrade(tradeId, userId));
    }

    @PostMapping("/{tradeId}/reject")
    @Operation(summary = "Reject a trade offer")
    public ResponseEntity<TradeResponse> reject(
            @PathVariable UUID tradeId,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(tradeService.rejectTrade(tradeId, userId));
    }

    @GetMapping("/incoming")
    @Operation(summary = "Get incoming trade offers")
    public List<TradeResponse> getIncoming(@RequestParam UUID userId) {
        return tradeService.getIncomingTrades(userId);
    }

    @GetMapping("/outgoing")
    @Operation(summary = "Get outgoing trade offers")
    public List<TradeResponse> getOutgoing(@RequestParam UUID userId) {
        return tradeService.getOutgoingTrades(userId);
    }
}