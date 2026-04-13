package com.kickoff.pack_service.service;

import com.kickoff.pack_service.client.CoinServiceClient;
import com.kickoff.pack_service.event.PackOpenedEvent;
import com.kickoff.pack_service.model.PackDefinition;
import com.kickoff.pack_service.model.PackPurchase;
import com.kickoff.pack_service.model.PackType;
import com.kickoff.pack_service.repository.PackDefinitionRepository;
import com.kickoff.pack_service.repository.PackPurchaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PackService {

    private final PackDefinitionRepository packDefinitionRepository;
    private final PackPurchaseRepository packPurchaseRepository;
    private final CoinServiceClient coinServiceClient;
    private final KafkaTemplate<String, PackOpenedEvent> kafkaTemplate;

    @Value("${kafka.topics.pack-opened}")
    private String packOpenedTopic;

    public PackService(PackDefinitionRepository packDefinitionRepository,
                       PackPurchaseRepository packPurchaseRepository,
                       CoinServiceClient coinServiceClient,
                       KafkaTemplate<String, PackOpenedEvent> kafkaTemplate) {
        this.packDefinitionRepository = packDefinitionRepository;
        this.packPurchaseRepository = packPurchaseRepository;
        this.coinServiceClient = coinServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public UUID buyPack(UUID userId, PackType packType) {
        PackDefinition definition = packDefinitionRepository.findByPackType(packType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown pack type: " + packType));

        validateWeeklyLimit(userId, packType, definition);

        boolean spent = coinServiceClient.spendCoins(
                userId, definition.getCoinCost(), "PACK_PURCHASE");

        if (!spent) {
            throw new IllegalStateException("Insufficient coins or coin-service unavailable");
        }

        PackPurchase purchase = new PackPurchase();
        purchase.setUserId(userId);
        purchase.setPackType(packType);
        packPurchaseRepository.save(purchase);

        PackOpenedEvent event = new PackOpenedEvent(
                userId,
                purchase.getId(),
                definition.getCardCount(),
                null  // guaranteedTeamId — handled by Gameweek Pack logic later
        );

        kafkaTemplate.send(packOpenedTopic, userId.toString(), event);

        log.info("Pack {} purchased by user {}, {} cards to be generated",
                packType, userId, definition.getCardCount());

        return purchase.getId();
    }

    public List<PackDefinition> getAvailablePacks() {
        return packDefinitionRepository.findAll();
    }

    private void validateWeeklyLimit(UUID userId, PackType packType, PackDefinition definition) {
        if (definition.getWeeklyLimit() == null) return;

        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusWeeks(1);
        long purchasesThisWeek = packPurchaseRepository
                .countPurchasesSince(userId, packType, oneWeekAgo);

        if (purchasesThisWeek >= definition.getWeeklyLimit()) {
            throw new IllegalStateException(
                    "Weekly limit reached for pack type: " + packType);
        }
    }
}