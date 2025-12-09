package com.mzc.backend.lms.domains.notification.service;

import com.mzc.backend.lms.domains.notification.dto.NotificationCursorResponseDto;
import com.mzc.backend.lms.domains.notification.dto.NotificationResponseDto;

/**
 * 알림 서비스 인터페이스
 */
public interface NotificationService {

    /**
     * 커서 기반 알림 목록 조회
     *
     * @param userId 사용자 ID
     * @param cursor 커서 (null이면 처음부터)
     * @param size 페이지 크기
     * @return 커서 기반 알림 목록 응답
     */
    NotificationCursorResponseDto getNotifications(Long userId, Long cursor, int size);

    /**
     * 커서 기반 읽지 않은 알림 목록 조회
     *
     * @param userId 사용자 ID
     * @param cursor 커서 (null이면 처음부터)
     * @param size 페이지 크기
     * @return 커서 기반 알림 목록 응답
     */
    NotificationCursorResponseDto getUnreadNotifications(Long userId, Long cursor, int size);

    /**
     * 알림 상세 조회
     *
     * @param userId 사용자 ID
     * @param notificationId 알림 ID
     * @return 알림 응답
     */
    NotificationResponseDto getNotification(Long userId, Long notificationId);

    /**
     * 알림 읽음 처리
     *
     * @param userId 사용자 ID
     * @param notificationId 알림 ID
     * @return 업데이트된 알림 응답
     */
    NotificationResponseDto markAsRead(Long userId, Long notificationId);

    /**
     * 모든 알림 읽음 처리
     *
     * @param userId 사용자 ID
     * @return 읽음 처리된 알림 개수
     */
    int markAllAsRead(Long userId);

    /**
     * 읽지 않은 알림 개수 조회
     *
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    long getUnreadCount(Long userId);

    /**
     * 알림 삭제
     *
     * @param userId 사용자 ID
     * @param notificationId 알림 ID
     */
    void deleteNotification(Long userId, Long notificationId);

    /**
     * 읽은 알림 일괄 삭제
     *
     * @param userId 사용자 ID
     * @return 삭제된 알림 개수
     */
    int deleteReadNotifications(Long userId);

    /**
     * 모든 알림 삭제
     *
     * @param userId 사용자 ID
     * @return 삭제된 알림 개수
     */
    int deleteAllNotifications(Long userId);
}
