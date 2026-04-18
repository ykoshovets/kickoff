package com.kickoff.prediction_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prediction")
@Getter
@Setter
@NoArgsConstructor
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer gameExternalId;

    @Column(nullable = false)
    private Integer gameweek;

    @Column(nullable = false)
    private Integer predictedHomeScore;

    @Column(nullable = false)
    private Integer predictedAwayScore;

    @Enumerated(EnumType.STRING)
    private PredictionResult result = PredictionResult.PENDING;

    private Integer coinsAwarded = 0;
    private OffsetDateTime evaluatedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

}