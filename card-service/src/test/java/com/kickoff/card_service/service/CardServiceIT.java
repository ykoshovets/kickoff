package com.kickoff.card_service.service;

import com.kickoff.card_service.client.MatchServiceClient;
import com.kickoff.card_service.dto.PlayerDto;
import com.kickoff.card_service.event.PackOpenedEvent;
import com.kickoff.card_service.model.CardTier;
import com.kickoff.card_service.model.PlayerCard;
import com.kickoff.card_service.repository.CollectionProgressRepository;
import com.kickoff.card_service.repository.PlayerCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CardServiceIT {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("kickoff")
            .withUsername("kickoff")
            .withPassword("kickoff");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private CardService cardService;

    @Autowired
    private PlayerCardRepository playerCardRepository;

    @Autowired
    private CollectionProgressRepository collectionProgressRepository;

    @Autowired
    private RedisTemplate<String, List<PlayerDto>> playerCacheTemplate;

    @MockitoBean
    private MatchServiceClient matchServiceClient;

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean(name = "defaultRetryTopicKafkaTemplate")
    private KafkaTemplate<?, ?> retryTopicKafkaTemplate;

    @MockitoBean(name = "defaultKafkaListenerContainerFactory")
    private ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory;

    private static final String PLAYERS_CACHE_KEY = "players:all";

    private final PlayerDto player1 = new PlayerDto(1, "Player One", 10, "Team A", 100, 100);
    private final PlayerDto player2 = new PlayerDto(2, "Player Two", 20, "Team B", 100, 100);
    private final PlayerDto player3 = new PlayerDto(3, "Player Three", 30, "Team C", 100, 100);

    @BeforeEach
    void setUp() {
        playerCardRepository.deleteAll();
        collectionProgressRepository.deleteAll();
        playerCacheTemplate.delete(PLAYERS_CACHE_KEY);

        when(matchServiceClient.getAllPlayers())
                .thenReturn(List.of(player1, player2, player3));
    }

    @Test
    void processPackOpenedCreatesCardsForUser() {
        UUID userId = UUID.randomUUID();

        cardService.processPackOpened(new PackOpenedEvent(userId, UUID.randomUUID(), 3, null));

        List<PlayerCard> cards = playerCardRepository.findAllByUserId(userId);
        assertEquals(3, cards.size());
        assertTrue(cards.stream().allMatch(c -> c.getTier() == CardTier.BRONZE));
        assertTrue(cards.stream().allMatch(c -> c.getUserId().equals(userId)));
    }

    @Test
    void processPackOpenedWithGuaranteedTeamIncludesTeamCard() {
        UUID userId = UUID.randomUUID();

        cardService.processPackOpened(new PackOpenedEvent(userId, UUID.randomUUID(), 3, 10));

        List<PlayerCard> cards = playerCardRepository.findAllByUserId(userId);
        assertEquals(3, cards.size());
        assertTrue(cards.stream().anyMatch(c -> c.getTeamId().equals(10)));
    }

    @Test
    void processPackOpenedUpdatesCollectionProgress() {
        UUID userId = UUID.randomUUID();

        cardService.processPackOpened(new PackOpenedEvent(userId, UUID.randomUUID(), 3, null));

        var progress = collectionProgressRepository.findByUserId(userId).orElseThrow();
        assertEquals(3, progress.getTotalCards());
        assertEquals(3, progress.getBronzeCount());
    }

    @Test
    void processPackOpenedDuplicateGoldCardAwardsCoinsInsteadOfCard() {
        UUID userId = UUID.randomUUID();

        PlayerCard goldCard = new PlayerCard();
        goldCard.setUserId(userId);
        goldCard.setPlayerId(player1.externalId());
        goldCard.setPlayerName(player1.name());
        goldCard.setTeamId(player1.teamExternalId());
        goldCard.setTeamName(player1.teamName());
        goldCard.setTier(CardTier.GOLD);
        playerCardRepository.save(goldCard);

        when(matchServiceClient.getAllPlayers()).thenReturn(List.of(player1));
        cardService.processPackOpened(new PackOpenedEvent(userId, UUID.randomUUID(), 1, null));

        List<PlayerCard> cards = playerCardRepository.findAllByUserId(userId);
        assertEquals(1, cards.size());
    }

    @Test
    void redisCacheStoresAndRetrievesPlayers() {
        List<PlayerDto> players = List.of(player1, player2, player3);
        playerCacheTemplate.opsForValue().set(PLAYERS_CACHE_KEY, players);

        List<PlayerDto> cached = playerCacheTemplate.opsForValue().get(PLAYERS_CACHE_KEY);

        assertNotNull(cached);
        assertEquals(3, cached.size());
        assertEquals(player1.externalId(), cached.getFirst().externalId());
        assertEquals(player1.name(), cached.getFirst().name());
    }
}