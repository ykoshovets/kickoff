package com.kickoff.match_service.scheduler;

import com.kickoff.match_service.client.FootballClient;
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
    private final FootballClient footballClient;

    public GameScheduler(GameCalendarService gameCalendarService, FootballClient footballClient) {
        this.gameCalendarService = gameCalendarService;
        this.footballClient = footballClient;
    }

    @Scheduled(cron = "0 0 12 * * MON")
    public void fetchLatestResults() {
        Integer currentGameweek = footballClient.getCurrentGameweek();
        log.info("Fetching results for current gameweek {}", currentGameweek);
        gameCalendarService.fetchAndProcessGameweek(currentGameweek);
    }
}