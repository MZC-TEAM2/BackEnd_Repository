package com.mzc.backend.lms.domains.academy.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.repository.AcademicTermRepository;
import com.mzc.backend.lms.domains.course.course.dto.AcademicTermDto;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentAcademicTermService {

    private final EnrollmentRepository enrollmentRepository;
    private final AcademicTermRepository academicTermRepository;

    @Transactional(readOnly = true)
    public List<AcademicTermDto> listMyAcademicTerms(Long studentId) {
        Objects.requireNonNull(studentId, "studentId");

        List<AcademicTerm> terms = enrollmentRepository.findDistinctAcademicTermsByStudentId(studentId);
        return terms.stream()
                .sorted(Comparator.comparing(AcademicTerm::getId, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(t -> AcademicTermDto.builder()
                        .id(t.getId())
                        .year(t.getYear())
                        .termType(t.getTermType())
                        .startDate(t.getStartDate())
                        .endDate(t.getEndDate())
                        .build())
                .toList();
    }

    /**
     * 현재 학기 조회
     * - academic_terms의 start_date/end_date 범위로 현재 날짜에 해당하는 학기를 반환
     */
    @Transactional(readOnly = true)
    public AcademicTermDto getCurrentAcademicTerm(LocalDateTime now) {
        // now는 기존 호출부 호환을 위해 유지, 날짜 기준으로 학기를 찾는다.
        AcademicTerm t = academicTermRepository.findCurrentTerms(LocalDate.from(now)).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("현재 날짜에 해당하는 학기가 없습니다."));

        return AcademicTermDto.builder()
                .id(t.getId())
                .year(t.getYear())
                .termType(t.getTermType())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .build();
    }
}


