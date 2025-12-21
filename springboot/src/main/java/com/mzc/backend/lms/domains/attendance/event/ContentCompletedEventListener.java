package com.mzc.backend.lms.domains.attendance.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.backend.lms.domains.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * 콘텐츠 완료 이벤트 리스너
 * Video Streaming Server에서 발행한 이벤트를 수신하여 출석 상태를 갱신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentCompletedEventListener implements MessageListener {

    private final AttendanceService attendanceService;

    @Qualifier("attendanceObjectMapper")
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            log.info("Received content completed event: {}", messageBody);

            ContentCompletedEvent event = objectMapper.readValue(messageBody, ContentCompletedEvent.class);
            log.info("Parsed event: {}", event);

            attendanceService.processContentCompleted(event);

        } catch (Exception e) {
            log.error("Failed to process content completed event", e);
        }
    }
}
