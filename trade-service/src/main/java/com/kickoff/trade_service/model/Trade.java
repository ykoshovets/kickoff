package com.kickoff.trade_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID initiatorId;

    @Column(nullable = false)
    private UUID receiverId;

    @Column(nullable = false)
    private UUID offeredCardId;

    @Column(nullable = false)
    private UUID requestedCardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status = TradeStatus.PENDING;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(48);

    private OffsetDateTime resolvedAt;

    @Version
    private Long version;
}