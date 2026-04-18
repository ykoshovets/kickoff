package com.kickoff.coin_service.model;

import lombok.Getter;

@Getter
public enum TransactionReason {
    CORRECT_SCORE(TransactionType.CREDIT),
    CORRECT_RESULT(TransactionType.CREDIT),
    DUPLICATE_GOLD(TransactionType.CREDIT),
    CARD_SELL(TransactionType.CREDIT),
    PACK_PURCHASE(TransactionType.DEBIT);

    private final TransactionType transactionType;

    TransactionReason(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}