package com.kickoff.coin_service.service;

import com.kickoff.coin_service.dto.BalanceDto;
import com.kickoff.coin_service.dto.TransactionLogDto;
import com.kickoff.coin_service.mapper.TransactionLogMapper;
import com.kickoff.coin_service.model.TransactionLog;
import com.kickoff.coin_service.model.TransactionReason;
import com.kickoff.coin_service.model.TransactionType;
import com.kickoff.coin_service.model.Wallet;
import com.kickoff.coin_service.repository.TransactionLogRepository;
import com.kickoff.coin_service.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CoinService {

    private final WalletRepository walletRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final TransactionLogMapper transactionLogMapper;

    public CoinService(WalletRepository walletRepository, TransactionLogRepository transactionLogRepository,
                       TransactionLogMapper transactionLogMapper) {
        this.walletRepository = walletRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.transactionLogMapper = transactionLogMapper;
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public void processTransaction(UUID userId, Integer amount, TransactionReason reason) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> createWallet(userId));

        if (reason.getTransactionType() == TransactionType.DEBIT && wallet.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance");
        }

        int delta = reason.getTransactionType() == TransactionType.DEBIT ? -amount : amount;
        wallet.setBalance(wallet.getBalance() + delta);

        walletRepository.save(wallet);
        transactionLogRepository.save(
                new TransactionLog(amount, userId, reason.getTransactionType(), reason));
    }

    public BalanceDto getBalance(UUID userId) {
        return walletRepository.findByUserId(userId)
                .map(wallet -> new BalanceDto(wallet.getBalance()))
                .orElse(new BalanceDto(0));
    }

    public List<TransactionLogDto> getTransactionHistory(UUID userId) {
        List<TransactionLog> transactionHistory = transactionLogRepository.findAllByUserId(userId);
        return transactionLogMapper.map(transactionHistory);
    }

    private Wallet createWallet(UUID userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(0);
        return walletRepository.save(wallet);
    }

}