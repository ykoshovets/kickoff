package com.kickoff.card_service.service;

import com.kickoff.card_service.client.MatchServiceClient;
import com.kickoff.card_service.dto.PlayerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardGenerationServiceTest {

    @Mock
    private MatchServiceClient matchServiceClient;

    private CardGenerationService cardGenerationService;

    @BeforeEach
    void setUp() {
        cardGenerationService = new CardGenerationService(matchServiceClient);
    }

    @Test
    void selectPlayersShouldThrowWhenNoPlayersAvailable() {
        when(matchServiceClient.getAllPlayers()).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> cardGenerationService.selectPlayers(3, null));
    }

    @Test
    void selectPlayersShouldReturnCorrectCount() {
        when(matchServiceClient.getAllPlayers()).thenReturn(List.of(
                playerWith(1, 1, 100, 100),
                playerWith(2, 2, 100, 100),
                playerWith(3, 3, 100, 100)
        ));

        List<PlayerDto> result = cardGenerationService.selectPlayers(3, null);

        assertEquals(3, result.size());
    }

    @Test
    void selectPlayersShouldReturnUniquePlayersOnly() {
        when(matchServiceClient.getAllPlayers()).thenReturn(List.of(
                playerWith(1, 1, 100, 100),
                playerWith(2, 2, 100, 100),
                playerWith(3, 3, 100, 100)
        ));

        List<PlayerDto> result = cardGenerationService.selectPlayers(3, null);

        long uniqueCount = result.stream().map(PlayerDto::externalId).distinct().count();
        assertEquals(result.size(), uniqueCount);
    }

    @Test
    void selectPlayersShouldIncludePlayerFromGuaranteedTeam() {
        when(matchServiceClient.getAllPlayers()).thenReturn(List.of(
                playerWith(1, 1, 100, 100),
                playerWith(2, 2, 100, 100)
        ));

        List<PlayerDto> result = cardGenerationService.selectPlayers(1, 2);

        assertEquals(1, result.size());
        assertEquals(2, result.getFirst().teamExternalId());
    }

    @Test
    void selectPlayersShouldReturnFewerCardsWhenPoolExhausted() {
        when(matchServiceClient.getAllPlayers()).thenReturn(List.of(
                playerWith(1, 1, 100, 100),
                playerWith(2, 2, 100, 100)
        ));

        List<PlayerDto> result = cardGenerationService.selectPlayers(3, 99);

        assertEquals(2, result.size());
    }

    @Test
    void selectPlayersShouldReturnSinglePlayerWhenCountIsOne() {
        when(matchServiceClient.getAllPlayers()).thenReturn(List.of(
                playerWith(1, 1, 100, 100)
        ));

        List<PlayerDto> result = cardGenerationService.selectPlayers(1, null);

        assertEquals(1, result.size());
        assertNotNull(result.getFirst());
    }

    private PlayerDto playerWith(int externalId, int teamExternalId,
                                 int teamRarityWeight, int playerRarityWeight) {
        return new PlayerDto(externalId, "Player " + externalId,
                teamExternalId, "Team " + teamExternalId,
                teamRarityWeight, playerRarityWeight);
    }
}