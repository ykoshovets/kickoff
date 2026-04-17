package com.kickoff.match_service.scheduler;

import com.kickoff.match_service.service.GameCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableScheduling
public class GameScheduler {

    private final GameCalendarService gameCalendarService;

    public GameScheduler(GameCalendarService gameCalendarService) {
        this.gameCalendarService = gameCalendarService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void fetchAndProcessFinishedMatches() {
        log.info("Daily fetch — processing finished matches");
        gameCalendarService.fetchAndProcessFinished();
    }
}