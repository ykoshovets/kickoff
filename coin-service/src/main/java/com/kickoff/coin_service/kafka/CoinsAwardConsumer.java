package com.kickoff.coin_service.kafka;

import com.kickoff.coin_service.event.CoinsAwardedEvent;
import com.kickoff.coin_service.service.CoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CoinsAwardConsumer {

    private final CoinService coinService;

    public CoinsAwardConsumer(CoinService coinService) {
        this.coinService = coinService;
    }

    @KafkaListener(
            topics = "${kafka.topics.coins-award}",
            groupId = "coin-group"
    )
    public void consume(CoinsAwardedEvent event) {
        log.info("Received CoinsAwardedEvent for user {}", event.userId());
        coinService.processTransaction(event.userId(), event.amount(), event.reason());
    }
}