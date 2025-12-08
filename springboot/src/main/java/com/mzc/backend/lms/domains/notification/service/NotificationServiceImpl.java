package com.mzc.backend.lms.domains.notification.service;

import com.mzc.backend.lms.domains.notification.dto.NotificationListResponseDto;
import com.mzc.backend.lms.domains.notification.dto.NotificationResponseDto;
import com.mzc.backend.lms.domains.notification.entity.Notification;
import com.mzc.backend.lms.domains.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationListResponseDto getNotifications(Long userId, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable);

        Page<NotificationResponseDto> dtoPage = notificationPage.map(NotificationResponseDto::from);
        long unreadCount = notificationRepository.countUnreadByRecipientId(userId);

        return NotificationListResponseDto.from(dtoPage, unreadCount);
    }

    @Override
    public NotificationListResponseDto getUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notificationPage = notificationRepository
                .findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);

        Page<NotificationResponseDto> dtoPage = notificationPage.map(NotificationResponseDto::from);
        long unreadCount = notificationRepository.countUnreadByRecipientId(userId);

        return NotificationListResponseDto.from(dtoPage, unreadCount);
    }

    @Override
    public NotificationResponseDto getNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        validateRecipient(notification, userId);

        return NotificationResponseDto.from(notification);
    }

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        validateRecipient(notification, userId);

        notification.markAsRead();
        notificationRepository.save(notification);

        log.debug("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);

        return NotificationResponseDto.from(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        int updatedCount = notificationRepository.markAllAsReadByRecipientId(userId);

        log.info("모든 알림 읽음 처리: userId={}, count={}", userId, updatedCount);

        return updatedCount;
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByRecipientId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        validateRecipient(notification, userId);

        notificationRepository.delete(notification);

        log.info("알림 삭제: notificationId={}, userId={}", notificationId, userId);
    }

    /**
     * 알림 수신자 검증
     */
    private void validateRecipient(Notification notification, Long userId) {
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 알림에 대한 권한이 없습니다.");
        }
    }
}
