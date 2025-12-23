package com.mzc.backend.lms.domains.notification.aop.annotation;

import com.mzc.backend.lms.domains.notification.aop.event.NotificationEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 알림 이벤트 발생을 위한 AOP 어노테이션
 * 메소드에 적용하면 실행 후 알림이 자동 발송됨
 *
 * <pre>
 * 사용 예시:
 * {@code
 * @NotifyEvent(
 *     type = NotificationEventType.ASSIGNMENT_CREATED,
 *     titleExpression = "'새 과제가 등록되었습니다'",
 *     messageExpression = "#result.title + ' 과제가 등록되었습니다.'"
 * )
 * public Assignment createAssignment(Long courseId, AssignmentDto dto) { ... }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotifyEvent {
	
	/**
	 * 알림 이벤트 타입
	 */
	NotificationEventType type();
	
	/**
	 * 알림 제목 SpEL 표현식
	 * 사용 가능한 변수: #result (메소드 반환값), #args (메소드 인자), #arg0, #arg1, ...
	 */
	String titleExpression() default "";
	
	/**
	 * 알림 메시지 SpEL 표현식
	 * 사용 가능한 변수: #result (메소드 반환값), #args (메소드 인자), #arg0, #arg1, ...
	 */
	String messageExpression() default "";
	
	/**
	 * 수신자 ID SpEL 표현식 (단일 수신자)
	 * 배치 알림이 아닌 경우 사용
	 */
	String recipientIdExpression() default "";
	
	/**
	 * 수신자 ID 목록 SpEL 표현식 (다중 수신자)
	 * 배치 알림인 경우 사용
	 */
	String recipientIdsExpression() default "";
	
	/**
	 * 발신자 ID SpEL 표현식
	 * 기본값: 현재 인증된 사용자
	 */
	String senderIdExpression() default "";
	
	/**
	 * 강의 ID SpEL 표현식 (강의 관련 알림인 경우)
	 */
	String courseIdExpression() default "";
	
	/**
	 * 관련 엔티티 타입 (예: ASSIGNMENT, NOTICE, SUBMISSION)
	 */
	String relatedEntityType() default "";
	
	/**
	 * 관련 엔티티 ID SpEL 표현식
	 */
	String relatedEntityIdExpression() default "";
	
	/**
	 * 액션 URL SpEL 표현식 (클릭 시 이동할 URL)
	 */
	String actionUrlExpression() default "";
	
	/**
	 * 조건 SpEL 표현식
	 * true일 때만 알림 발송 (기본: 항상 발송)
	 */
	String conditionExpression() default "";
	
	/**
	 * 비동기 처리 여부 (기본: true)
	 */
	boolean async() default true;
}
