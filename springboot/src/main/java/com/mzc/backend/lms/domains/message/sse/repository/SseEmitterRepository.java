package com.mzc.backend.lms.domains.message.sse.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE Emitter 저장소
 * 사용자별 SSE 연결을 관리
 */
@Slf4j
@Repository
public class SseEmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE Emitter 저장
     */
    public SseEmitter save(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);
        log.debug("SSE 연결 저장: userId={}", userId);
        return emitter;
    }

    /**
     * SSE Emitter 조회
     */
    public Optional<SseEmitter> findByUserId(Long userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    /**
     * SSE Emitter 삭제
     */
    public void deleteByUserId(Long userId) {
        emitters.remove(userId);
        log.debug("SSE 연결 삭제: userId={}", userId);
    }

    /**
     * 연결된 사용자 수 조회
     */
    public int getConnectionCount() {
        return emitters.size();
    }

    /**
     * 특정 사용자의 연결 여부 확인
     */
    public boolean isConnected(Long userId) {
        return emitters.containsKey(userId);
    }
}
