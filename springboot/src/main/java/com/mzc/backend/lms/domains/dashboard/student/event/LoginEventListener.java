package com.mzc.backend.lms.domains.dashboard.student.event;

import com.mzc.backend.lms.domains.dashboard.student.dto.TodayCourseDto;
import com.mzc.backend.lms.domains.dashboard.student.service.DailyLoginService;
import com.mzc.backend.lms.domains.dashboard.student.service.StudentDashboardService;
import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import com.mzc.backend.lms.domains.notification.queue.dto.NotificationMessage;
import com.mzc.backend.lms.domains.notification.queue.service.NotificationQueueService;
import com.mzc.backend.lms.domains.notification.repository.NotificationTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 로그인 이벤트 리스너
 * 오늘 첫 로그인 시 수업 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener {

    private static final String NOTIFICATION_TYPE_CODE = "DAILY_COURSE_REMINDER";

    private final DailyLoginService dailyLoginService;
    private final StudentDashboardService studentDashboardService;
    private final NotificationQueueService notificationQueueService;
    private final NotificationTypeRepository notificationTypeRepository;

    @Async
    @EventListener
    public void handleLoginSuccess(LoginSuccessEvent event) {
        // 학생만 처리
        if (!"STUDENT".equals(event.getUserType())) {
            return;
        }

        Long studentId = event.getUserId();

        // 오늘 첫 로그인 확인
        if (!dailyLoginService.checkAndMarkFirstLoginToday(studentId)) {
            return;
        }

        // 오늘 강의 조회
        List<TodayCourseDto> todayCourses = studentDashboardService.getTodayCourses(studentId);

        if (todayCourses.isEmpty()) {
            log.debug("오늘 수업 없음: studentId={}", studentId);
            return;
        }

        // 알림 발송
        sendTodayCoursesNotification(studentId, todayCourses);
    }

    private void sendTodayCoursesNotification(Long studentId, List<TodayCourseDto> courses) {
        NotificationType notificationType = notificationTypeRepository
                .findByTypeCode(NOTIFICATION_TYPE_CODE)
                .orElse(null);

        if (notificationType == null) {
            log.warn("알림 타입을 찾을 수 없음: typeCode={}", NOTIFICATION_TYPE_CODE);
            return;
        }

        String courseList = courses.stream()
                .map(c -> {
                    String schedule = c.getSchedule().isEmpty() ? "" :
                            c.getSchedule().get(0).getStartTime() + " - " +
                            c.getSchedule().get(0).getEndTime() + ", " +
                            c.getSchedule().get(0).getClassroom();
                    return String.format("- %s (%s)", c.getCourse().getCourseName(), schedule);
                })
                .collect(Collectors.joining("\n"));

        String message = String.format("오늘 %d개의 수업이 있습니다.\n%s",
                courses.size(), courseList);

        NotificationMessage notificationMessage = NotificationMessage.builder()
                .typeId(notificationType.getId())
                .senderId(null)
                .recipientId(studentId)
                .title("오늘의 수업 안내")
                .message(message)
                .actionUrl("/dashboard")
                .build();

        notificationQueueService.enqueue(notificationMessage);

        log.info("오늘의 수업 알림 발송: studentId={}, courseCount={}", studentId, courses.size());
    }
}
