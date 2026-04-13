package com.kickoff.pack_service.repository;

import com.kickoff.pack_service.model.PackDefinition;
import com.kickoff.pack_service.model.PackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PackDefinitionRepository extends JpaRepository<PackDefinition, UUID> {
    Optional<PackDefinition> findByPackType(PackType packType);
}