package com.kickoff.prediction_service.service;

import com.kickoff.prediction_service.client.MatchServiceClient;
import com.kickoff.prediction_service.event.CoinsAwardedEvent;
import com.kickoff.prediction_service.event.MatchCompletedEvent;
import com.kickoff.prediction_service.model.Prediction;
import com.kickoff.prediction_service.model.PredictionResult;
import com.kickoff.prediction_service.repository.PredictionRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PredictionServiceIT {

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
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private PredictionRepository predictionRepository;

    @MockitoBean
    private MatchServiceClient matchServiceClient;

    @MockitoBean(name = "defaultRetryTopicKafkaTemplate")
    private KafkaTemplate<?, ?> retryTopicKafkaTemplate;

    private static BlockingQueue<ConsumerRecord<String, CoinsAwardedEvent>> receivedEvents;
    private static KafkaMessageListenerContainer<String, CoinsAwardedEvent> listenerContainer;

    @BeforeEach
    void setUp() {
        predictionRepository.deleteAll();
        receivedEvents = new LinkedBlockingQueue<>();

        when(matchServiceClient.getKickoffTime(any()))
                .thenReturn(OffsetDateTime.now().plusDays(1));

        Map<String, Object> consumerProps = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "*",
                JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, CoinsAwardedEvent.class.getName()
        );

        ContainerProperties containerProps = new ContainerProperties("coins.award");
        containerProps.setMessageListener(
                (MessageListener<String, CoinsAwardedEvent>) record -> receivedEvents.add(record));

        listenerContainer = new KafkaMessageListenerContainer<>(
                new DefaultKafkaConsumerFactory<>(consumerProps),
                containerProps
        );
        listenerContainer.start();
    }

    @AfterEach
    void tearDown() {
        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }

    @Test
    void evaluatePredictions_correctScore_awards25Coins() throws Exception {
        UUID userId = UUID.randomUUID();
        predictionRepository.save(buildPrediction(userId, 537786, 2, 1));

        MatchCompletedEvent event = new MatchCompletedEvent(537786, "HOM", "AWY", 2, 1, 1);
        predictionService.evaluatePredictions(event);

        Prediction evaluated = predictionRepository
                .findByUserIdAndGameExternalId(userId, 537786)
                .orElseThrow();
        assertEquals(PredictionResult.CORRECT_SCORE, evaluated.getResult());
        assertEquals(25, evaluated.getCoinsAwarded());
        assertNotNull(evaluated.getEvaluatedAt());

        ConsumerRecord<String, CoinsAwardedEvent> received =
                receivedEvents.poll(10, TimeUnit.SECONDS);
        assertNotNull(received, "CoinsAwardedEvent should have been published");
        assertEquals(userId, received.value().userId());
        assertEquals(25, received.value().amount());
        assertEquals(PredictionResult.CORRECT_SCORE.name(), received.value().reason());
    }

    @Test
    void evaluatePredictions_correctResult_awards5Coins() throws Exception {
        UUID userId = UUID.randomUUID();
        predictionRepository.save(buildPrediction(userId, 537787, 2, 0));

        MatchCompletedEvent event = new MatchCompletedEvent(537787, "HOM", "AWY", 3, 1, 1);
        predictionService.evaluatePredictions(event);

        Prediction evaluated = predictionRepository
                .findByUserIdAndGameExternalId(userId, 537787)
                .orElseThrow();
        assertEquals(PredictionResult.CORRECT_RESULT, evaluated.getResult());
        assertEquals(5, evaluated.getCoinsAwarded());

        ConsumerRecord<String, CoinsAwardedEvent> received =
                receivedEvents.poll(10, TimeUnit.SECONDS);
        assertNotNull(received, "CoinsAwardedEvent should have been published");
        assertEquals(5, received.value().amount());
        assertEquals(PredictionResult.CORRECT_RESULT.name(), received.value().reason());
    }

    @Test
    void evaluatePredictions_incorrectPrediction_awardsNoCoins() throws Exception {
        UUID userId = UUID.randomUUID();
        predictionRepository.save(buildPrediction(userId, 537788, 2, 0));

        MatchCompletedEvent event = new MatchCompletedEvent(537788, "HOM", "AWY", 0, 1, 1);
        predictionService.evaluatePredictions(event);

        Prediction evaluated = predictionRepository
                .findByUserIdAndGameExternalId(userId, 537788)
                .orElseThrow();
        assertEquals(PredictionResult.INCORRECT, evaluated.getResult());
        assertEquals(0, evaluated.getCoinsAwarded());

        ConsumerRecord<String, CoinsAwardedEvent> received =
                receivedEvents.poll(3, TimeUnit.SECONDS);
        assertNull(received, "No CoinsAwardedEvent should be published for incorrect prediction");
    }

    private Prediction buildPrediction(UUID userId, int gameExternalId, int home, int away) {
        Prediction prediction = new Prediction();
        prediction.setUserId(userId);
        prediction.setUsername("testuser");
        prediction.setGameExternalId(gameExternalId);
        prediction.setGameweek(1);
        prediction.setPredictedHomeScore(home);
        prediction.setPredictedAwayScore(away);
        prediction.setResult(PredictionResult.PENDING);
        return prediction;
    }
}