package com.kickoff.match_service.init;

import com.kickoff.match_service.repository.GameRepository;
import com.kickoff.match_service.repository.TeamRepository;
import com.kickoff.match_service.service.GameCalendarService;
import com.kickoff.match_service.service.SquadSeedingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final SquadSeedingService squadSeedingService;
    private final GameCalendarService gameCalendarService;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;

    public DataInitializer(SquadSeedingService squadSeedingService,
                           GameCalendarService gameCalendarService,
                           TeamRepository teamRepository,
                           GameRepository gameRepository) {
        this.squadSeedingService = squadSeedingService;
        this.gameCalendarService = gameCalendarService;
        this.teamRepository = teamRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (teamRepository.count() == 0) {
            log.info("No teams found — seeding squads...");
            squadSeedingService.seedSquads();
        } else {
            log.info("Squads already seeded, skipping...");
        }

        if (gameRepository.count() == 0) {
            log.info("No games found — seeding calendar...");
            gameCalendarService.seedCalendar();
        } else {
            log.info("Calendar already seeded, skipping...");
        }
    }
}
