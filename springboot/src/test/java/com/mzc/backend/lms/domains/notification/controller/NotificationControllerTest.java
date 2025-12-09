package com.mzc.backend.lms.domains.notification.controller;

import com.mzc.backend.lms.domains.notification.dto.NotificationCursorResponseDto;
import com.mzc.backend.lms.domains.notification.dto.NotificationResponseDto;
import com.mzc.backend.lms.domains.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationController 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationController 테스트")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService);
    }

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotificationsSuccess() {
        // given
        Long userId = 100L;
        NotificationCursorResponseDto mockResponse = createMockCursorResponse(5, true);

        when(notificationService.getNotifications(eq(userId), isNull(), eq(20)))
                .thenReturn(mockResponse);

        // when
        ResponseEntity<?> response = controller.getNotifications(userId, null, 20, false);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(NotificationCursorResponseDto.class);

        NotificationCursorResponseDto body = (NotificationCursorResponseDto) response.getBody();
        assertThat(body.getNotifications()).hasSize(5);
        assertThat(body.isHasNext()).isTrue();
    }

    @Test
    @DisplayName("읽지 않은 알림만 조회")
    void getUnreadNotifications() {
        // given
        Long userId = 100L;
        NotificationCursorResponseDto mockResponse = createMockCursorResponse(3, false);

        when(notificationService.getUnreadNotifications(eq(userId), isNull(), eq(20)))
                .thenReturn(mockResponse);

        // when
        ResponseEntity<?> response = controller.getNotifications(userId, null, 20, true);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).getUnreadNotifications(userId, null, 20);
        verify(notificationService, never()).getNotifications(anyLong(), any(), anyInt());
    }

    @Test
    @DisplayName("커서로 다음 페이지 조회")
    void getNotificationsWithCursor() {
        // given
        Long userId = 100L;
        Long cursor = 50L;
        NotificationCursorResponseDto mockResponse = createMockCursorResponse(5, true);

        when(notificationService.getNotifications(eq(userId), eq(cursor), eq(20)))
                .thenReturn(mockResponse);

        // when
        ResponseEntity<?> response = controller.getNotifications(userId, cursor, 20, false);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).getNotifications(userId, cursor, 20);
    }

    @Test
    @DisplayName("인증되지 않은 사용자 - 400 반환")
    void getNotificationsUnauthorized() {
        // when
        ResponseEntity<?> response = controller.getNotifications(null, null, 20, false);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("success")).isEqualTo(false);
        assertThat(body.get("message")).isEqualTo("인증이 필요합니다.");
    }

    @Test
    @DisplayName("페이지 크기 최대값 제한")
    void getNotificationsMaxSize() {
        // given
        Long userId = 100L;
        NotificationCursorResponseDto mockResponse = createMockCursorResponse(100, false);

        when(notificationService.getNotifications(eq(userId), isNull(), eq(100)))
                .thenReturn(mockResponse);

        // when - 200 요청해도 100으로 제한
        ResponseEntity<?> response = controller.getNotifications(userId, null, 200, false);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).getNotifications(userId, null, 100);
    }

    @Test
    @DisplayName("알림 상세 조회 성공")
    void getNotificationSuccess() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;
        NotificationResponseDto mockResponse = createMockNotificationResponse(notificationId);

        when(notificationService.getNotification(userId, notificationId)).thenReturn(mockResponse);

        // when
        ResponseEntity<?> response = controller.getNotification(userId, notificationId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(NotificationResponseDto.class);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회")
    void getUnreadCount() {
        // given
        Long userId = 100L;

        when(notificationService.getUnreadCount(userId)).thenReturn(10L);

        // when
        ResponseEntity<?> response = controller.getUnreadCount(userId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("unreadCount")).isEqualTo(10L);
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markAsReadSuccess() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;
        NotificationResponseDto mockResponse = createMockNotificationResponse(notificationId);

        when(notificationService.markAsRead(userId, notificationId)).thenReturn(mockResponse);

        // when
        ResponseEntity<?> response = controller.markAsRead(userId, notificationId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(notificationService).markAsRead(userId, notificationId);
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 성공")
    void markAllAsReadSuccess() {
        // given
        Long userId = 100L;

        when(notificationService.markAllAsRead(userId)).thenReturn(5);

        // when
        ResponseEntity<?> response = controller.markAllAsRead(userId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("success")).isEqualTo(true);
        assertThat(body.get("updatedCount")).isEqualTo(5);
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void deleteNotificationSuccess() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;

        doNothing().when(notificationService).deleteNotification(userId, notificationId);

        // when
        ResponseEntity<?> response = controller.deleteNotification(userId, notificationId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("success")).isEqualTo(true);
        verify(notificationService).deleteNotification(userId, notificationId);
    }

    @Test
    @DisplayName("알림 삭제 실패 - 권한 없음")
    void deleteNotificationUnauthorized() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;

        doThrow(new IllegalArgumentException("해당 알림에 대한 권한이 없습니다."))
                .when(notificationService).deleteNotification(userId, notificationId);

        // when
        ResponseEntity<?> response = controller.deleteNotification(userId, notificationId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("success")).isEqualTo(false);
    }

    @Test
    @DisplayName("서비스 예외 발생 시 500 반환")
    void getNotificationsServerError() {
        // given
        Long userId = 100L;

        when(notificationService.getNotifications(eq(userId), isNull(), anyInt()))
                .thenThrow(new RuntimeException("DB 연결 오류"));

        // when
        ResponseEntity<?> response = controller.getNotifications(userId, null, 20, false);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("success")).isEqualTo(false);
    }

    /**
     * 테스트용 커서 응답 생성
     */
    private NotificationCursorResponseDto createMockCursorResponse(int count, boolean hasNext) {
        List<NotificationResponseDto> notifications = new ArrayList<>();
        for (int i = count; i >= 1; i--) {
            notifications.add(createMockNotificationResponse((long) i));
        }

        return NotificationCursorResponseDto.builder()
                .notifications(notifications)
                .nextCursor(hasNext ? 1L : null)
                .hasNext(hasNext)
                .size(20)
                .unreadCount(5L)
                .build();
    }

    /**
     * 테스트용 알림 응답 생성
     */
    private NotificationResponseDto createMockNotificationResponse(Long id) {
        return NotificationResponseDto.builder()
                .id(id)
                .typeCode("TEST_TYPE")
                .typeName("테스트 타입")
                .category("테스트")
                .title("테스트 알림 " + id)
                .message("테스트 메시지")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
