package com.kickoff.match_service.service;

import com.kickoff.match_service.dto.MatchDto;
import com.kickoff.match_service.event.MatchCompletedEvent;
import com.kickoff.match_service.mapper.GameMapper;
import com.kickoff.match_service.model.Game;
import com.kickoff.match_service.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class GameResultProcessor {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final KafkaTemplate<String, MatchCompletedEvent> kafkaTemplate;

    @Value("${kafka.topics.match-results}")
    private String matchResultsTopic;

    public GameResultProcessor(GameRepository gameRepository,
                               GameMapper gameMapper,
                               KafkaTemplate<String, MatchCompletedEvent> kafkaTemplate) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processResult(MatchDto matchDto) {
        gameRepository.findByExternalIdWithTeams(matchDto.id())
                .ifPresentOrElse(
                        game -> {
                            if (game.getCompletedAt() != null) {
                                log.debug("Match {} already processed, skipping", matchDto.id());
                                return;
                            }
                            gameMapper.mapUpdate(matchDto, game);
                            game.setCompletedAt(OffsetDateTime.now());
                            gameRepository.save(game);
                            publishMatchCompleted(game, matchDto);
                        },
                        () -> log.warn("Game not found for match {}", matchDto.id())
                );
    }

    private void publishMatchCompleted(Game game, MatchDto matchDto) {
        MatchCompletedEvent event = new MatchCompletedEvent(
                game.getExternalId(),
                matchDto.homeTeam().tla(),
                matchDto.awayTeam().tla(),
                game.getHomeScore(),
                game.getAwayScore(),
                game.getGameweek()
        );

        kafkaTemplate.send(matchResultsTopic,
                String.valueOf(game.getExternalId()),
                event);

        log.info("Published MatchCompletedEvent: {} {} vs {} {}",
                matchDto.homeTeam().tla(), game.getHomeScore(),
                matchDto.awayTeam().tla(), game.getAwayScore());
    }
}