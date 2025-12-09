package com.mzc.backend.lms.domains.notification.queue.processor;

import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;

/**
 * 알림 처리 인터페이스
 */
public interface NotificationProcessor {

    /**
     * 단일 알림 메시지 처리
     */
    void process(NotificationMessage message);

    /**
     * 배치 알림 메시지 처리
     */
    void processBatch(BatchNotificationMessage message);
}
