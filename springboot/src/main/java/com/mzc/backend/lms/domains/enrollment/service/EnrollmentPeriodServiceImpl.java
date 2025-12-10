package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentPeriodResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 수강신청 기간 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentPeriodServiceImpl implements EnrollmentPeriodService {

    private final EnrollmentPeriodRepository enrollmentPeriodRepository;

    @Override
    public EnrollmentPeriodResponseDto getCurrentEnrollmentPeriod() {
        LocalDateTime now = LocalDateTime.now();

        // 모든 수강신청 기간 조회
        List<EnrollmentPeriod> periods = enrollmentPeriodRepository.findAll();

        // 현재 활성화된 수강신청 기간 찾기
        EnrollmentPeriod currentPeriod = periods.stream()
                .filter(period -> {
                    LocalDateTime startDatetime = period.getStartDatetime();
                    LocalDateTime endDatetime = period.getEndDatetime();
                    return !now.isBefore(startDatetime) && !now.isAfter(endDatetime);
                })
                .findFirst()
                .orElse(null);

        // 활성화된 기간이 없으면 null 반환
        if (currentPeriod == null) {
            return EnrollmentPeriodResponseDto.builder()
                    .isActive(false)
                    .currentPeriod(null)
                    .build();
        }

        // 학기 정보 가져오기
        AcademicTerm term = currentPeriod.getAcademicTerm();
        
        // 날짜를 LocalDateTime으로 변환 (시작일은 00:00:00, 종료일은 23:59:59)
        LocalDateTime startDateTime = currentPeriod.getStartDatetime();
        LocalDateTime endDateTime = currentPeriod.getEndDatetime();

        // 남은 시간 계산
        Duration remaining = Duration.between(now, endDateTime);
        long totalSeconds = remaining.getSeconds();
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        // 학기 이름 생성
        String termName = String.format("%d학년도 %s", term.getYear(), getTermTypeName(term.getTermType()));

        return EnrollmentPeriodResponseDto.builder()
                .isActive(true)
                .currentPeriod(EnrollmentPeriodResponseDto.CurrentPeriodDto.builder()
                        .id(currentPeriod.getId())
                        .term(EnrollmentPeriodResponseDto.TermDto.builder()
                                .year(term.getYear())
                                .termType(term.getTermType())
                                .termName(termName)
                                .build())
                        .periodName(currentPeriod.getPeriodName())
                        .startDatetime(startDateTime)
                        .endDatetime(endDateTime)
                        .targetYear(currentPeriod.getTargetYear() == 0 ? null : currentPeriod.getTargetYear())
                        .remainingTime(EnrollmentPeriodResponseDto.RemainingTimeDto.builder()
                                .days(days > 0 ? days : 0)
                                .hours(hours > 0 ? hours : 0)
                                .minutes(minutes > 0 ? minutes : 0)
                                .totalSeconds(totalSeconds > 0 ? totalSeconds : 0)
                                .build())
                        .build())
                .build();
    }

    /**
     * 학기 타입 이름 변환
     */
    private String getTermTypeName(String termType) {
        return switch (termType) {
            case "1" -> "1학기";
            case "2" -> "2학기";
            case "SUMMER" -> "여름학기";
            case "WINTER" -> "겨울학기";
            default -> termType;
        };
    }
}
