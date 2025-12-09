package com.mzc.backend.lms.domains.notification.queue.processor;

import com.mzc.backend.lms.domains.notification.entity.Notification;
import com.mzc.backend.lms.domains.notification.entity.NotificationBatch;
import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;
import com.mzc.backend.lms.domains.notification.repository.NotificationBatchRepository;
import com.mzc.backend.lms.domains.notification.repository.NotificationRepository;
import com.mzc.backend.lms.domains.notification.repository.NotificationTypeRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 알림 처리 구현체
 * 큐에서 받은 메시지를 실제 알림 엔티티로 변환하여 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessorImpl implements NotificationProcessor {

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationBatchRepository notificationBatchRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void process(NotificationMessage message) {
        try {
            NotificationType type = notificationTypeRepository.findById(message.getTypeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "알림 타입을 찾을 수 없습니다: " + message.getTypeId()));

            User sender = message.getSenderId() != null ?
                    userRepository.findById(message.getSenderId()).orElse(null) : null;

            User recipient = userRepository.findById(message.getRecipientId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "수신자를 찾을 수 없습니다: " + message.getRecipientId()));

            Notification notification = Notification.createWithRelatedEntity(
                    type, sender, recipient,
                    message.getTitle(), message.getMessage(),
                    message.getRelatedEntityType(), message.getRelatedEntityId(),
                    message.getCourseId(), message.getActionUrl()
            );

            notificationRepository.save(notification);
            log.debug("알림 저장 완료: recipientId={}", message.getRecipientId());

        } catch (Exception e) {
            log.error("알림 처리 실패: recipientId={}, error={}",
                    message.getRecipientId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void processBatch(BatchNotificationMessage message) {
        NotificationBatch batch = null;

        try {
            // 배치 상태 업데이트: 처리 중
            if (message.getBatchId() != null) {
                batch = notificationBatchRepository.findById(message.getBatchId()).orElse(null);
                if (batch != null) {
                    batch.startProcessing();
                    notificationBatchRepository.save(batch);
                }
            }

            NotificationType type = notificationTypeRepository.findById(message.getTypeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "알림 타입을 찾을 수 없습니다: " + message.getTypeId()));

            User sender = message.getSenderId() != null ?
                    userRepository.findById(message.getSenderId()).orElse(null) : null;

            List<Notification> notifications = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Long recipientId : message.getRecipientIds()) {
                try {
                    User recipient = userRepository.findById(recipientId).orElse(null);
                    if (recipient == null) {
                        log.warn("수신자를 찾을 수 없습니다: {}", recipientId);
                        failCount++;
                        continue;
                    }

                    Notification notification = Notification.createWithRelatedEntity(
                            type, sender, recipient,
                            message.getTitle(), message.getMessage(),
                            message.getRelatedEntityType(), message.getRelatedEntityId(),
                            message.getCourseId(), message.getActionUrl()
                    );
                    notifications.add(notification);
                    successCount++;

                } catch (Exception e) {
                    log.error("개별 알림 생성 실패: recipientId={}", recipientId, e);
                    failCount++;
                }
            }

            // 일괄 저장
            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
            }

            // 배치 상태 업데이트: 완료
            if (batch != null) {
                batch.complete();
                notificationBatchRepository.save(batch);
            }

            log.info("배치 알림 처리 완료: batchId={}, success={}, fail={}",
                    message.getBatchId(), successCount, failCount);

        } catch (Exception e) {
            // 배치 상태 업데이트: 실패
            if (batch != null) {
                batch.fail(e.getMessage());
                notificationBatchRepository.save(batch);
            }
            log.error("배치 알림 처리 실패: batchId={}, error={}",
                    message.getBatchId(), e.getMessage(), e);
            throw e;
        }
    }
}
