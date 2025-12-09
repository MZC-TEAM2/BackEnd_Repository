package com.mzc.backend.lms.domains.notification.aop.publisher;

import com.mzc.backend.lms.domains.notification.aop.event.NotificationEventType;
import com.mzc.backend.lms.domains.notification.entity.NotificationBatch;
import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.service.NotificationQueueService;
import com.mzc.backend.lms.domains.notification.repository.NotificationBatchRepository;
import com.mzc.backend.lms.domains.notification.repository.NotificationTypeRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 알림 이벤트 발행 구현체
 * NotificationQueueService를 통해 Redis 큐에 알림을 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisherImpl implements NotificationEventPublisher {

    private final NotificationQueueService queueService;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationBatchRepository notificationBatchRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    public void publish(NotificationEventType eventType, Long senderId, Long recipientId,
                        String title, String message) {
        try {
            Integer typeId = getTypeId(eventType);
            if (typeId == null) {
                log.warn("알림 타입을 찾을 수 없음: {}", eventType.getTypeCode());
                return;
            }

            NotificationMessage notificationMessage = NotificationMessage.builder()
                    .typeId(typeId)
                    .senderId(senderId)
                    .recipientId(recipientId)
                    .title(title)
                    .message(message)
                    .build();

            queueService.enqueue(notificationMessage);
            log.debug("알림 발행 완료: type={}, recipientId={}", eventType, recipientId);

        } catch (Exception e) {
            log.error("알림 발행 실패: type={}, recipientId={}, error={}",
                    eventType, recipientId, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void publishForCourse(NotificationEventType eventType, Long senderId, Long recipientId,
                                 Long courseId, String title, String message) {
        try {
            Integer typeId = getTypeId(eventType);
            if (typeId == null) {
                log.warn("알림 타입을 찾을 수 없음: {}", eventType.getTypeCode());
                return;
            }

            NotificationMessage notificationMessage = NotificationMessage.forCourse(
                    typeId, senderId, recipientId, courseId, title, message
            );

            queueService.enqueue(notificationMessage);
            log.debug("강의 알림 발행 완료: type={}, courseId={}, recipientId={}",
                    eventType, courseId, recipientId);

        } catch (Exception e) {
            log.error("강의 알림 발행 실패: type={}, courseId={}, error={}",
                    eventType, courseId, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void publishWithEntity(NotificationEventType eventType, Long senderId, Long recipientId,
                                  String relatedEntityType, Long relatedEntityId, Long courseId,
                                  String title, String message, String actionUrl) {
        try {
            Integer typeId = getTypeId(eventType);
            if (typeId == null) {
                log.warn("알림 타입을 찾을 수 없음: {}", eventType.getTypeCode());
                return;
            }

            NotificationMessage notificationMessage = NotificationMessage.withRelatedEntity(
                    typeId, senderId, recipientId, relatedEntityType, relatedEntityId,
                    courseId, title, message, actionUrl
            );

            queueService.enqueue(notificationMessage);
            log.debug("엔티티 알림 발행 완료: type={}, entityType={}, entityId={}",
                    eventType, relatedEntityType, relatedEntityId);

        } catch (Exception e) {
            log.error("엔티티 알림 발행 실패: type={}, entityType={}, error={}",
                    eventType, relatedEntityType, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void publishBatch(NotificationEventType eventType, Long senderId, List<Long> recipientIds,
                             Long courseId, String title, String message) {
        publishBatchWithEntity(eventType, senderId, recipientIds, null, null,
                courseId, title, message, null);
    }

    @Override
    @Transactional
    public void publishBatchWithEntity(NotificationEventType eventType, Long senderId, List<Long> recipientIds,
                                       String relatedEntityType, Long relatedEntityId, Long courseId,
                                       String title, String message, String actionUrl) {
        try {
            if (recipientIds == null || recipientIds.isEmpty()) {
                log.warn("배치 알림 수신자 목록이 비어있음: type={}", eventType);
                return;
            }

            Optional<NotificationType> typeOpt = notificationTypeRepository.findByTypeCode(eventType.getTypeCode());
            if (typeOpt.isEmpty()) {
                log.warn("알림 타입을 찾을 수 없음: {}", eventType.getTypeCode());
                return;
            }

            NotificationType notificationType = typeOpt.get();
            User sender = senderId != null ? userRepository.findById(senderId).orElse(null) : null;

            // 배치 레코드 생성
            NotificationBatch batch = NotificationBatch.create(
                    notificationType, sender, courseId, title, message, recipientIds.size()
            );
            batch = notificationBatchRepository.save(batch);

            // 배치 메시지 생성 및 큐에 추가
            BatchNotificationMessage batchMessage = BatchNotificationMessage.builder()
                    .batchId(batch.getId())
                    .typeId(notificationType.getId())
                    .senderId(senderId)
                    .recipientIds(recipientIds)
                    .courseId(courseId)
                    .relatedEntityType(relatedEntityType)
                    .relatedEntityId(relatedEntityId)
                    .title(title)
                    .message(message)
                    .actionUrl(actionUrl)
                    .build();

            queueService.enqueueBatch(batchMessage);
            log.info("배치 알림 발행 완료: type={}, batchId={}, recipientCount={}",
                    eventType, batch.getId(), recipientIds.size());

        } catch (Exception e) {
            log.error("배치 알림 발행 실패: type={}, error={}", eventType, e.getMessage(), e);
        }
    }

    /**
     * 이벤트 타입으로 알림 타입 ID 조회
     */
    private Integer getTypeId(NotificationEventType eventType) {
        return notificationTypeRepository.findByTypeCode(eventType.getTypeCode())
                .map(NotificationType::getId)
                .orElse(null);
    }
}
