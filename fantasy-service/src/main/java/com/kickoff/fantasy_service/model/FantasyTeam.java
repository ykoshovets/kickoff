package com.kickoff.fantasy_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fantasy_teams")
@Getter
@Setter
@NoArgsConstructor
public class FantasyTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer gameweek;

    @ElementCollection
    @CollectionTable(
            name = "fantasy_team_players",
            joinColumns = @JoinColumn(name = "fantasy_team_id")
    )
    @Column(name = "player_card_id")
    private List<UUID> playerCardIds = new ArrayList<>();

    @Column(nullable = false)
    private OffsetDateTime submittedAt = OffsetDateTime.now();
}