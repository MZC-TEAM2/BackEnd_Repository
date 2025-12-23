package com.mzc.backend.lms.domains.enrollment.listener;

import com.mzc.backend.lms.domains.enrollment.event.EnrollmentCancelledEvent;
import com.mzc.backend.lms.domains.enrollment.event.EnrollmentCreatedEvent;
import com.mzc.backend.lms.domains.notification.aop.event.NotificationEventType;
import com.mzc.backend.lms.domains.notification.aop.publisher.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 수강신청 관련 알림 이벤트 리스너
 * 트랜잭션 커밋 후 비동기로 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentNotificationListener {
	
	private final NotificationEventPublisher notificationEventPublisher;
	
	/**
	 * 수강신청 완료 이벤트 처리
	 * 학생 본인에게 수강신청 완료 알림 발송
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleEnrollmentCreated(EnrollmentCreatedEvent event) {
		log.debug("수강신청 완료 이벤트 수신: studentId={}, courseId={}, courseName={}",
				event.getStudentId(), event.getCourseId(), event.getCourseName());
		
		try {
			String message = String.format("%s (%s분반) 수강신청이 완료되었습니다.",
					event.getCourseName(), event.getSectionNumber());
			
			notificationEventPublisher.publishForCourse(
					NotificationEventType.ENROLLMENT_CREATED,
					null,  // 시스템 발송
					event.getStudentId(),
					event.getCourseId(),
					"수강신청 완료",
					message
			);
			
			log.info("수강신청 완료 알림 발송: studentId={}, courseId={}",
					event.getStudentId(), event.getCourseId());
		} catch (Exception e) {
			log.error("수강신청 완료 알림 발송 실패: studentId={}, courseId={}, error={}",
					event.getStudentId(), event.getCourseId(), e.getMessage(), e);
		}
	}
	
	/**
	 * 수강취소 완료 이벤트 처리
	 * 학생 본인에게 수강취소 완료 알림 발송
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleEnrollmentCancelled(EnrollmentCancelledEvent event) {
		log.debug("수강취소 완료 이벤트 수신: studentId={}, courseId={}, courseName={}",
				event.getStudentId(), event.getCourseId(), event.getCourseName());
		
		try {
			String message = String.format("%s (%s분반) 수강이 취소되었습니다.",
					event.getCourseName(), event.getSectionNumber());
			
			notificationEventPublisher.publishForCourse(
					NotificationEventType.ENROLLMENT_CANCELLED,
					null,  // 시스템 발송
					event.getStudentId(),
					event.getCourseId(),
					"수강취소 완료",
					message
			);
			
			log.info("수강취소 완료 알림 발송: studentId={}, courseId={}",
					event.getStudentId(), event.getCourseId());
		} catch (Exception e) {
			log.error("수강취소 완료 알림 발송 실패: studentId={}, courseId={}, error={}",
					event.getStudentId(), event.getCourseId(), e.getMessage(), e);
		}
	}
}
