package com.kickoff.pack_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pack_purchases")
@Getter
@Setter
@NoArgsConstructor
public class PackPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackType packType;

    @Column(nullable = false)
    private OffsetDateTime purchasedAt = OffsetDateTime.now();
}