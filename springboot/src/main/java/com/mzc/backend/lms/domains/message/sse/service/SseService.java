package com.mzc.backend.lms.domains.message.sse.service;

import com.mzc.backend.lms.domains.message.sse.dto.MessageNotificationDto;
import com.mzc.backend.lms.domains.message.sse.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * SSE 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분

    private final SseEmitterRepository sseEmitterRepository;

    /**
     * SSE 연결 생성
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        sseEmitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE 연결 완료: userId={}", userId);
            sseEmitterRepository.deleteByUserId(userId);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE 연결 타임아웃: userId={}", userId);
            sseEmitterRepository.deleteByUserId(userId);
        });

        emitter.onError(e -> {
            log.warn("SSE 연결 에러: userId={}, error={}", userId, e.getMessage());
            sseEmitterRepository.deleteByUserId(userId);
        });

        // 연결 직후 더미 이벤트 전송 (연결 확인용)
        sendToUser(userId, "connect", "connected");

        log.info("SSE 연결 성공: userId={}", userId);
        return emitter;
    }

    /**
     * 특정 사용자에게 이벤트 전송
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        sseEmitterRepository.findByUserId(userId).ifPresent(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("SSE 전송 실패: userId={}, error={}", userId, e.getMessage());
                sseEmitterRepository.deleteByUserId(userId);
            }
        });
    }

    /**
     * 새 메시지 알림 전송
     */
    public void sendNewMessageNotification(Long receiverId, MessageNotificationDto notification) {
        sendToUser(receiverId, "message", notification);
        log.debug("새 메시지 알림 전송: receiverId={}, messageId={}",
                receiverId, notification.getMessageId());
    }

    /**
     * 사용자 연결 여부 확인
     */
    public boolean isUserConnected(Long userId) {
        return sseEmitterRepository.isConnected(userId);
    }
}
