package com.kickoff.fantasy_service.kafka;

import com.kickoff.fantasy_service.event.MatchCompletedEvent;
import com.kickoff.fantasy_service.service.FantasyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MatchResultConsumer {

    private final FantasyService fantasyService;

    public MatchResultConsumer(FantasyService fantasyService) {
        this.fantasyService = fantasyService;
    }

    @KafkaListener(
            topics = "${kafka.topics.match-results}",
            groupId = "fantasy-group"
    )
    public void consume(MatchCompletedEvent event) {
        log.info("Received MatchCompletedEvent for gameweek {}", event.gameweek());
        fantasyService.scoreGameweek(event);
    }
}