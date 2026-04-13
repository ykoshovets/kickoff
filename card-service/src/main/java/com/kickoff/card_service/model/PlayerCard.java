package com.kickoff.card_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_cards")
@Getter
@Setter
@NoArgsConstructor
public class PlayerCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer playerId;

    @Column(nullable = false)
    private String playerName;

    @Column(nullable = false)
    private Integer teamId;

    @Column(nullable = false)
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardTier tier = CardTier.BRONZE;

    @Column(nullable = false)
    private OffsetDateTime obtainedAt = OffsetDateTime.now();
}