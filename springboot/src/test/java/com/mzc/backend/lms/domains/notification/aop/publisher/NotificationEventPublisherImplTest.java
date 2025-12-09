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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationEventPublisherImpl 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationEventPublisherImpl 테스트")
class NotificationEventPublisherImplTest {

    @Mock
    private NotificationQueueService queueService;

    @Mock
    private NotificationTypeRepository notificationTypeRepository;

    @Mock
    private NotificationBatchRepository notificationBatchRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationEventPublisherImpl publisher;

    private NotificationType mockNotificationType;

    @BeforeEach
    void setUp() {
        publisher = new NotificationEventPublisherImpl(
                queueService,
                notificationTypeRepository,
                notificationBatchRepository,
                userRepository
        );

        mockNotificationType = mock(NotificationType.class);
        when(mockNotificationType.getId()).thenReturn(1);
        when(mockNotificationType.getTypeCode()).thenReturn("ASSIGNMENT_CREATED");
    }

    @Test
    @DisplayName("단일 알림 발행 성공")
    void publishSuccess() {
        // given
        when(notificationTypeRepository.findByTypeCode("ASSIGNMENT_CREATED"))
                .thenReturn(Optional.of(mockNotificationType));

        // when
        publisher.publish(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, 200L,
                "테스트 제목", "테스트 메시지"
        );

        // then
        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(queueService).enqueue(captor.capture());

        NotificationMessage message = captor.getValue();
        assertThat(message.getTypeId()).isEqualTo(1);
        assertThat(message.getSenderId()).isEqualTo(100L);
        assertThat(message.getRecipientId()).isEqualTo(200L);
        assertThat(message.getTitle()).isEqualTo("테스트 제목");
        assertThat(message.getMessage()).isEqualTo("테스트 메시지");
    }

    @Test
    @DisplayName("알림 타입이 없으면 발행하지 않음")
    void publishTypeNotFound() {
        // given
        when(notificationTypeRepository.findByTypeCode("ASSIGNMENT_CREATED"))
                .thenReturn(Optional.empty());

        // when
        publisher.publish(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, 200L,
                "테스트 제목", "테스트 메시지"
        );

        // then
        verify(queueService, never()).enqueue(any());
    }

    @Test
    @DisplayName("강의 관련 알림 발행 성공")
    void publishForCourseSuccess() {
        // given
        when(notificationTypeRepository.findByTypeCode("ASSIGNMENT_CREATED"))
                .thenReturn(Optional.of(mockNotificationType));

        // when
        publisher.publishForCourse(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, 200L, 10L,
                "강의 알림", "새 과제가 등록되었습니다."
        );

        // then
        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(queueService).enqueue(captor.capture());

        NotificationMessage message = captor.getValue();
        assertThat(message.getCourseId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("엔티티 정보 포함 알림 발행 성공")
    void publishWithEntitySuccess() {
        // given
        when(notificationTypeRepository.findByTypeCode("ASSIGNMENT_CREATED"))
                .thenReturn(Optional.of(mockNotificationType));

        // when
        publisher.publishWithEntity(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, 200L,
                "ASSIGNMENT", 50L, 10L,
                "과제 알림", "새 과제가 등록되었습니다.",
                "/courses/10/assignments/50"
        );

        // then
        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(queueService).enqueue(captor.capture());

        NotificationMessage message = captor.getValue();
        assertThat(message.getRelatedEntityType()).isEqualTo("ASSIGNMENT");
        assertThat(message.getRelatedEntityId()).isEqualTo(50L);
        assertThat(message.getActionUrl()).isEqualTo("/courses/10/assignments/50");
    }

    @Test
    @DisplayName("배치 알림 발행 성공")
    void publishBatchSuccess() {
        // given
        List<Long> recipientIds = Arrays.asList(200L, 300L, 400L);
        User mockSender = mock(User.class);
        NotificationBatch mockBatch = mock(NotificationBatch.class);

        when(notificationTypeRepository.findByTypeCode("ASSIGNMENT_CREATED"))
                .thenReturn(Optional.of(mockNotificationType));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockSender));
        when(notificationBatchRepository.save(any(NotificationBatch.class))).thenReturn(mockBatch);
        when(mockBatch.getId()).thenReturn(1L);

        // when
        publisher.publishBatch(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, recipientIds, 10L,
                "배치 알림", "새 과제가 등록되었습니다."
        );

        // then
        verify(notificationBatchRepository).save(any(NotificationBatch.class));

        ArgumentCaptor<BatchNotificationMessage> captor = ArgumentCaptor.forClass(BatchNotificationMessage.class);
        verify(queueService).enqueueBatch(captor.capture());

        BatchNotificationMessage message = captor.getValue();
        assertThat(message.getBatchId()).isEqualTo(1L);
        assertThat(message.getRecipientIds()).hasSize(3);
    }

    @Test
    @DisplayName("빈 수신자 목록으로 배치 발행 시 발행하지 않음")
    void publishBatchWithEmptyRecipients() {
        // when
        publisher.publishBatch(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, List.of(), 10L,
                "배치 알림", "메시지"
        );

        // then
        verify(queueService, never()).enqueueBatch(any());
    }

    @Test
    @DisplayName("null 수신자 목록으로 배치 발행 시 발행하지 않음")
    void publishBatchWithNullRecipients() {
        // when
        publisher.publishBatch(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, null, 10L,
                "배치 알림", "메시지"
        );

        // then
        verify(queueService, never()).enqueueBatch(any());
    }

    @Test
    @DisplayName("엔티티 정보 포함 배치 알림 발행 성공")
    void publishBatchWithEntitySuccess() {
        // given
        List<Long> recipientIds = Arrays.asList(200L, 300L);
        User mockSender = mock(User.class);
        NotificationBatch mockBatch = mock(NotificationBatch.class);

        when(notificationTypeRepository.findByTypeCode("ASSIGNMENT_CREATED"))
                .thenReturn(Optional.of(mockNotificationType));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockSender));
        when(notificationBatchRepository.save(any(NotificationBatch.class))).thenReturn(mockBatch);
        when(mockBatch.getId()).thenReturn(1L);

        // when
        publisher.publishBatchWithEntity(
                NotificationEventType.ASSIGNMENT_CREATED,
                100L, recipientIds,
                "ASSIGNMENT", 50L, 10L,
                "과제 알림", "새 과제가 등록되었습니다.",
                "/courses/10/assignments/50"
        );

        // then
        ArgumentCaptor<BatchNotificationMessage> captor = ArgumentCaptor.forClass(BatchNotificationMessage.class);
        verify(queueService).enqueueBatch(captor.capture());

        BatchNotificationMessage message = captor.getValue();
        assertThat(message.getRelatedEntityType()).isEqualTo("ASSIGNMENT");
        assertThat(message.getRelatedEntityId()).isEqualTo(50L);
        assertThat(message.getActionUrl()).isEqualTo("/courses/10/assignments/50");
    }

    @Test
    @DisplayName("발신자가 null인 경우 배치 발행 성공")
    void publishBatchWithNullSender() {
        // given
        List<Long> recipientIds = Arrays.asList(200L, 300L);
        NotificationBatch mockBatch = mock(NotificationBatch.class);

        when(notificationTypeRepository.findByTypeCode("SYSTEM_NOTICE_CREATED"))
                .thenReturn(Optional.of(mockNotificationType));
        when(notificationBatchRepository.save(any(NotificationBatch.class))).thenReturn(mockBatch);
        when(mockBatch.getId()).thenReturn(1L);

        // when
        publisher.publishBatch(
                NotificationEventType.SYSTEM_NOTICE_CREATED,
                null, recipientIds, null,
                "시스템 공지", "시스템 점검 안내"
        );

        // then
        verify(userRepository, never()).findById(any());
        verify(queueService).enqueueBatch(any(BatchNotificationMessage.class));
    }
}
