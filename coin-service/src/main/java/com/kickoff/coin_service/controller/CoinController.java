package com.kickoff.coin_service.controller;

import com.kickoff.coin_service.dto.BalanceDto;
import com.kickoff.coin_service.dto.SpendCoinsRequest;
import com.kickoff.coin_service.dto.TransactionLogDto;
import com.kickoff.coin_service.model.TransactionReason;
import com.kickoff.coin_service.service.CoinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coins")
@Tag(name = "Coins", description = "User coin wallet management and transaction history")
public class CoinController {

    private final CoinService coinService;

    public CoinController(CoinService coinService) {
        this.coinService = coinService;
    }

    @PostMapping("/spend")
    @Operation(summary = "Deduct coins from wallet", description = "Called by pack-service when a user buys a pack. Returns 409 if insufficient balance")
    public ResponseEntity<String> spend(@Valid @RequestBody SpendCoinsRequest request) {
        coinService.processTransaction(request.userId(), request.amount(), TransactionReason.valueOf(request.reason()));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/balance")
    @Operation(summary = "Get coin balance", description = "Returns current coin balance. Returns 0 if user has never received coins")
    public BalanceDto getBalance(@RequestHeader("X-User-Id") UUID userId) {
        return coinService.getBalance(userId);
    }

    @GetMapping("/history")
    @Operation(summary = "Get transaction history", description = "Returns all credit and debit transactions ordered by date. Includes reason for each transaction")
    public List<TransactionLogDto> getTransactionHistory(@RequestHeader("X-User-Id") UUID userId) {
        return coinService.getTransactionHistory(userId);
    }
}