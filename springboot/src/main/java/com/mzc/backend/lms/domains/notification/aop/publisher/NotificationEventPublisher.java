package com.mzc.backend.lms.domains.notification.aop.publisher;

import com.mzc.backend.lms.domains.notification.aop.event.NotificationEventType;

import java.util.List;

/**
 * 알림 이벤트 발행 인터페이스
 */
public interface NotificationEventPublisher {

    /**
     * 단일 수신자에게 알림 발행
     *
     * @param eventType 알림 이벤트 타입
     * @param senderId 발신자 ID (null 가능)
     * @param recipientId 수신자 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     */
    void publish(NotificationEventType eventType, Long senderId, Long recipientId,
                 String title, String message);

    /**
     * 단일 수신자에게 강의 관련 알림 발행
     *
     * @param eventType 알림 이벤트 타입
     * @param senderId 발신자 ID
     * @param recipientId 수신자 ID
     * @param courseId 강의 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     */
    void publishForCourse(NotificationEventType eventType, Long senderId, Long recipientId,
                          Long courseId, String title, String message);

    /**
     * 단일 수신자에게 관련 엔티티 정보 포함 알림 발행
     *
     * @param eventType 알림 이벤트 타입
     * @param senderId 발신자 ID
     * @param recipientId 수신자 ID
     * @param relatedEntityType 관련 엔티티 타입
     * @param relatedEntityId 관련 엔티티 ID
     * @param courseId 강의 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param actionUrl 액션 URL
     */
    void publishWithEntity(NotificationEventType eventType, Long senderId, Long recipientId,
                           String relatedEntityType, Long relatedEntityId, Long courseId,
                           String title, String message, String actionUrl);

    /**
     * 다중 수신자에게 배치 알림 발행
     *
     * @param eventType 알림 이벤트 타입
     * @param senderId 발신자 ID
     * @param recipientIds 수신자 ID 목록
     * @param courseId 강의 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     */
    void publishBatch(NotificationEventType eventType, Long senderId, List<Long> recipientIds,
                      Long courseId, String title, String message);

    /**
     * 다중 수신자에게 관련 엔티티 정보 포함 배치 알림 발행
     *
     * @param eventType 알림 이벤트 타입
     * @param senderId 발신자 ID
     * @param recipientIds 수신자 ID 목록
     * @param relatedEntityType 관련 엔티티 타입
     * @param relatedEntityId 관련 엔티티 ID
     * @param courseId 강의 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param actionUrl 액션 URL
     */
    void publishBatchWithEntity(NotificationEventType eventType, Long senderId, List<Long> recipientIds,
                                String relatedEntityType, Long relatedEntityId, Long courseId,
                                String title, String message, String actionUrl);
}
