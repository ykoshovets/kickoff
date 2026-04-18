package com.kickoff.trade_service.service;

import com.kickoff.trade_service.client.CardServiceClient;
import com.kickoff.trade_service.dto.CardDto;
import com.kickoff.trade_service.dto.TradeOfferRequest;
import com.kickoff.trade_service.dto.TradeResponse;
import com.kickoff.trade_service.event.TradeStatusChangedEvent;
import com.kickoff.trade_service.mapper.TradeMapper;
import com.kickoff.trade_service.model.Trade;
import com.kickoff.trade_service.model.TradeStatus;
import com.kickoff.trade_service.repository.TradeRepository;
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
public class TradeService {

    private final TradeRepository tradeRepository;
    private final CardServiceClient cardServiceClient;
    private final TradeMapper tradeMapper;
    private final KafkaTemplate<String, TradeStatusChangedEvent> kafkaTemplate;

    @Value("${kafka.topics.trade-status-changed}")
    private String tradeStatusChangedTopic;

    public TradeService(TradeRepository tradeRepository,
                        CardServiceClient cardServiceClient,
                        TradeMapper tradeMapper,
                        KafkaTemplate<String, TradeStatusChangedEvent> kafkaTemplate) {
        this.tradeRepository = tradeRepository;
        this.cardServiceClient = cardServiceClient;
        this.tradeMapper = tradeMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public TradeResponse createOffer(TradeOfferRequest request, UUID initiatorId) {
        validateOffer(request, initiatorId);

        Trade trade = new Trade();
        trade.setInitiatorId(initiatorId);
        trade.setReceiverId(request.receiverId());
        trade.setOfferedCardId(request.offeredCardId());
        trade.setRequestedCardId(request.requestedCardId());

        tradeRepository.save(trade);
        log.info("Trade offer created: {} offers card {} for card {}",
                initiatorId, request.offeredCardId(), request.requestedCardId());

        publishStatusChanged(trade);
        return tradeMapper.toResponse(trade);
    }

    @Transactional
    public TradeResponse acceptTrade(UUID tradeId, UUID userId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        if (!trade.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("Only receiver can accept trade");
        }

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new IllegalStateException("Trade is no longer pending");
        }

        validateCardOwnership(trade);

        cardServiceClient.transferCard(trade.getOfferedCardId(), trade.getReceiverId());
        cardServiceClient.transferCard(trade.getRequestedCardId(), trade.getInitiatorId());

        trade.setStatus(TradeStatus.ACCEPTED);
        trade.setResolvedAt(OffsetDateTime.now());
        tradeRepository.save(trade);

        log.info("Trade {} accepted — cards swapped", tradeId);
        publishStatusChanged(trade);
        return tradeMapper.toResponse(trade);
    }

    @Transactional
    public TradeResponse rejectTrade(UUID tradeId, UUID userId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        if (!trade.getReceiverId().equals(userId)) {
            throw new IllegalArgumentException("Only receiver can reject trade");
        }

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new IllegalStateException("Trade is no longer pending");
        }

        trade.setStatus(TradeStatus.REJECTED);
        trade.setResolvedAt(OffsetDateTime.now());
        tradeRepository.save(trade);

        log.info("Trade {} rejected", tradeId);
        publishStatusChanged(trade);
        return tradeMapper.toResponse(trade);
    }

    public List<TradeResponse> getIncomingTrades(UUID userId) {
        return tradeMapper.toResponse(
                tradeRepository.findByReceiverIdAndStatus(userId, TradeStatus.PENDING));
    }

    public List<TradeResponse> getOutgoingTrades(UUID userId) {
        return tradeMapper.toResponse(
                tradeRepository.findByInitiatorIdAndStatus(userId, TradeStatus.PENDING));
    }

    @Transactional
    public void expirePendingTrades() {
        List<Trade> expired = tradeRepository.findExpiredTrades(OffsetDateTime.now());

        expired.forEach(trade -> {
            trade.setStatus(TradeStatus.EXPIRED);
            trade.setResolvedAt(OffsetDateTime.now());
            tradeRepository.save(trade);
            publishStatusChanged(trade);
            log.info("Trade {} expired", trade.getId());
        });

        if (!expired.isEmpty()) {
            log.info("Expired {} pending trades", expired.size());
        }
    }

    private void validateOffer(TradeOfferRequest request, UUID initiatorId) {
        CardDto offeredCard = cardServiceClient.getCard(request.offeredCardId());
        if (offeredCard == null || !offeredCard.userId().equals(initiatorId)) {
            throw new IllegalArgumentException("Offered card does not belong to initiator");
        }

        CardDto requestedCard = cardServiceClient.getCard(request.requestedCardId());
        if (requestedCard == null || !requestedCard.userId().equals(request.receiverId())) {
            throw new IllegalArgumentException("Requested card does not belong to receiver");
        }

        boolean alreadyPending = tradeRepository.existsByInitiatorIdAndOfferedCardIdAndStatus(
                initiatorId, request.offeredCardId(), TradeStatus.PENDING);
        if (alreadyPending) {
            throw new IllegalStateException("Card already has a pending trade offer");
        }
    }

    private void validateCardOwnership(Trade trade) {
        CardDto offeredCard = cardServiceClient.getCard(trade.getOfferedCardId());
        if (offeredCard == null || !offeredCard.userId().equals(trade.getInitiatorId())) {
            throw new IllegalStateException("Offered card no longer belongs to initiator");
        }

        CardDto requestedCard = cardServiceClient.getCard(trade.getRequestedCardId());
        if (requestedCard == null || !requestedCard.userId().equals(trade.getReceiverId())) {
            throw new IllegalStateException("Requested card no longer belongs to receiver");
        }
    }

    private void publishStatusChanged(Trade trade) {
        TradeStatusChangedEvent event = new TradeStatusChangedEvent(
                trade.getId(),
                trade.getInitiatorId(),
                trade.getReceiverId(),
                trade.getOfferedCardId(),
                trade.getRequestedCardId(),
                trade.getStatus().name()
        );
        kafkaTemplate.send(tradeStatusChangedTopic, trade.getId().toString(), event);
    }
}