package com.mzc.backend.lms.domains.course.notice.listener;

import com.mzc.backend.lms.domains.course.notice.event.CourseNoticeCreatedEvent;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.notification.aop.event.NotificationEventType;
import com.mzc.backend.lms.domains.notification.aop.publisher.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 강의 공지사항 관련 알림 이벤트 리스너
 * 트랜잭션 커밋 후 비동기로 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseNoticeNotificationListener {

    private final NotificationEventPublisher notificationEventPublisher;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * 공지사항 생성 이벤트 처리
     * 해당 강의의 모든 수강생에게 알림 발송
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseNoticeCreated(CourseNoticeCreatedEvent event) {
        log.debug("공지사항 생성 이벤트 수신: courseId={}, noticeId={}, title={}",
                event.getCourseId(), event.getNoticeId(), event.getNoticeTitle());

        try {
            List<Long> studentIds = enrollmentRepository.findStudentIdsByCourseId(event.getCourseId());

            if (studentIds.isEmpty()) {
                log.debug("수강생이 없어 알림을 발송하지 않습니다: courseId={}", event.getCourseId());
                return;
            }

            String title = "새 공지사항";
            String message = String.format("[%s] %s", event.getCourseName(), event.getNoticeTitle());
            String actionUrl = String.format("/courses/%d/notices/%d", event.getCourseId(), event.getNoticeId());

            notificationEventPublisher.publishBatchWithEntity(
                    NotificationEventType.COURSE_NOTICE_CREATED,
                    event.getProfessorId(),
                    studentIds,
                    "COURSE_NOTICE",
                    event.getNoticeId(),
                    event.getCourseId(),
                    title,
                    message,
                    actionUrl
            );

            log.info("공지사항 알림 발송 완료: courseId={}, noticeId={}, recipientCount={}",
                    event.getCourseId(), event.getNoticeId(), studentIds.size());
        } catch (Exception e) {
            log.error("공지사항 알림 발송 실패: courseId={}, noticeId={}, error={}",
                    event.getCourseId(), event.getNoticeId(), e.getMessage(), e);
        }
    }
}
