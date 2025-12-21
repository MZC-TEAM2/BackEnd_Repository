package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.academy.repository.PeriodTypeRepository;
import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentPeriodResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 수강신청 기간 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentPeriodServiceImpl implements EnrollmentPeriodService {

    private final EnrollmentPeriodRepository enrollmentPeriodRepository;
    private final PeriodTypeRepository periodTypeRepository;

    @Override
    public EnrollmentPeriodResponseDto getCurrentPeriod(String typeCode) {
        LocalDateTime now = LocalDateTime.now();
        
        Optional<EnrollmentPeriod> currentPeriodOpt;
        
        // typeCode가 null이거나 빈 문자열이면 현재 활성화된 기간 중 하나를 반환
        if (typeCode == null || typeCode.trim().isEmpty()) {
            currentPeriodOpt = enrollmentPeriodRepository.findFirstActivePeriod(now);
        } else {
            String periodTypeCode = typeCode.toUpperCase();
            
            // 타입 코드 유효성 검증
            if (!periodTypeRepository.existsByTypeCode(periodTypeCode)) {
                throw new IllegalArgumentException("유효하지 않은 기간 타입 코드입니다: " + periodTypeCode);
            }

            // 현재 활성화된 기간 찾기
            currentPeriodOpt = enrollmentPeriodRepository
                    .findFirstActivePeriodByTypeCode(periodTypeCode, now);
        }

        // 활성화된 기간이 없으면 null 반환
        if (currentPeriodOpt.isEmpty()) {
            return EnrollmentPeriodResponseDto.builder()
                    .isActive(false)
                    .currentPeriod(null)
                    .build();
        }

        EnrollmentPeriod currentPeriod = currentPeriodOpt.get();

        // 학기 정보 가져오기
        AcademicTerm term = currentPeriod.getAcademicTerm();
        
        // PeriodType 정보 가져오기 (이미 fetch join으로 로딩됨)
        var periodType = currentPeriod.getPeriodType();
        
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
                                .termId(term.getId())
                                .year(term.getYear())
                                .termType(term.getTermType())
                                .termName(termName)
                                .build())
                        .periodName(currentPeriod.getPeriodName())
                        .periodType(periodType != null ? EnrollmentPeriodResponseDto.PeriodTypeDto.builder()
                                .id(periodType.getId())
                                .typeCode(periodType.getTypeCode())
                                .typeName(periodType.getTypeName())
                                .description(periodType.getDescription())
                                .build() : null)
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

    @Override
    @Deprecated
    public EnrollmentPeriodResponseDto getCurrentEnrollmentPeriod() {
        return getCurrentPeriod("ENROLLMENT");
    }
}
