package com.kickoff.notification_service.service;

import com.kickoff.notification_service.dto.NotificationResponse;
import com.kickoff.notification_service.event.CoinsAwardedEvent;
import com.kickoff.notification_service.event.TradeStatusChangedEvent;
import com.kickoff.notification_service.mapper.NotificationMapper;
import com.kickoff.notification_service.model.Notification;
import com.kickoff.notification_service.model.NotificationType;
import com.kickoff.notification_service.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private static final String UNREAD_COUNT_KEY = "notifications:unread:";

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper,
                               RedisTemplate<String, String> redisTemplate) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void handleTradeStatusChanged(TradeStatusChangedEvent event) {
        NotificationType type = switch (event.newStatus()) {
            case "ACCEPTED" -> NotificationType.TRADE_ACCEPTED;
            case "REJECTED" -> NotificationType.TRADE_REJECTED;
            case "EXPIRED" -> NotificationType.TRADE_EXPIRED;
            default -> {
                log.warn("Unknown trade status: {}", event.newStatus());
                yield null;
            }
        };

        if (type == null) return;

        String message = switch (type) {
            case TRADE_ACCEPTED -> "Your trade offer was accepted!";
            case TRADE_REJECTED -> "Your trade offer was rejected.";
            case TRADE_EXPIRED -> "Your trade offer has expired.";
            default -> "";
        };

        createNotification(event.initiatorId(), type, message);

        if (type == NotificationType.TRADE_ACCEPTED ||
                type == NotificationType.TRADE_REJECTED) {
            createNotification(event.receiverId(), type, message);
        }
    }

    @Transactional
    public void handleCoinsAwarded(CoinsAwardedEvent event) {
        String message = String.format("You earned %d coins — %s",
                event.amount(), formatReason(event.reason()));
        createNotification(event.userId(), NotificationType.COINS_AWARDED, message);
    }

    public List<NotificationResponse> getNotifications(UUID userId) {
        return notificationMapper.toResponse(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    public Long getUnreadCount(UUID userId) {
        String cacheKey = UNREAD_COUNT_KEY + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Long.parseLong(cached);
        }

        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(count));
        return count;
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
        redisTemplate.delete(UNREAD_COUNT_KEY + userId);
        log.info("Marked all notifications as read for user {}", userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
            redisTemplate.delete(UNREAD_COUNT_KEY + n.getUserId());
        });
    }

    private void createNotification(UUID userId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        notificationRepository.save(notification);
        redisTemplate.delete(UNREAD_COUNT_KEY + userId);
        log.info("Created {} notification for user {}", type, userId);
    }

    private String formatReason(String reason) {
        return switch (reason) {
            case "CORRECT_SCORE" -> "correct score prediction";
            case "CORRECT_RESULT" -> "correct result prediction";
            case "CARD_SELL" -> "selling a card";
            case "DUPLICATE_GOLD" -> "duplicate gold card";
            default -> reason.toLowerCase().replace("_", " ");
        };
    }
}