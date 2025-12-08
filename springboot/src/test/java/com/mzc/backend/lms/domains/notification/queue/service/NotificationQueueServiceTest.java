package com.mzc.backend.lms.domains.notification.queue.service;

import com.mzc.backend.lms.domains.notification.queue.config.NotificationRedisConfig;
import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationQueueService 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationQueueService 테스트")
class NotificationQueueServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    private NotificationQueueServiceImpl queueService;

    @BeforeEach
    void setUp() {
        queueService = new NotificationQueueServiceImpl(redisTemplate);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    @DisplayName("단일 알림 메시지 큐 추가")
    void enqueue() {
        // given
        NotificationMessage message = NotificationMessage.of(1, 100L, 200L, "테스트");
        when(listOperations.leftPush(anyString(), any())).thenReturn(1L);

        // when
        queueService.enqueue(message);

        // then
        verify(listOperations).leftPush(
                eq(NotificationRedisConfig.NOTIFICATION_QUEUE_KEY),
                eq(message)
        );
    }

    @Test
    @DisplayName("배치 알림 메시지 큐 추가")
    void enqueueBatch() {
        // given
        BatchNotificationMessage message = BatchNotificationMessage.forCourse(
                1L, 1, 100L, Arrays.asList(200L, 300L), 10L, "알림", "메시지"
        );
        when(listOperations.leftPush(anyString(), any())).thenReturn(1L);

        // when
        queueService.enqueueBatch(message);

        // then
        verify(listOperations).leftPush(
                eq(NotificationRedisConfig.BATCH_NOTIFICATION_QUEUE_KEY),
                eq(message)
        );
    }

    @Test
    @DisplayName("단일 알림 메시지 큐에서 조회")
    void dequeue() {
        // given
        NotificationMessage message = NotificationMessage.of(1, 100L, 200L, "테스트");
        when(listOperations.rightPop(anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(message);

        // when
        Optional<NotificationMessage> result = queueService.dequeue(5);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRecipientId()).isEqualTo(200L);
        verify(listOperations).rightPop(
                eq(NotificationRedisConfig.NOTIFICATION_QUEUE_KEY),
                eq(5L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("큐가 비어있을 때 빈 Optional 반환")
    void dequeueEmpty() {
        // given
        when(listOperations.rightPop(anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(null);

        // when
        Optional<NotificationMessage> result = queueService.dequeue(5);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("배치 알림 메시지 큐에서 조회")
    void dequeueBatch() {
        // given
        BatchNotificationMessage message = BatchNotificationMessage.forCourse(
                1L, 1, 100L, Arrays.asList(200L, 300L), 10L, "알림", "메시지"
        );
        when(listOperations.rightPop(anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(message);

        // when
        Optional<BatchNotificationMessage> result = queueService.dequeueBatch(5);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getBatchId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("큐 크기 조회")
    void getQueueSize() {
        // given
        when(listOperations.size(NotificationRedisConfig.NOTIFICATION_QUEUE_KEY))
                .thenReturn(10L);

        // when
        long size = queueService.getQueueSize();

        // then
        assertThat(size).isEqualTo(10L);
    }

    @Test
    @DisplayName("큐 크기 null인 경우 0 반환")
    void getQueueSizeNull() {
        // given
        when(listOperations.size(NotificationRedisConfig.NOTIFICATION_QUEUE_KEY))
                .thenReturn(null);

        // when
        long size = queueService.getQueueSize();

        // then
        assertThat(size).isEqualTo(0L);
    }

    @Test
    @DisplayName("배치 큐 크기 조회")
    void getBatchQueueSize() {
        // given
        when(listOperations.size(NotificationRedisConfig.BATCH_NOTIFICATION_QUEUE_KEY))
                .thenReturn(5L);

        // when
        long size = queueService.getBatchQueueSize();

        // then
        assertThat(size).isEqualTo(5L);
    }

    @Test
    @DisplayName("큐 초기화")
    void clearQueue() {
        // given
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // when
        queueService.clearQueue();

        // then
        verify(redisTemplate).delete(NotificationRedisConfig.NOTIFICATION_QUEUE_KEY);
        verify(redisTemplate).delete(NotificationRedisConfig.BATCH_NOTIFICATION_QUEUE_KEY);
    }

    @Test
    @DisplayName("큐 추가 실패 시 예외 발생")
    void enqueueFailure() {
        // given
        NotificationMessage message = NotificationMessage.of(1, 100L, 200L, "테스트");
        when(listOperations.leftPush(anyString(), any()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        // when & then
        assertThatThrownBy(() -> queueService.enqueue(message))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("알림 큐 추가 실패");
    }
}
