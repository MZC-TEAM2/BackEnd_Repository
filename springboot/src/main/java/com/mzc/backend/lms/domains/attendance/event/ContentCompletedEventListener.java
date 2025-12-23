package com.mzc.backend.lms.domains.attendance.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.backend.lms.domains.attendance.service.AttendanceService;
import com.mzc.backend.lms.util.lock.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 콘텐츠 완료 이벤트 리스너
 * Video Streaming Server에서 발행한 이벤트를 수신하여 출석 상태를 갱신
 * <p>
 * 분산 락을 사용하여 여러 서버에서 동시에 처리되는 것을 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentCompletedEventListener implements MessageListener {
	
	private static final String LOCK_KEY_PREFIX = "attendance:event:";
	
	private final AttendanceService attendanceService;
	private final DistributedLockService distributedLockService;
	
	@Qualifier("attendanceObjectMapper")
	private final ObjectMapper objectMapper;
	
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String messageBody = new String(message.getBody());
			log.info("Received content completed event: {}", messageBody);
			
			// 이중 직렬화 처리: Video Server에서 JSON 문자열이 RedisTemplate에 의해 다시 직렬화된 경우
			// 메시지가 따옴표로 시작하면 먼저 String으로 역직렬화
			if (messageBody.startsWith("\"")) {
				messageBody = objectMapper.readValue(messageBody, String.class);
				log.debug("Unwrapped double-serialized message: {}", messageBody);
			}
			
			ContentCompletedEvent event = objectMapper.readValue(messageBody, ContentCompletedEvent.class);
			log.info("Parsed event: studentId={}, contentId={}, weekId={}, courseId={}",
					event.getStudentId(), event.getContentId(), event.getWeekId(), event.getCourseId());
			
			// 분산 락을 사용하여 하나의 서버만 처리
			String lockKey = LOCK_KEY_PREFIX + event.getStudentId() + ":" + event.getWeekId();
			boolean processed = distributedLockService.tryExecuteWithLock(lockKey, () -> {
				attendanceService.processContentCompleted(event);
			});
			
			if (!processed) {
				log.debug("Event already being processed by another server: studentId={}, weekId={}",
						event.getStudentId(), event.getWeekId());
			}
			
		} catch (Exception e) {
			log.error("Failed to process content completed event", e);
		}
	}
}
