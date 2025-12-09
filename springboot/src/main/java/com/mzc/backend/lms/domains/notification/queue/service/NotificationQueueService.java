package com.mzc.backend.lms.domains.notification.queue.service;

import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;

import java.util.Optional;

/**
 * 알림 큐 서비스 인터페이스
 */
public interface NotificationQueueService {

    /**
     * 단일 알림 메시지를 큐에 추가
     */
    void enqueue(NotificationMessage message);

    /**
     * 배치 알림 메시지를 큐에 추가
     */
    void enqueueBatch(BatchNotificationMessage message);

    /**
     * 큐에서 알림 메시지를 가져옴 (blocking)
     */
    Optional<NotificationMessage> dequeue(long timeoutSeconds);

    /**
     * 큐에서 배치 알림 메시지를 가져옴 (blocking)
     */
    Optional<BatchNotificationMessage> dequeueBatch(long timeoutSeconds);

    /**
     * 큐에 대기 중인 알림 개수 조회
     */
    long getQueueSize();

    /**
     * 배치 큐에 대기 중인 알림 개수 조회
     */
    long getBatchQueueSize();

    /**
     * 큐 비우기 (테스트용)
     */
    void clearQueue();
}
