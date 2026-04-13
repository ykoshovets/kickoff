package com.kickoff.card_service.repository;

import com.kickoff.card_service.model.CollectionProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollectionProgressRepository extends JpaRepository<CollectionProgress, UUID> {
    Optional<CollectionProgress> findByUserId(UUID userId);
}