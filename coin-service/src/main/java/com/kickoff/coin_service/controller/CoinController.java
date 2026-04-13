package com.kickoff.coin_service.controller;

import com.kickoff.coin_service.dto.BalanceDto;
import com.kickoff.coin_service.dto.SpendCoinsRequest;
import com.kickoff.coin_service.dto.TransactionLogDto;
import com.kickoff.coin_service.model.TransactionType;
import com.kickoff.coin_service.service.CoinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coins")
@Tag(name = "Coins", description = "coins award endpoints")
public class CoinController {

    private final CoinService coinService;

    public CoinController(CoinService coinService) {
        this.coinService = coinService;
    }

    @PostMapping("/spend")
    @Operation(summary = "Post a debit transaction")
    public ResponseEntity<String> create(@RequestBody SpendCoinsRequest request) {
        coinService.processTransaction(request.userId(), request.amount(), TransactionType.DEBIT, request.reason());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/balance/{userId}")
    @Operation(summary = "Get user balance")
    public BalanceDto getBalance(@PathVariable UUID userId) {
        return coinService.getBalance(userId);
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Get user transaction history")
    public List<TransactionLogDto> getTransactionHistory(@PathVariable UUID userId) {
        return coinService.getTransactionHistory(userId);
    }
}