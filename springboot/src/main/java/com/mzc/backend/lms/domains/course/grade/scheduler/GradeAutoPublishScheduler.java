package com.mzc.backend.lms.domains.course.grade.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 성적 공개 스케줄러
 *
 * 정책 변경:
 * - 성적 공개는 "성적 공개 기간(GRADE_PUBLISH)"에 교수의 수동 요청(버튼)으로 수행
 * - 따라서 스케줄러는 자동 공개를 수행하지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GradeAutoPublishScheduler {

    /**
     * 매일 03:10 실행 (기존 스케줄러들과 충돌 회피)
     * - idempotent: 이미 공개된 성적은 재저장해도 동일 결과
     */
    @Scheduled(cron = "0 10 3 * * *")
    @SchedulerLock(name = "gradeAutoPublish", lockAtMostFor = "30m", lockAtLeastFor = "1m")
    public void publishGradesAfterGradeCalculationPeriod() {
        LocalDateTime now = LocalDateTime.now();
        log.info("성적 공개 스케줄러(자동 공개 비활성) 실행 at={}", now);
        // intentionally no-op (manual publish only)
    }
}


