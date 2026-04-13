package com.kickoff.card_service.service;

import com.kickoff.card_service.dto.PlayerDto;
import com.kickoff.card_service.event.CoinsAwardedEvent;
import com.kickoff.card_service.event.PackOpenedEvent;
import com.kickoff.card_service.model.CardTier;
import com.kickoff.card_service.model.CollectionProgress;
import com.kickoff.card_service.model.PlayerCard;
import com.kickoff.card_service.model.TransactionReason;
import com.kickoff.card_service.repository.CollectionProgressRepository;
import com.kickoff.card_service.repository.PlayerCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CardService {

    private static final Map<CardTier, Integer> SELL_PRICES = Map.of(
            CardTier.BRONZE, 1,
            CardTier.SILVER, 3,
            CardTier.GOLD, 7
    );

    private final PlayerCardRepository playerCardRepository;
    private final CollectionProgressRepository collectionProgressRepository;
    private final CardGenerationService cardGenerationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.coins-award}")
    private String coinsAwardTopic;

    public CardService(PlayerCardRepository playerCardRepository,
                       CollectionProgressRepository collectionProgressRepository,
                       CardGenerationService cardGenerationService,
                       KafkaTemplate<String, Object> kafkaTemplate) {
        this.playerCardRepository = playerCardRepository;
        this.collectionProgressRepository = collectionProgressRepository;
        this.cardGenerationService = cardGenerationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processPackOpened(PackOpenedEvent event) {
        log.info("Processing pack for user {}", event.userId());

        List<PlayerDto> players = cardGenerationService.selectPlayers(
                event.numberOfCards(),
                event.guaranteedTeamId()
        );

        players.forEach(player -> generateCard(event.userId(), player));
    }

    private void generateCard(UUID userId, PlayerDto player) {
        Optional<PlayerCard> existing = playerCardRepository
                .findByUserIdAndPlayerId(userId, player.externalId());

        if (existing.isPresent() && existing.get().getTier() == CardTier.GOLD) {
            log.info("Player {} at GOLD for user {} — awarding coins", player.externalId(), userId);
            publishCoinsAwarded(userId, SELL_PRICES.get(CardTier.GOLD), TransactionReason.DUPLICATE_GOLD);
            return;
        }

        PlayerCard card = new PlayerCard();
        card.setUserId(userId);
        card.setPlayerId(player.externalId());
        card.setPlayerName(player.name());
        card.setTeamId(player.teamExternalId());
        card.setTeamName(player.teamName());
        card.setTier(CardTier.BRONZE);

        playerCardRepository.save(card);
        updateCollectionProgress(userId, card, true);
    }

    @Transactional
    public void upgradeCard(UUID userId, Integer playerId) {
        Optional<PlayerCard> existing = playerCardRepository
                .findByUserIdAndPlayerId(userId, playerId);

        CardTier currentTier = existing
                .map(PlayerCard::getTier)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (currentTier == CardTier.GOLD) {
            throw new IllegalStateException("Already at max tier");
        }

        List<PlayerCard> cardsToUpgrade = playerCardRepository
                .findByUserIdAndPlayerIdAndTier(userId, playerId, currentTier);

        if (cardsToUpgrade.size() < 2) {
            throw new IllegalStateException("Need 2 cards of same tier to upgrade");
        }

        playerCardRepository.deleteAll(cardsToUpgrade.subList(0, 2));

        PlayerCard upgraded = new PlayerCard();
        upgraded.setUserId(userId);
        upgraded.setPlayerId(playerId);
        upgraded.setPlayerName(cardsToUpgrade.getFirst().getPlayerName());
        upgraded.setTeamId(cardsToUpgrade.getFirst().getTeamId());
        upgraded.setTeamName(cardsToUpgrade.getFirst().getTeamName());
        upgraded.setTier(currentTier.next());

        playerCardRepository.save(upgraded);
        log.info("Upgraded player {} to {} for user {}", playerId, currentTier.next(), userId);
    }

    @Transactional
    public void sellCard(UUID cardId, UUID userId) {
        PlayerCard card = playerCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }

        int coins = SELL_PRICES.get(card.getTier());
        playerCardRepository.delete(card);
        publishCoinsAwarded(userId, coins, TransactionReason.CARD_SELL);
        updateCollectionProgress(userId, card, false);

        log.info("Sold {} card for {} coins for user {}", card.getTier(), coins, userId);
    }

    public List<PlayerCard> getCollection(UUID userId) {
        return playerCardRepository.findAllByUserId(userId);
    }

    public CollectionProgress getCollectionProgress(UUID userId) {
        return collectionProgressRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CollectionProgress p = new CollectionProgress();
                    p.setUserId(userId);
                    return p;
                });
    }

    private void updateCollectionProgress(UUID userId, PlayerCard card, boolean adding) {
        CollectionProgress progress = collectionProgressRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    CollectionProgress p = new CollectionProgress();
                    p.setUserId(userId);
                    return p;
                });

        int delta = adding ? 1 : -1;
        progress.setTotalCards(progress.getTotalCards() + delta);

        switch (card.getTier()) {
            case BRONZE -> progress.setBronzeCount(progress.getBronzeCount() + delta);
            case SILVER -> progress.setSilverCount(progress.getSilverCount() + delta);
            case GOLD -> progress.setGoldCount(progress.getGoldCount() + delta);
        }

        collectionProgressRepository.save(progress);
    }

    private void publishCoinsAwarded(UUID userId, Integer amount, TransactionReason reason) {
        kafkaTemplate.send(coinsAwardTopic, userId.toString(),
                new CoinsAwardedEvent(userId, amount, reason.name()));
    }
}