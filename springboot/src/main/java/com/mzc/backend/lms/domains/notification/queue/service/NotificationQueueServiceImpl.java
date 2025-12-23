package com.mzc.backend.lms.domains.notification.queue.service;

import com.mzc.backend.lms.domains.notification.queue.config.NotificationRedisConfig;
import com.mzc.backend.lms.domains.notification.queue.dto.BatchNotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis List를 활용한 알림 큐 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationQueueServiceImpl implements NotificationQueueService {
	
	@Qualifier("notificationRedisTemplate")
	private final RedisTemplate<String, Object> redisTemplate;
	
	@Override
	public void enqueue(NotificationMessage message) {
		try {
			redisTemplate.opsForList().leftPush(
					NotificationRedisConfig.NOTIFICATION_QUEUE_KEY,
					message
			);
			log.debug("알림 메시지 큐 추가: recipientId={}", message.getRecipientId());
		} catch (Exception e) {
			log.error("알림 메시지 큐 추가 실패: {}", e.getMessage(), e);
			throw new RuntimeException("알림 큐 추가 실패", e);
		}
	}
	
	@Override
	public void enqueueBatch(BatchNotificationMessage message) {
		try {
			redisTemplate.opsForList().leftPush(
					NotificationRedisConfig.BATCH_NOTIFICATION_QUEUE_KEY,
					message
			);
			log.debug("배치 알림 메시지 큐 추가: batchId={}, recipientCount={}",
					message.getBatchId(), message.getRecipientCount());
		} catch (Exception e) {
			log.error("배치 알림 메시지 큐 추가 실패: {}", e.getMessage(), e);
			throw new RuntimeException("배치 알림 큐 추가 실패", e);
		}
	}
	
	@Override
	public Optional<NotificationMessage> dequeue(long timeoutSeconds) {
		try {
			Object result = redisTemplate.opsForList().rightPop(
					NotificationRedisConfig.NOTIFICATION_QUEUE_KEY,
					timeoutSeconds,
					TimeUnit.SECONDS
			);
			
			if (result == null) {
				return Optional.empty();
			}
			
			log.debug("큐에서 메시지 수신: type={}, value={}", result.getClass().getName(), result);
			
			if (result instanceof NotificationMessage) {
				return Optional.of((NotificationMessage) result);
			}
			
			// LinkedHashMap으로 역직렬화된 경우 수동 변환
			if (result instanceof java.util.Map) {
				log.warn("메시지가 Map으로 역직렬화됨, 수동 변환 시도: {}", result);
				try {
					@SuppressWarnings("unchecked")
					java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
					NotificationMessage message = convertMapToNotificationMessage(map);
					return Optional.of(message);
				} catch (Exception e) {
					log.error("Map -> NotificationMessage 변환 실패: {}", e.getMessage(), e);
					return Optional.empty();
				}
			}
			
			log.warn("알 수 없는 메시지 타입, 메시지 폐기: {}", result.getClass().getName());
			return Optional.empty();
		} catch (Exception e) {
			log.error("알림 메시지 큐 조회 실패: {}", e.getMessage(), e);
			return Optional.empty();
		}
	}
	
	/**
	 * Map을 NotificationMessage로 변환
	 */
	private NotificationMessage convertMapToNotificationMessage(java.util.Map<String, Object> map) {
		return NotificationMessage.builder()
				.typeId(map.get("typeId") != null ? ((Number) map.get("typeId")).intValue() : null)
				.senderId(map.get("senderId") != null ? ((Number) map.get("senderId")).longValue() : null)
				.recipientId(map.get("recipientId") != null ? ((Number) map.get("recipientId")).longValue() : null)
				.courseId(map.get("courseId") != null ? ((Number) map.get("courseId")).longValue() : null)
				.relatedEntityType((String) map.get("relatedEntityType"))
				.relatedEntityId(map.get("relatedEntityId") != null ? ((Number) map.get("relatedEntityId")).longValue() : null)
				.title((String) map.get("title"))
				.message((String) map.get("message"))
				.actionUrl((String) map.get("actionUrl"))
				.build();
	}
	
	@Override
	public Optional<BatchNotificationMessage> dequeueBatch(long timeoutSeconds) {
		try {
			Object result = redisTemplate.opsForList().rightPop(
					NotificationRedisConfig.BATCH_NOTIFICATION_QUEUE_KEY,
					timeoutSeconds,
					TimeUnit.SECONDS
			);
			if (result instanceof BatchNotificationMessage) {
				return Optional.of((BatchNotificationMessage) result);
			}
			return Optional.empty();
		} catch (Exception e) {
			log.error("배치 알림 메시지 큐 조회 실패: {}", e.getMessage(), e);
			return Optional.empty();
		}
	}
	
	@Override
	public long getQueueSize() {
		Long size = redisTemplate.opsForList().size(NotificationRedisConfig.NOTIFICATION_QUEUE_KEY);
		return size != null ? size : 0L;
	}
	
	@Override
	public long getBatchQueueSize() {
		Long size = redisTemplate.opsForList().size(NotificationRedisConfig.BATCH_NOTIFICATION_QUEUE_KEY);
		return size != null ? size : 0L;
	}
	
	@Override
	public void clearQueue() {
		redisTemplate.delete(NotificationRedisConfig.NOTIFICATION_QUEUE_KEY);
		redisTemplate.delete(NotificationRedisConfig.BATCH_NOTIFICATION_QUEUE_KEY);
		log.info("알림 큐 초기화 완료");
	}
}
