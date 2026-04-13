package com.kickoff.coin_service.repository;

import com.kickoff.coin_service.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
    List<TransactionLog> findAllByUserId(UUID userId);
}