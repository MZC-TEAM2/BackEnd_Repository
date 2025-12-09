package com.mzc.backend.lms.domains.notification.service;

import com.mzc.backend.lms.domains.notification.dto.NotificationCursorResponseDto;
import com.mzc.backend.lms.domains.notification.dto.NotificationResponseDto;
import com.mzc.backend.lms.domains.notification.entity.Notification;
import com.mzc.backend.lms.domains.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 서비스 구현체
 * 커서 기반 페이징으로 대용량 데이터 효율적 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationCursorResponseDto getNotifications(Long userId, Long cursor, int size) {
        // hasNext 확인을 위해 size + 1 조회
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Notification> notifications;
        if (cursor == null) {
            // 최초 요청
            notifications = notificationRepository.findByRecipientIdOrderByIdDesc(userId, pageable);
        } else {
            // 다음 페이지 요청
            notifications = notificationRepository.findByRecipientIdAndIdLessThanOrderByIdDesc(
                    userId, cursor, pageable);
        }

        List<NotificationResponseDto> dtos = notifications.stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());

        // 읽지 않은 개수는 첫 요청 시에만 조회 (성능 최적화)
        Long unreadCount = cursor == null
                ? notificationRepository.countUnreadByRecipientId(userId)
                : null;

        return NotificationCursorResponseDto.of(dtos, size, unreadCount);
    }

    @Override
    public NotificationCursorResponseDto getUnreadNotifications(Long userId, Long cursor, int size) {
        // hasNext 확인을 위해 size + 1 조회
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Notification> notifications;
        if (cursor == null) {
            // 최초 요청
            notifications = notificationRepository.findUnreadByRecipientIdOrderByIdDesc(userId, pageable);
        } else {
            // 다음 페이지 요청
            notifications = notificationRepository.findUnreadByRecipientIdAndIdLessThanOrderByIdDesc(
                    userId, cursor, pageable);
        }

        List<NotificationResponseDto> dtos = notifications.stream()
                .map(NotificationResponseDto::from)
                .collect(Collectors.toList());

        // 읽지 않은 개수는 첫 요청 시에만 조회
        Long unreadCount = cursor == null
                ? notificationRepository.countUnreadByRecipientId(userId)
                : null;

        return NotificationCursorResponseDto.of(dtos, size, unreadCount);
    }

    @Override
    @Transactional
    public NotificationResponseDto getNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        validateRecipient(notification, userId);

        // 상세 조회 시 자동 읽음 처리
        if (notification.isUnread()) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }

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

    @Override
    @Transactional
    public int deleteReadNotifications(Long userId) {
        int deletedCount = notificationRepository.deleteReadByRecipientId(userId);

        log.info("읽은 알림 삭제: userId={}, count={}", userId, deletedCount);

        return deletedCount;
    }

    @Override
    @Transactional
    public int deleteAllNotifications(Long userId) {
        int deletedCount = notificationRepository.deleteAllByRecipientId(userId);

        log.info("모든 알림 삭제: userId={}, count={}", userId, deletedCount);

        return deletedCount;
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
