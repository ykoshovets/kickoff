package com.kickoff.match_service.init;

import com.kickoff.match_service.repository.TeamRepository;
import com.kickoff.match_service.service.SquadSeedingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final SquadSeedingService squadSeedingService;
    private final TeamRepository teamRepository;

    public DataInitializer(SquadSeedingService squadSeedingService, TeamRepository teamRepository) {
        this.squadSeedingService = squadSeedingService;
        this.teamRepository = teamRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (teamRepository.count() == 0) {
            log.info("No teams found, starting seeding...");
            squadSeedingService.seedSquads();
        } else {
            log.info("Teams already seeded, skipping...");
        }
    }
}
