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
@Tag(name = "Trades", description = "1-for-1 card trading between users")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/offer")
    @Operation(summary = "Create trade offer", description = "Propose a 1-for-1 card swap with another user. Validates that offered card belongs to initiator and requested card belongs to receiver. Offer expires after 48 hours")
    public ResponseEntity<TradeResponse> createOffer(@Valid @RequestBody TradeOfferRequest request,
                                                     @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tradeService.createOffer(request, userId));
    }

    @PostMapping("/{tradeId}/accept")
    @Operation(summary = "Accept trade offer", description = "Atomically swaps both cards between users. Only the receiver can accept. Validates card ownership at time of acceptance to prevent fraud")
    public ResponseEntity<TradeResponse> accept(
            @PathVariable UUID tradeId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(tradeService.acceptTrade(tradeId, userId));
    }

    @PostMapping("/{tradeId}/reject")
    @Operation(summary = "Reject trade offer", description = "Declines the trade offer. Only the receiver can reject. Both users are notified via notification-service")
    public ResponseEntity<TradeResponse> reject(
            @PathVariable UUID tradeId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(tradeService.rejectTrade(tradeId, userId));
    }

    @GetMapping("/incoming")
    @Operation(summary = "Get incoming trade offers", description = "Returns all pending trade offers where the current user is the receiver")
    public List<TradeResponse> getIncoming(@RequestHeader("X-User-Id") UUID userId) {
        return tradeService.getIncomingTrades(userId);
    }

    @GetMapping("/outgoing")
    @Operation(summary = "Get outgoing trade offers", description = "Returns all pending trade offers initiated by the current user")
    public List<TradeResponse> getOutgoing(@RequestHeader("X-User-Id") UUID userId) {
        return tradeService.getOutgoingTrades(userId);
    }
}