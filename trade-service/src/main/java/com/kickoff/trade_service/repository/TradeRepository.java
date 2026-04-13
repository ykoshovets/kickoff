package com.kickoff.trade_service.repository;

import com.kickoff.trade_service.model.Trade;
import com.kickoff.trade_service.model.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {

    List<Trade> findByInitiatorIdAndStatus(UUID initiatorId, TradeStatus status);

    List<Trade> findByReceiverIdAndStatus(UUID receiverId, TradeStatus status);

    @Query("""
            SELECT t FROM Trade t
            WHERE t.status = 'PENDING'
            AND t.expiresAt < :now
            """)
    List<Trade> findExpiredTrades(@Param("now") OffsetDateTime now);

    boolean existsByInitiatorIdAndOfferedCardIdAndStatus(
            UUID initiatorId, UUID offeredCardId, TradeStatus status);
}