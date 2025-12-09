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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * NotificationProcessor 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationProcessor 테스트")
class NotificationProcessorTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTypeRepository notificationTypeRepository;

    @Mock
    private NotificationBatchRepository notificationBatchRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationProcessorImpl processor;

    private NotificationType mockType;
    private User mockSender;
    private User mockRecipient;

    @BeforeEach
    void setUp() {
        processor = new NotificationProcessorImpl(
                notificationRepository,
                notificationTypeRepository,
                notificationBatchRepository,
                userRepository
        );

        mockType = mock(NotificationType.class);
        mockSender = mock(User.class);
        mockRecipient = mock(User.class);

        when(mockSender.getId()).thenReturn(100L);
        when(mockRecipient.getId()).thenReturn(200L);
    }

    @Test
    @DisplayName("단일 알림 메시지 처리 성공")
    void processSuccess() {
        // given
        NotificationMessage message = NotificationMessage.of(1, 100L, 200L, "테스트 메시지");

        when(notificationTypeRepository.findById(1)).thenReturn(Optional.of(mockType));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockSender));
        when(userRepository.findById(200L)).thenReturn(Optional.of(mockRecipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        // when
        processor.process(message);

        // then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 타입을 찾을 수 없는 경우 예외 발생")
    void processTypeNotFound() {
        // given
        NotificationMessage message = NotificationMessage.of(999, 100L, 200L, "테스트");
        when(notificationTypeRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> processor.process(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("알림 타입을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("수신자를 찾을 수 없는 경우 예외 발생")
    void processRecipientNotFound() {
        // given
        NotificationMessage message = NotificationMessage.of(1, 100L, 999L, "테스트");
        when(notificationTypeRepository.findById(1)).thenReturn(Optional.of(mockType));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockSender));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> processor.process(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수신자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("발신자가 null인 경우에도 처리 성공")
    void processWithNullSender() {
        // given
        NotificationMessage message = NotificationMessage.of(1, null, 200L, "테스트 메시지");

        when(notificationTypeRepository.findById(1)).thenReturn(Optional.of(mockType));
        when(userRepository.findById(200L)).thenReturn(Optional.of(mockRecipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        // when
        processor.process(message);

        // then
        verify(notificationRepository).save(any(Notification.class));
        verify(userRepository, never()).findById(isNull());
    }

    @Test
    @DisplayName("배치 알림 메시지 처리 성공")
    void processBatchSuccess() {
        // given
        List<Long> recipientIds = Arrays.asList(200L, 300L, 400L);
        BatchNotificationMessage message = BatchNotificationMessage.forCourse(
                1L, 1, 100L, recipientIds, 10L, "알림", "메시지"
        );

        NotificationBatch mockBatch = mock(NotificationBatch.class);

        when(notificationBatchRepository.findById(1L)).thenReturn(Optional.of(mockBatch));
        when(notificationTypeRepository.findById(1)).thenReturn(Optional.of(mockType));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockSender));
        when(userRepository.findById(200L)).thenReturn(Optional.of(mockRecipient));
        when(userRepository.findById(300L)).thenReturn(Optional.of(mock(User.class)));
        when(userRepository.findById(400L)).thenReturn(Optional.of(mock(User.class)));

        // when
        processor.processBatch(message);

        // then
        verify(mockBatch).startProcessing();
        verify(mockBatch).complete();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("배치 처리 중 일부 수신자를 찾을 수 없는 경우")
    void processBatchWithSomeInvalidRecipients() {
        // given
        List<Long> recipientIds = Arrays.asList(200L, 999L, 400L);
        BatchNotificationMessage message = BatchNotificationMessage.forCourse(
                1L, 1, 100L, recipientIds, 10L, "알림", "메시지"
        );

        NotificationBatch mockBatch = mock(NotificationBatch.class);

        when(notificationBatchRepository.findById(1L)).thenReturn(Optional.of(mockBatch));
        when(notificationTypeRepository.findById(1)).thenReturn(Optional.of(mockType));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockSender));
        when(userRepository.findById(200L)).thenReturn(Optional.of(mockRecipient));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(userRepository.findById(400L)).thenReturn(Optional.of(mock(User.class)));

        // when
        processor.processBatch(message);

        // then
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2); // 999L은 제외
        verify(mockBatch).complete();
    }

    @Test
    @DisplayName("배치 처리 실패 시 상태 업데이트")
    void processBatchFailure() {
        // given
        List<Long> recipientIds = Arrays.asList(200L);
        BatchNotificationMessage message = BatchNotificationMessage.forCourse(
                1L, 1, 100L, recipientIds, 10L, "알림", "메시지"
        );

        NotificationBatch mockBatch = mock(NotificationBatch.class);

        when(notificationBatchRepository.findById(1L)).thenReturn(Optional.of(mockBatch));
        when(notificationTypeRepository.findById(1)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> processor.processBatch(message))
                .isInstanceOf(IllegalArgumentException.class);

        verify(mockBatch).startProcessing();
        verify(mockBatch).fail(anyString());
        verify(mockBatch, never()).complete();
    }

    @Test
    @DisplayName("batchId가 null인 경우에도 처리")
    void processBatchWithNullBatchId() {
        // given
        List<Long> recipientIds = Arrays.asList(200L);
        BatchNotificationMessage message = BatchNotificationMessage.builder()
                .batchId(null)
                .typeId(1)
                .senderId(100L)
                .recipientIds(recipientIds)
                .message("메시지")
                .build();

        when(notificationTypeRepository.findById(1)).thenReturn(Optional.of(mockType));
        when(userRepository.findById(100L)).thenReturn(Optional.of(mockSender));
        when(userRepository.findById(200L)).thenReturn(Optional.of(mockRecipient));

        // when
        processor.processBatch(message);

        // then
        verify(notificationBatchRepository, never()).findById(anyLong());
        verify(notificationRepository).saveAll(anyList());
    }
}
