package com.mzc.backend.lms.domains.user.student.service;

import com.mzc.backend.lms.domains.user.student.entity.StudentNumberSequence;
import com.mzc.backend.lms.domains.user.student.repository.StudentNumberSequenceRepository;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * 학번/교번 생성 서비스
 *
 * 학번/교번 형식: YYYYCCDDNNN
 * - YYYY: 입학년도 (학생) / 임용년도 (교수)
 * - CC: 단과대학 코드 (2자리)
 * - DD: 학과 코드 (2자리)
 * - NNN: 순번 (3자리)
 *
 * 로드밸런싱 환경(다중 인스턴스)에서도 안전하게 동작:
 * - 비관적 락(PESSIMISTIC_WRITE)으로 동시성 제어
 * - 시퀀스 미존재 시 기존 users 테이블 기반으로 초기값 설정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentNumberGenerator {

    private final StudentNumberSequenceRepository sequenceRepository;
    private final UserRepository userRepository;

    /**
     * 학번 생성
     * @param collegeId 단과대학 ID
     * @param departmentId 학과 ID
     * @return 생성된 학번
     */
    @Transactional
    public Long generateStudentNumber(Long collegeId, Long departmentId) {
        return generateNumber(collegeId, departmentId);
    }

    /**
     * 교번 생성
     * @param collegeId 단과대학 ID
     * @param departmentId 학과 ID
     * @return 생성된 교번
     */
    @Transactional
    public Long generateProfessorNumber(Long collegeId, Long departmentId) {
        return generateNumber(collegeId, departmentId);
    }

    /**
     * 번호 생성 공통 로직
     */
    private Long generateNumber(Long collegeId, Long departmentId) {
        Integer currentYear = Year.now().getValue();

        // 시퀀스 조회 또는 생성 (비관적 락)
        StudentNumberSequence sequence = sequenceRepository
                .findByYearAndCollegeAndDepartmentWithLock(currentYear, collegeId, departmentId)
                .orElseGet(() -> createSequenceWithExistingData(currentYear, collegeId, departmentId));

        // 다음 시퀀스 번호 가져오기
        Integer nextSequence = sequence.getNextSequence();
        sequenceRepository.save(sequence);

        // 번호 생성 (Long 타입)
        Long generatedNumber = Long.parseLong(String.format("%d%02d%02d%03d",
                currentYear,           // 년도 (4자리)
                collegeId,            // 단과대학 (2자리)
                departmentId,         // 학과 (2자리)
                nextSequence));        // 순번 (3자리)

        log.info("Generated number: {}", generatedNumber);
        return generatedNumber;
    }

    /**
     * 기존 users 테이블의 데이터를 고려하여 시퀀스 생성
     */
    private StudentNumberSequence createSequenceWithExistingData(
            Integer year, Long collegeId, Long departmentId) {
        // 학번 prefix 생성 (YYYYCCDD)
        String prefix = String.format("%d%02d%02d", year, collegeId, departmentId);

        // 기존 users 테이블에서 해당 패턴의 최대 순번 조회
        Integer maxExistingSequence = userRepository.findMaxSequenceByPrefix(prefix)
                .orElse(0);

        log.info("Creating new sequence for prefix={}, maxExistingSequence={}",
                prefix, maxExistingSequence);

        StudentNumberSequence newSequence = StudentNumberSequence.createWithInitialSequence(
                year, collegeId, departmentId, maxExistingSequence);
        return sequenceRepository.save(newSequence);
    }

    /**
     * 학번에서 정보 추출
     */
    public StudentNumberInfo parseStudentNumber(Long studentNumber) {
        if (studentNumber == null) {
            throw new IllegalArgumentException("학번이 null입니다.");
        }

        String numberStr = studentNumber.toString();
        if (numberStr.length() != 11) {
            throw new IllegalArgumentException("잘못된 학번 형식입니다.");
        }

        return StudentNumberInfo.builder()
                .year(Integer.parseInt(numberStr.substring(0, 4)))
                .collegeId(Long.parseLong(numberStr.substring(4, 6)))
                .departmentId(Long.parseLong(numberStr.substring(6, 8)))
                .sequence(Integer.parseInt(numberStr.substring(8, 11)))
                .build();
    }

    /**
     * 교번에서 정보 추출
     */
    public StudentNumberInfo parseProfessorNumber(Long professorNumber) {
        if (professorNumber == null) {
            throw new IllegalArgumentException("교번이 null입니다.");
        }

        return parseStudentNumber(professorNumber);
    }

    /**
     * 학번/교번 정보
     */
    @lombok.Builder
    @lombok.Getter
    public static class StudentNumberInfo {
        private final Integer year;
        private final Long collegeId;
        private final Long departmentId;
        private final Integer sequence;
    }
}