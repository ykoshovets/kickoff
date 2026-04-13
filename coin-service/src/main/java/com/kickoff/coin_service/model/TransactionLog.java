package com.kickoff.coin_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_logs")
@Immutable
@Getter
public class TransactionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    Integer amount;

    @Column(nullable = false)
    UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TransactionReason reason;

    @Column(updatable = false)
    OffsetDateTime createdAt;

    public TransactionLog() {
    }

    public TransactionLog(Integer amount, UUID userId, TransactionType type, TransactionReason reason) {
        this.amount = amount;
        this.userId = userId;
        this.type = type;
        this.reason = reason;
        this.createdAt = OffsetDateTime.now();
    }

}
