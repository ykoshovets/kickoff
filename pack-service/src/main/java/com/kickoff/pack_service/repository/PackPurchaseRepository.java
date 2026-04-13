package com.kickoff.pack_service.repository;

import com.kickoff.pack_service.model.PackPurchase;
import com.kickoff.pack_service.model.PackType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PackPurchaseRepository extends JpaRepository<PackPurchase, UUID> {

    @Query("""
            SELECT COUNT(p) FROM PackPurchase p
            WHERE p.userId = :userId
            AND p.packType = :packType
            AND p.purchasedAt >= :since
            """)
    long countPurchasesSince(
            @Param("userId") UUID userId,
            @Param("packType") PackType packType,
            @Param("since") OffsetDateTime since);
}