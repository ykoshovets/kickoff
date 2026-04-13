package com.kickoff.card_service.model;

public enum CardTier {
    BRONZE, SILVER, GOLD;

    public CardTier next() {
        return switch (this) {
            case BRONZE -> SILVER;
            case SILVER -> GOLD;
            case GOLD -> throw new IllegalStateException("Already at max tier");
        };
    }
}