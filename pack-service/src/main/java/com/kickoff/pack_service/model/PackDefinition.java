package com.kickoff.pack_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "pack_definitions")
@Getter
@Setter
@NoArgsConstructor
public class PackDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PackType packType;

    @Column(nullable = false)
    private Integer cardCount;

    @Column(nullable = false)
    private Integer coinCost;

    private Integer weeklyLimit;
}