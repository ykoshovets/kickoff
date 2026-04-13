package com.kickoff.trade_service.scheduler;

import com.kickoff.trade_service.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableScheduling
public class TradeScheduler {

    private final TradeService tradeService;

    public TradeScheduler(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void expirePendingTrades() {
        log.info("Running trade expiry job");
        tradeService.expirePendingTrades();
    }
}