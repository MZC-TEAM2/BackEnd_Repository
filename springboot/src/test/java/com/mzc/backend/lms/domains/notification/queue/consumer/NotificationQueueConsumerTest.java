package com.mzc.backend.lms.domains.notification.queue.consumer;

import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.processor.NotificationProcessor;
import com.mzc.backend.lms.domains.notification.queue.service.NotificationQueueService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * NotificationQueueConsumer 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationQueueConsumer 테스트")
class NotificationQueueConsumerTest {
	
	@Mock
	private NotificationQueueService queueService;
	
	@Mock
	private NotificationProcessor processor;
	
	private NotificationQueueConsumer consumer;
	
	@BeforeEach
	void setUp() {
		consumer = new NotificationQueueConsumer(queueService, processor);
		ReflectionTestUtils.setField(consumer, "consumerEnabled", false);
		ReflectionTestUtils.setField(consumer, "pollTimeoutSeconds", 1L);
		ReflectionTestUtils.setField(consumer, "threadCount", 2);
	}
	
	@AfterEach
	void tearDown() {
		consumer.stop();
	}
	
	@Test
	@DisplayName("컨슈머 시작 후 running 상태 true")
	void startConsumers() {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		when(queueService.dequeue(anyLong())).thenReturn(Optional.empty());
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		
		// when
		consumer.start();
		
		// then
		assertThat(consumer.isRunning()).isTrue();
	}
	
	@Test
	@DisplayName("컨슈머 중지 후 running 상태 false")
	void stopConsumers() {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		when(queueService.dequeue(anyLong())).thenReturn(Optional.empty());
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		consumer.start();
		
		// when
		consumer.stop();
		
		// then
		assertThat(consumer.isRunning()).isFalse();
	}
	
	@Test
	@DisplayName("이미 시작된 컨슈머는 중복 시작되지 않음")
	void startConsumersAlreadyRunning() {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		when(queueService.dequeue(anyLong())).thenReturn(Optional.empty());
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		consumer.start();
		
		// when
		consumer.start(); // 중복 호출
		
		// then
		assertThat(consumer.isRunning()).isTrue();
	}
	
	@Test
	@DisplayName("이미 중지된 컨슈머는 중복 중지되지 않음")
	void stopConsumersAlreadyStopped() {
		// when
		consumer.stop(); // 시작하지 않고 중지 호출
		
		// then
		assertThat(consumer.isRunning()).isFalse();
	}
	
	@Test
	@DisplayName("단일 알림 메시지 처리")
	void processNotificationMessage() throws InterruptedException {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		NotificationMessage message = NotificationMessage.of(1, 100L, 200L, "테스트");
		CountDownLatch latch = new CountDownLatch(1);
		
		when(queueService.dequeue(anyLong()))
				.thenReturn(Optional.of(message))
				.thenAnswer(invocation -> {
					latch.countDown();
					return Optional.empty();
				});
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		
		// when
		consumer.start();
		boolean completed = latch.await(5, TimeUnit.SECONDS);
		
		// then
		assertThat(completed).isTrue();
		verify(processor, timeout(2000)).process(message);
	}
	
	@Test
	@DisplayName("배치 알림 메시지 처리")
	void processBatchNotificationMessage() throws InterruptedException {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		BatchNotificationMessage message = BatchNotificationMessage.forCourse(
				1L, 1, 100L, Arrays.asList(200L, 300L), 10L, "알림", "메시지"
		);
		CountDownLatch latch = new CountDownLatch(1);
		
		when(queueService.dequeue(anyLong())).thenReturn(Optional.empty());
		when(queueService.dequeueBatch(anyLong()))
				.thenReturn(Optional.of(message))
				.thenAnswer(invocation -> {
					latch.countDown();
					return Optional.empty();
				});
		
		// when
		consumer.start();
		boolean completed = latch.await(5, TimeUnit.SECONDS);
		
		// then
		assertThat(completed).isTrue();
		verify(processor, timeout(2000)).processBatch(message);
	}
	
	@Test
	@DisplayName("단일 알림 처리 실패 시 재시도")
	void processWithRetryForNotification() throws InterruptedException {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		NotificationMessage message = NotificationMessage.of(1, 100L, 200L, "테스트");
		AtomicInteger callCount = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(1);
		
		when(queueService.dequeue(anyLong()))
				.thenReturn(Optional.of(message))
				.thenAnswer(invocation -> {
					latch.countDown();
					return Optional.empty();
				});
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		
		doAnswer(invocation -> {
			int count = callCount.incrementAndGet();
			if (count < 3) {
				throw new RuntimeException("처리 실패");
			}
			return null;
		}).when(processor).process(any(NotificationMessage.class));
		
		// when
		consumer.start();
		boolean completed = latch.await(10, TimeUnit.SECONDS);
		
		// then
		assertThat(completed).isTrue();
		verify(processor, timeout(5000).atLeast(2)).process(message);
	}
	
	@Test
	@DisplayName("배치 알림 처리 실패 시 재시도")
	void processWithRetryForBatch() throws InterruptedException {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		BatchNotificationMessage message = BatchNotificationMessage.forCourse(
				1L, 1, 100L, Arrays.asList(200L, 300L), 10L, "알림", "메시지"
		);
		AtomicInteger callCount = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(1);
		
		when(queueService.dequeue(anyLong())).thenReturn(Optional.empty());
		when(queueService.dequeueBatch(anyLong()))
				.thenReturn(Optional.of(message))
				.thenAnswer(invocation -> {
					latch.countDown();
					return Optional.empty();
				});
		
		doAnswer(invocation -> {
			int count = callCount.incrementAndGet();
			if (count < 3) {
				throw new RuntimeException("처리 실패");
			}
			return null;
		}).when(processor).processBatch(any(BatchNotificationMessage.class));
		
		// when
		consumer.start();
		boolean completed = latch.await(10, TimeUnit.SECONDS);
		
		// then
		assertThat(completed).isTrue();
		verify(processor, timeout(5000).atLeast(2)).processBatch(message);
	}
	
	@Test
	@DisplayName("start 메소드 - 컨슈머 비활성화 상태")
	void startWithConsumerDisabled() {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", false);
		
		// when
		consumer.start();
		
		// then
		assertThat(consumer.isRunning()).isFalse();
	}
	
	@Test
	@DisplayName("start 메소드 - 컨슈머 활성화 상태")
	void startWithConsumerEnabled() {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		when(queueService.dequeue(anyLong())).thenReturn(Optional.empty());
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		
		// when
		consumer.start();
		
		// then
		assertThat(consumer.isRunning()).isTrue();
	}
	
	@Test
	@DisplayName("stop 메소드 호출 시 컨슈머 중지")
	void stopStopsConsumer() {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		when(queueService.dequeue(anyLong())).thenReturn(Optional.empty());
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		consumer.start();
		assertThat(consumer.isRunning()).isTrue();
		
		// when
		consumer.stop();
		
		// then
		assertThat(consumer.isRunning()).isFalse();
	}
	
	@Test
	@DisplayName("큐 서비스 오류 발생 시에도 컨슈머 계속 동작")
	void consumerContinuesOnQueueServiceError() throws InterruptedException {
		// given
		ReflectionTestUtils.setField(consumer, "consumerEnabled", true);
		AtomicInteger callCount = new AtomicInteger(0);
		CountDownLatch latch = new CountDownLatch(3);
		
		when(queueService.dequeue(anyLong()))
				.thenAnswer(invocation -> {
					int count = callCount.incrementAndGet();
					latch.countDown();
					if (count == 1) {
						throw new RuntimeException("Redis 연결 오류");
					}
					return Optional.empty();
				});
		when(queueService.dequeueBatch(anyLong())).thenReturn(Optional.empty());
		
		// when
		consumer.start();
		boolean completed = latch.await(10, TimeUnit.SECONDS);
		
		// then
		assertThat(completed).isTrue();
		assertThat(callCount.get()).isGreaterThanOrEqualTo(3);
		assertThat(consumer.isRunning()).isTrue();
	}
	
	@Test
	@DisplayName("SmartLifecycle - isAutoStartup은 true")
	void isAutoStartupReturnsTrue() {
		assertThat(consumer.isAutoStartup()).isTrue();
	}
	
	@Test
	@DisplayName("SmartLifecycle - getPhase는 높은 값 반환")
	void getPhaseReturnsHighValue() {
		assertThat(consumer.getPhase()).isEqualTo(Integer.MAX_VALUE - 1);
	}
}
