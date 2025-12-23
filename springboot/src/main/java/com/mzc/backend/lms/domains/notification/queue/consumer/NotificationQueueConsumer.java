package com.mzc.backend.lms.domains.notification.queue.consumer;

import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.processor.NotificationProcessor;
import com.mzc.backend.lms.domains.notification.queue.service.NotificationQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 알림 큐 컨슈머
 * Redis 큐에서 알림 메시지를 가져와 처리
 * SmartLifecycle을 구현하여 Redis ConnectionFactory보다 먼저 종료되도록 함
 */
@Slf4j
@Component
@EnableAsync
public class NotificationQueueConsumer implements SmartLifecycle {
	
	private final NotificationQueueService queueService;
	private final NotificationProcessor processor;
	private final AtomicBoolean running = new AtomicBoolean(false);
	@Value("${notification.queue.consumer.enabled:true}")
	private boolean consumerEnabled;
	@Value("${notification.queue.consumer.poll-timeout:5}")
	private long pollTimeoutSeconds;
	@Value("${notification.queue.consumer.thread-count:2}")
	private int threadCount;
	private ExecutorService executorService;
	
	public NotificationQueueConsumer(NotificationQueueService queueService, NotificationProcessor processor) {
		this.queueService = queueService;
		this.processor = processor;
	}
	
	/**
	 * SmartLifecycle - 시작
	 * Redis ConnectionFactory가 준비된 후 시작됨
	 */
	@Override
	public void start() {
		if (consumerEnabled) {
			startConsumers();
		} else {
			log.info("알림 큐 컨슈머 비활성화 상태");
		}
	}
	
	/**
	 * SmartLifecycle - 종료
	 * Redis ConnectionFactory보다 먼저 종료됨 (phase가 낮으므로)
	 */
	@Override
	public void stop() {
		stopConsumers();
	}
	
	/**
	 * SmartLifecycle - 실행 상태 확인
	 */
	@Override
	public boolean isRunning() {
		return running.get();
	}
	
	/**
	 * SmartLifecycle - 자동 시작 여부
	 */
	@Override
	public boolean isAutoStartup() {
		return true;
	}
	
	/**
	 * SmartLifecycle - phase 설정
	 * 낮은 값이 먼저 시작되고 나중에 종료됨
	 * Redis ConnectionFactory는 기본값(0)이므로, 이보다 높은 값을 설정하면
	 * 나중에 시작되고 먼저 종료됨
	 */
	@Override
	public int getPhase() {
		return Integer.MAX_VALUE - 1;
	}
	
	/**
	 * 컨슈머 시작
	 */
	private void startConsumers() {
		if (running.compareAndSet(false, true)) {
			executorService = Executors.newFixedThreadPool(threadCount);
			
			// 단일 알림 처리 스레드
			executorService.submit(this::consumeNotifications);
			
			// 배치 알림 처리 스레드
			executorService.submit(this::consumeBatchNotifications);
			
			log.info("알림 큐 컨슈머 시작: threadCount={}", threadCount);
		}
	}
	
	/**
	 * 컨슈머 중지
	 */
	private void stopConsumers() {
		if (running.compareAndSet(true, false)) {
			if (executorService != null) {
				executorService.shutdown();
				try {
					// 블로킹 작업이 완료될 때까지 대기 (poll timeout + 여유시간)
					if (!executorService.awaitTermination(pollTimeoutSeconds + 5, TimeUnit.SECONDS)) {
						executorService.shutdownNow();
					}
				} catch (InterruptedException e) {
					executorService.shutdownNow();
					Thread.currentThread().interrupt();
				}
			}
			log.info("알림 큐 컨슈머 중지");
		}
	}
	
	/**
	 * 단일 알림 큐 소비
	 */
	private void consumeNotifications() {
		log.info("단일 알림 큐 컨슈머 시작");
		
		while (running.get()) {
			try {
				Optional<NotificationMessage> messageOpt = queueService.dequeue(pollTimeoutSeconds);
				
				if (messageOpt.isPresent()) {
					processWithRetry(messageOpt.get());
				}
				
			} catch (Exception e) {
				log.error("알림 큐 소비 중 오류 발생: {}", e.getMessage(), e);
				sleepOnError();
			}
		}
		
		log.info("단일 알림 큐 컨슈머 종료");
	}
	
	/**
	 * 배치 알림 큐 소비
	 */
	private void consumeBatchNotifications() {
		log.info("배치 알림 큐 컨슈머 시작");
		
		while (running.get()) {
			try {
				Optional<BatchNotificationMessage> messageOpt = queueService.dequeueBatch(pollTimeoutSeconds);
				
				if (messageOpt.isPresent()) {
					processWithRetry(messageOpt.get());
				}
				
			} catch (Exception e) {
				log.error("배치 알림 큐 소비 중 오류 발생: {}", e.getMessage(), e);
				sleepOnError();
			}
		}
		
		log.info("배치 알림 큐 컨슈머 종료");
	}
	
	/**
	 * 단일 알림 재시도 처리
	 */
	private void processWithRetry(NotificationMessage message) {
		int maxRetries = 3;
		int attempt = 0;
		
		while (attempt < maxRetries) {
			try {
				processor.process(message);
				return;
			} catch (Exception e) {
				attempt++;
				log.warn("알림 처리 실패 (시도 {}/{}): {}", attempt, maxRetries, e.getMessage());
				
				if (attempt >= maxRetries) {
					log.error("알림 처리 최종 실패, 메시지 폐기: recipientId={}", message.getRecipientId());
					// TODO: Dead Letter Queue 처리
				}
				
				sleepOnError();
			}
		}
	}
	
	/**
	 * 배치 알림 재시도 처리
	 */
	private void processWithRetry(BatchNotificationMessage message) {
		int maxRetries = 3;
		int attempt = 0;
		
		while (attempt < maxRetries) {
			try {
				processor.processBatch(message);
				return;
			} catch (Exception e) {
				attempt++;
				log.warn("배치 알림 처리 실패 (시도 {}/{}): {}", attempt, maxRetries, e.getMessage());
				
				if (attempt >= maxRetries) {
					log.error("배치 알림 처리 최종 실패, 메시지 폐기: batchId={}", message.getBatchId());
					// TODO: Dead Letter Queue 처리
				}
				
				sleepOnError();
			}
		}
	}
	
	/**
	 * 오류 발생 시 잠시 대기
	 */
	private void sleepOnError() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
