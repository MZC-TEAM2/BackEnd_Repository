package com.mzc.backend.lms.domains.notification.aop.aspect;

import com.mzc.backend.lms.domains.notification.aop.annotation.NotifyEvent;
import com.mzc.backend.lms.domains.notification.aop.event.NotificationEventType;
import com.mzc.backend.lms.domains.notification.aop.publisher.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 알림 이벤트 AOP Aspect
 * @NotifyEvent 어노테이션이 붙은 메소드 실행 후 자동으로 알림 발송
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect {

    private final NotificationEventPublisher eventPublisher;

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * @NotifyEvent 어노테이션이 붙은 메소드 실행 성공 후 알림 발송
     */
    @AfterReturning(pointcut = "@annotation(notifyEvent)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, NotifyEvent notifyEvent, Object result) {
        try {
            // SpEL 평가 컨텍스트 생성
            EvaluationContext context = createEvaluationContext(joinPoint, result);

            // 조건 확인
            if (!evaluateCondition(notifyEvent.conditionExpression(), context)) {
                log.debug("알림 조건 미충족: type={}", notifyEvent.type());
                return;
            }

            // 알림 정보 추출
            NotificationEventType eventType = notifyEvent.type();
            String title = evaluateExpression(notifyEvent.titleExpression(), context, String.class);
            String message = evaluateExpression(notifyEvent.messageExpression(), context, String.class);
            Long senderId = extractSenderId(notifyEvent.senderIdExpression(), context);
            Long courseId = evaluateExpression(notifyEvent.courseIdExpression(), context, Long.class);
            String relatedEntityType = notifyEvent.relatedEntityType();
            Long relatedEntityId = evaluateExpression(notifyEvent.relatedEntityIdExpression(), context, Long.class);
            String actionUrl = evaluateExpression(notifyEvent.actionUrlExpression(), context, String.class);

            // 기본값 처리
            if (!StringUtils.hasText(title)) {
                title = eventType.getCategory() + " 알림";
            }
            if (!StringUtils.hasText(message)) {
                message = eventType.getDefaultMessage();
            }

            // 배치 알림 또는 단일 알림 발송
            if (StringUtils.hasText(notifyEvent.recipientIdsExpression())) {
                publishBatchNotification(notifyEvent, context, eventType, senderId, courseId,
                        relatedEntityType, relatedEntityId, title, message, actionUrl);
            } else {
                publishSingleNotification(notifyEvent, context, eventType, senderId, courseId,
                        relatedEntityType, relatedEntityId, title, message, actionUrl);
            }

        } catch (Exception e) {
            log.error("알림 AOP 처리 중 오류 발생: type={}, error={}",
                    notifyEvent.type(), e.getMessage(), e);
        }
    }

    /**
     * 단일 알림 발송
     */
    private void publishSingleNotification(NotifyEvent notifyEvent, EvaluationContext context,
                                           NotificationEventType eventType, Long senderId, Long courseId,
                                           String relatedEntityType, Long relatedEntityId,
                                           String title, String message, String actionUrl) {
        Long recipientId = evaluateExpression(notifyEvent.recipientIdExpression(), context, Long.class);
        if (recipientId == null) {
            log.warn("수신자 ID가 null: type={}", eventType);
            return;
        }

        if (StringUtils.hasText(relatedEntityType) && relatedEntityId != null) {
            eventPublisher.publishWithEntity(eventType, senderId, recipientId,
                    relatedEntityType, relatedEntityId, courseId, title, message, actionUrl);
        } else if (courseId != null) {
            eventPublisher.publishForCourse(eventType, senderId, recipientId, courseId, title, message);
        } else {
            eventPublisher.publish(eventType, senderId, recipientId, title, message);
        }

        log.debug("단일 알림 발송 요청: type={}, recipientId={}", eventType, recipientId);
    }

    /**
     * 배치 알림 발송
     */
    @SuppressWarnings("unchecked")
    private void publishBatchNotification(NotifyEvent notifyEvent, EvaluationContext context,
                                          NotificationEventType eventType, Long senderId, Long courseId,
                                          String relatedEntityType, Long relatedEntityId,
                                          String title, String message, String actionUrl) {
        List<Long> recipientIds = evaluateExpression(
                notifyEvent.recipientIdsExpression(), context, List.class);

        if (recipientIds == null || recipientIds.isEmpty()) {
            log.warn("수신자 목록이 비어있음: type={}", eventType);
            return;
        }

        if (StringUtils.hasText(relatedEntityType) && relatedEntityId != null) {
            eventPublisher.publishBatchWithEntity(eventType, senderId, recipientIds,
                    relatedEntityType, relatedEntityId, courseId, title, message, actionUrl);
        } else {
            eventPublisher.publishBatch(eventType, senderId, recipientIds, courseId, title, message);
        }

        log.debug("배치 알림 발송 요청: type={}, recipientCount={}", eventType, recipientIds.size());
    }

    /**
     * SpEL 평가 컨텍스트 생성
     */
    private EvaluationContext createEvaluationContext(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, args, parameterNameDiscoverer
        );

        // 결과값 설정
        context.setVariable("result", result);

        // 인자 배열 설정
        context.setVariable("args", args);

        // 개별 인자 설정 (arg0, arg1, ...)
        for (int i = 0; i < args.length; i++) {
            context.setVariable("arg" + i, args[i]);
        }

        return context;
    }

    /**
     * SpEL 표현식 평가
     */
    private <T> T evaluateExpression(String expressionString, EvaluationContext context, Class<T> type) {
        if (!StringUtils.hasText(expressionString)) {
            return null;
        }

        try {
            Expression expression = expressionParser.parseExpression(expressionString);
            return expression.getValue(context, type);
        } catch (Exception e) {
            log.warn("SpEL 표현식 평가 실패: expression={}, error={}", expressionString, e.getMessage());
            return null;
        }
    }

    /**
     * 조건 표현식 평가
     */
    private boolean evaluateCondition(String conditionExpression, EvaluationContext context) {
        if (!StringUtils.hasText(conditionExpression)) {
            return true; // 조건 없으면 항상 true
        }

        Boolean result = evaluateExpression(conditionExpression, context, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 발신자 ID 추출
     */
    private Long extractSenderId(String senderIdExpression, EvaluationContext context) {
        // 표현식이 있으면 평가
        if (StringUtils.hasText(senderIdExpression)) {
            return evaluateExpression(senderIdExpression, context, Long.class);
        }

        // 기본값: 현재 인증된 사용자
        return getCurrentUserId();
    }

    /**
     * 현재 인증된 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof Long) {
                    return (Long) principal;
                }
                // UserDetails나 다른 타입인 경우 추가 처리 필요
                // 예: ((UserDetails) principal).getUsername() -> userId 조회
            }
        } catch (Exception e) {
            log.debug("현재 사용자 ID 조회 실패: {}", e.getMessage());
        }
        return null;
    }
}
