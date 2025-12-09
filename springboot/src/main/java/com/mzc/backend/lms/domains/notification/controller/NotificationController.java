package com.mzc.backend.lms.domains.notification.controller;

import com.mzc.backend.lms.domains.notification.dto.NotificationCursorResponseDto;
import com.mzc.backend.lms.domains.notification.dto.NotificationResponseDto;
import com.mzc.backend.lms.domains.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 알림 컨트롤러
 * 커서 기반 페이징으로 대용량 데이터 효율적 처리
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    /**
     * 알림 목록 조회 (커서 기반)
     *
     * @param cursor 커서 (이전 응답의 nextCursor, 첫 요청 시 생략)
     * @param size 페이지 크기 (기본값: 20, 최대: 100)
     * @param unreadOnly 읽지 않은 알림만 조회 여부
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        try {
            validateUserId(userId);
            int validSize = validateSize(size);

            NotificationCursorResponseDto response;
            if (unreadOnly) {
                response = notificationService.getUnreadNotifications(userId, cursor, validSize);
            } else {
                response = notificationService.getNotifications(userId, cursor, validSize);
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("알림 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("알림 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 알림 상세 조회
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotification(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId) {
        try {
            validateUserId(userId);

            NotificationResponseDto response = notificationService.getNotification(userId, notificationId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("알림 상세 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("알림 조회에 실패했습니다."));
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal Long userId) {
        try {
            validateUserId(userId);

            long unreadCount = notificationService.getUnreadCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("알림 개수 조회에 실패했습니다."));
        }
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId) {
        try {
            validateUserId(userId);

            NotificationResponseDto response = notificationService.markAsRead(userId, notificationId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("알림 읽음 처리에 실패했습니다."));
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal Long userId) {
        try {
            validateUserId(userId);

            int updatedCount = notificationService.markAllAsRead(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모든 알림이 읽음 처리되었습니다.");
            response.put("updatedCount", updatedCount);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("알림 읽음 처리에 실패했습니다."));
        }
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId) {
        try {
            validateUserId(userId);

            notificationService.deleteNotification(userId, notificationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "알림이 삭제되었습니다.");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("알림 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("알림 삭제에 실패했습니다."));
        }
    }

    /**
     * 읽은 알림 일괄 삭제 (벌크 삭제)
     */
    @DeleteMapping("/read")
    public ResponseEntity<?> deleteReadNotifications(@AuthenticationPrincipal Long userId) {
        try {
            validateUserId(userId);

            int deletedCount = notificationService.deleteReadNotifications(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "읽은 알림이 삭제되었습니다.");
            response.put("deletedCount", deletedCount);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("읽은 알림 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("읽은 알림 삭제에 실패했습니다."));
        }
    }

    /**
     * 모든 알림 삭제 (벌크 삭제)
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(@AuthenticationPrincipal Long userId) {
        try {
            validateUserId(userId);

            int deletedCount = notificationService.deleteAllNotifications(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모든 알림이 삭제되었습니다.");
            response.put("deletedCount", deletedCount);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("모든 알림 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("모든 알림 삭제에 실패했습니다."));
        }
    }

    /**
     * 사용자 ID 유효성 검증
     */
    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
    }

    /**
     * 페이지 크기 유효성 검증
     */
    private int validateSize(int size) {
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }

    /**
     * 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
