package com.mzc.backend.lms.domains.notification.aop.aspect;

import com.mzc.backend.lms.domains.notification.aop.annotation.NotifyEvent;
import com.mzc.backend.lms.domains.notification.aop.event.NotificationEventType;
import com.mzc.backend.lms.domains.notification.aop.publisher.NotificationEventPublisher;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationAspect 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationAspect 테스트")
class NotificationAspectTest {

    @Mock
    private NotificationEventPublisher eventPublisher;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private NotificationAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new NotificationAspect(eventPublisher);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getTarget()).thenReturn(new TestService());
    }

    @Test
    @DisplayName("단일 수신자 알림 발송")
    void afterReturningSingleRecipient() throws NoSuchMethodException {
        // given
        Method method = TestService.class.getMethod("singleRecipientMethod", Long.class, String.class);
        NotifyEvent notifyEvent = method.getAnnotation(NotifyEvent.class);

        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{200L, "테스트 메시지"});

        TestResult result = new TestResult(1L, "테스트 과제");

        // when
        aspect.afterReturning(joinPoint, notifyEvent, result);

        // then
        verify(eventPublisher).publish(
                eq(NotificationEventType.ASSIGNMENT_CREATED),
                isNull(),
                eq(200L),
                eq("새 과제 알림"),
                eq("테스트 과제 과제가 등록되었습니다.")
        );
    }

    @Test
    @DisplayName("배치 수신자 알림 발송")
    void afterReturningBatchRecipients() throws NoSuchMethodException {
        // given
        Method method = TestService.class.getMethod("batchRecipientMethod", Long.class);
        NotifyEvent notifyEvent = method.getAnnotation(NotifyEvent.class);

        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L});

        TestResultWithRecipients result = new TestResultWithRecipients(
                1L, "공지사항", Arrays.asList(100L, 200L, 300L)
        );

        // when
        aspect.afterReturning(joinPoint, notifyEvent, result);

        // then
        verify(eventPublisher).publishBatch(
                eq(NotificationEventType.COURSE_NOTICE_CREATED),
                isNull(),
                eq(Arrays.asList(100L, 200L, 300L)),
                eq(10L),
                eq("강의 공지"),
                eq("공지사항 공지가 등록되었습니다.")
        );
    }

    @Test
    @DisplayName("조건이 false면 알림 발송하지 않음")
    void afterReturningConditionFalse() throws NoSuchMethodException {
        // given
        Method method = TestService.class.getMethod("conditionalMethod", Long.class);
        NotifyEvent notifyEvent = method.getAnnotation(NotifyEvent.class);

        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{200L});

        TestResult result = new TestResult(1L, "테스트");
        // result.isNotifiable() returns false

        // when
        aspect.afterReturning(joinPoint, notifyEvent, result);

        // then
        verify(eventPublisher, never()).publish(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("조건이 true면 알림 발송")
    void afterReturningConditionTrue() throws NoSuchMethodException {
        // given
        Method method = TestService.class.getMethod("conditionalMethod", Long.class);
        NotifyEvent notifyEvent = method.getAnnotation(NotifyEvent.class);

        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{200L});

        TestResult result = new TestResult(1L, "테스트");
        result.setNotifiable(true);

        // when
        aspect.afterReturning(joinPoint, notifyEvent, result);

        // then
        verify(eventPublisher).publish(
                eq(NotificationEventType.ASSIGNMENT_GRADED),
                isNull(),
                eq(200L),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("엔티티 정보 포함 알림 발송")
    void afterReturningWithEntity() throws NoSuchMethodException {
        // given
        Method method = TestService.class.getMethod("entityMethod", Long.class, Long.class);
        NotifyEvent notifyEvent = method.getAnnotation(NotifyEvent.class);

        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 200L});

        TestResult result = new TestResult(50L, "과제 제목");

        // when
        aspect.afterReturning(joinPoint, notifyEvent, result);

        // then
        verify(eventPublisher).publishWithEntity(
                eq(NotificationEventType.ASSIGNMENT_CREATED),
                isNull(),
                eq(200L),
                eq("ASSIGNMENT"),
                eq(50L),
                eq(10L),
                eq("과제 알림"),
                eq("과제 제목 과제가 등록되었습니다."),
                eq("/courses/10/assignments/50")
        );
    }

    @Test
    @DisplayName("기본 메시지 사용")
    void afterReturningWithDefaultMessage() throws NoSuchMethodException {
        // given
        Method method = TestService.class.getMethod("defaultMessageMethod", Long.class);
        NotifyEvent notifyEvent = method.getAnnotation(NotifyEvent.class);

        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{200L});

        TestResult result = new TestResult(1L, "테스트");

        // when
        aspect.afterReturning(joinPoint, notifyEvent, result);

        // then
        verify(eventPublisher).publish(
                eq(NotificationEventType.PASSWORD_CHANGED),
                isNull(),
                eq(200L),
                eq("시스템 알림"),
                eq("비밀번호가 변경되었습니다.")
        );
    }

    // ========== 테스트용 서비스 클래스 ==========

    static class TestService {

        @NotifyEvent(
                type = NotificationEventType.ASSIGNMENT_CREATED,
                titleExpression = "'새 과제 알림'",
                messageExpression = "#result.title + ' 과제가 등록되었습니다.'",
                recipientIdExpression = "#arg0"
        )
        public TestResult singleRecipientMethod(Long recipientId, String message) {
            return new TestResult(1L, "테스트 과제");
        }

        @NotifyEvent(
                type = NotificationEventType.COURSE_NOTICE_CREATED,
                titleExpression = "'강의 공지'",
                messageExpression = "#result.title + ' 공지가 등록되었습니다.'",
                recipientIdsExpression = "#result.recipientIds",
                courseIdExpression = "#arg0"
        )
        public TestResultWithRecipients batchRecipientMethod(Long courseId) {
            return new TestResultWithRecipients(1L, "공지사항", Arrays.asList(100L, 200L, 300L));
        }

        @NotifyEvent(
                type = NotificationEventType.ASSIGNMENT_GRADED,
                titleExpression = "'성적 알림'",
                messageExpression = "'성적이 등록되었습니다.'",
                recipientIdExpression = "#arg0",
                conditionExpression = "#result.notifiable"
        )
        public TestResult conditionalMethod(Long recipientId) {
            return new TestResult(1L, "테스트");
        }

        @NotifyEvent(
                type = NotificationEventType.ASSIGNMENT_CREATED,
                titleExpression = "'과제 알림'",
                messageExpression = "#result.title + ' 과제가 등록되었습니다.'",
                recipientIdExpression = "#arg1",
                courseIdExpression = "#arg0",
                relatedEntityType = "ASSIGNMENT",
                relatedEntityIdExpression = "#result.id",
                actionUrlExpression = "'/courses/' + #arg0 + '/assignments/' + #result.id"
        )
        public TestResult entityMethod(Long courseId, Long recipientId) {
            return new TestResult(50L, "과제 제목");
        }

        @NotifyEvent(
                type = NotificationEventType.PASSWORD_CHANGED,
                recipientIdExpression = "#arg0"
        )
        public TestResult defaultMessageMethod(Long recipientId) {
            return new TestResult(1L, "테스트");
        }
    }

    // ========== 테스트용 결과 클래스 ==========

    static class TestResult {
        private Long id;
        private String title;
        private boolean notifiable = false;

        public TestResult(Long id, String title) {
            this.id = id;
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public boolean isNotifiable() {
            return notifiable;
        }

        public void setNotifiable(boolean notifiable) {
            this.notifiable = notifiable;
        }
    }

    static class TestResultWithRecipients {
        private Long id;
        private String title;
        private List<Long> recipientIds;

        public TestResultWithRecipients(Long id, String title, List<Long> recipientIds) {
            this.id = id;
            this.title = title;
            this.recipientIds = recipientIds;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public List<Long> getRecipientIds() {
            return recipientIds;
        }
    }
}
