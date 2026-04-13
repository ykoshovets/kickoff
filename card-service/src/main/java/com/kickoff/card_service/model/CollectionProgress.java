package com.kickoff.card_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "collection_progress")
@Getter
@Setter
@NoArgsConstructor
public class CollectionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private Integer totalCards = 0;

    @Column(nullable = false)
    private Integer uniquePlayers = 0;

    @Column(nullable = false)
    private Integer bronzeCount = 0;

    @Column(nullable = false)
    private Integer silverCount = 0;

    @Column(nullable = false)
    private Integer goldCount = 0;

    @Version
    private Long version;
}