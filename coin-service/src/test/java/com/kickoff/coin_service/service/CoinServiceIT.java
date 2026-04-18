package com.kickoff.coin_service.service;

import com.kickoff.coin_service.event.CoinsAwardedEvent;
import com.kickoff.coin_service.model.TransactionReason;
import com.kickoff.coin_service.model.Wallet;
import com.kickoff.coin_service.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CoinServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("kickoff")
            .withUsername("kickoff")
            .withPassword("kickoff");

    @Autowired
    private CoinService coinService;

    @Autowired
    private WalletRepository walletRepository;

    @MockitoBean
    private KafkaTemplate<String, CoinsAwardedEvent> kafkaTemplate;

    @MockitoBean(name = "defaultRetryTopicKafkaTemplate")
    private KafkaTemplate<?, ?> retryTopicKafkaTemplate;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletRepository.deleteAll();
    }

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void creditTransactionIncreasesBalance() {
        coinService.processTransaction(userId, 25, TransactionReason.CORRECT_SCORE);

        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertEquals(25, wallet.getBalance());
    }

    @Test
    void debitTransactionDecreasesBalance() {
        coinService.processTransaction(userId, 100, TransactionReason.CORRECT_SCORE);
        coinService.processTransaction(userId, 5, TransactionReason.PACK_PURCHASE);

        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertEquals(95, wallet.getBalance());
    }

    @Test
    void insufficientBalanceThrowsIllegalState() {
        coinService.processTransaction(userId, 10, TransactionReason.CORRECT_SCORE);

        assertThrows(IllegalStateException.class, () ->
                coinService.processTransaction(userId, 100, TransactionReason.PACK_PURCHASE));
    }

    @Test
    void newUserGetsWalletCreatedAutomatically() {
        UUID newUser = UUID.randomUUID();
        coinService.processTransaction(newUser, 5, TransactionReason.CORRECT_RESULT);

        Wallet wallet = walletRepository.findByUserId(newUser).orElseThrow();
        assertEquals(5, wallet.getBalance());
    }

    @Test
    void concurrentAwardsProduceCorrectFinalBalance() throws Exception {
        coinService.processTransaction(userId, 0, TransactionReason.CORRECT_RESULT);

        int threadCount = 5;
        int coinsPerThread = 25;

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() ->
                        coinService.processTransaction(userId, coinsPerThread, TransactionReason.CORRECT_SCORE)
                ));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        }

        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertEquals(threadCount * coinsPerThread, wallet.getBalance());
    }
}