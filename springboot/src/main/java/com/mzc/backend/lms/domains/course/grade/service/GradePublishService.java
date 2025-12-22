package com.mzc.backend.lms.domains.course.grade.service;

import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.academy.repository.PeriodTypeRepository;
import com.mzc.backend.lms.domains.assessment.entity.Assessment;
import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import com.mzc.backend.lms.domains.assessment.repository.AssessmentAttemptRepository;
import com.mzc.backend.lms.domains.assessment.repository.AssessmentRepository;
import com.mzc.backend.lms.domains.attendance.repository.WeekAttendanceRepository;
import com.mzc.backend.lms.domains.board.assignment.entity.Assignment;
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentRepository;
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentSubmissionRepository;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseGradingPolicy;
import com.mzc.backend.lms.domains.course.course.repository.CourseGradingPolicyRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository;
import com.mzc.backend.lms.domains.course.grade.entity.Grade;
import com.mzc.backend.lms.domains.course.grade.repository.GradeRepository;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 성적 산출 + 확정 + 공개 서비스
 * 정책:
 * - 성적 산출(CALCULATE): GRADE_CALCULATION 기간 "진행 중"에 교수(강의 단위)가 실행
 * - 성적 공개(PUBLISH): GRADE_CALCULATION 기간 "종료 후"에 교수(강의 단위)가 실행(또는 스케줄러 자동 실행)
 * - 채점 미완료(제출됐는데 score=null) 가 있으면 해당 강의는 스킵
 * - 재응시 없음
 * - 퀴즈 0개는 0 처리
 * - final_score는 정규화(B): (획득합/만점합)*100 을 각 항목별로 만든 후 가중합
 * - 출석은 A안: (출석완료 주차수/전체주차수)*100
 * - 결석 3회 이상이면 최종 등급은 무조건 F (publish 단계에서 강제)
 * - 등급은 상대평가(비율)로 배정 (A+,A0,A-,B+,B0,B-,C+,C0,C-,D+,D0,D-,F)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class GradePublishService {

    private static final String GRADE_CALCULATION = "GRADE_CALCULATION";
    private static final String GRADE_PUBLISH = "GRADE_PUBLISH";
    private static final long FAIL_ABSENCE_COUNT = 3L;

    private final EnrollmentPeriodRepository enrollmentPeriodRepository;
    private final PeriodTypeRepository periodTypeRepository;

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private final CourseGradingPolicyRepository courseGradingPolicyRepository;

    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;

    private final CourseWeekRepository courseWeekRepository;
    private final WeekAttendanceRepository weekAttendanceRepository;

    private final GradeRepository gradeRepository;

    /**
     * (스케줄러에서 호출) 성적 산출기간이 종료된 학기들에 대해 자동 공개 처리
     * - 산출(GRADED)된 강의만 공개(PUBLISHED)
     */
    @Transactional
    public void publishEndedTerms(LocalDateTime now) {
        // period_types에 코드가 없으면 아무것도 하지 않음
        if (!periodTypeRepository.existsByTypeCode(GRADE_CALCULATION)) {
            return;
        }

        // 공개는 "성적공개기간(GRADE_PUBLISH)"에만 허용
        // 스케줄러는 최근 시작/진행 중인 공개기간의 학기만 대상으로 처리 (과거 학기 반복 방지)
        List<EnrollmentPeriod> activePublishPeriods = enrollmentPeriodRepository.findFirstActivePeriodByTypeCode(GRADE_PUBLISH, now)
                .map(List::of)
                .orElseGet(List::of);
        // 단일 활성 기간만 조회하므로, 다른 학기까지 동시에 열리는 케이스가 필요하면 repository 확장 필요.
        for (EnrollmentPeriod p : activePublishPeriods) {
            Long termId = p.getAcademicTerm().getId();
            publishTerm(termId, now);
        }
    }

    /**
     * (수동 실행용) 특정 학기의 성적 공개
     * - 성적산출기간이 종료된 학기만 허용
     */
    @Transactional
    public void publishTermIfAllowed(Long academicTermId, LocalDateTime now) {
        if (academicTermId == null) {
            throw new IllegalArgumentException("academicTermId는 필수입니다.");
        }
        if (!periodTypeRepository.existsByTypeCode(GRADE_PUBLISH)) {
            throw new IllegalArgumentException("period_types에 GRADE_PUBLISH가 없습니다.");
        }
        boolean active = enrollmentPeriodRepository.existsActivePeriodByTypeCodeAndAcademicTermId(
                GRADE_PUBLISH, academicTermId, now);
        if (!active) {
            throw new IllegalArgumentException("성적 공개 기간이 아닙니다.");
        }
        publishTerm(academicTermId, now);
    }

    /**
     * (수동 실행용) 특정 강의의 성적 산출(점수 계산)
     * - 성적산출기간(GRADE_CALCULATION) 진행 중에만 허용
     */
    @Transactional
    public void calculateCourseIfAllowed(Long courseId, LocalDateTime now) {
        if (courseId == null) {
            throw new IllegalArgumentException("courseId는 필수입니다.");
        }
        if (!periodTypeRepository.existsByTypeCode(GRADE_CALCULATION)) {
            throw new IllegalArgumentException("period_types에 GRADE_CALCULATION이 없습니다.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. courseId=" + courseId));
        if (course.getAcademicTerm() == null || course.getAcademicTerm().getId() == null) {
            throw new IllegalArgumentException("강의에 학기 정보가 없습니다. courseId=" + courseId);
        }

        Long academicTermId = course.getAcademicTerm().getId();
        boolean active = enrollmentPeriodRepository.existsActivePeriodByTypeCodeAndAcademicTermId(
                GRADE_CALCULATION, academicTermId, now);
        if (!active) {
            throw new IllegalArgumentException("성적 산출기간이 아닙니다.");
        }

        calculateCourseIfReady(course, now);
    }

    /**
     * (수동 실행용) 특정 강의의 성적 공개
     * - 성적산출기간이 종료된 학기(강의가 속한 academic term)만 허용
     * - 산출(GRADED)된 성적만 공개(PUBLISHED)
     */
    @Transactional
    public void publishCourseIfAllowed(Long courseId, LocalDateTime now) {
        if (courseId == null) {
            throw new IllegalArgumentException("courseId는 필수입니다.");
        }
        if (!periodTypeRepository.existsByTypeCode(GRADE_PUBLISH)) {
            throw new IllegalArgumentException("period_types에 GRADE_PUBLISH가 없습니다.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. courseId=" + courseId));
        if (course.getAcademicTerm() == null || course.getAcademicTerm().getId() == null) {
            throw new IllegalArgumentException("강의에 학기 정보가 없습니다. courseId=" + courseId);
        }

        Long academicTermId = course.getAcademicTerm().getId();
        boolean active = enrollmentPeriodRepository.existsActivePeriodByTypeCodeAndAcademicTermId(
                GRADE_PUBLISH, academicTermId, now);
        if (!active) {
            throw new IllegalArgumentException("성적 공개 기간이 아닙니다.");
        }

        publishCourseIfReady(course, now, true);
    }

    @Transactional
    public void publishTerm(Long academicTermId, LocalDateTime now) {
        List<Course> courses = courseRepository.findByAcademicTermId(academicTermId);
        for (Course c : courses) {
            try {
                publishCourseIfReady(c, now, false);
            } catch (Exception e) {
                log.error("성적 자동 공개 처리 실패 courseId={}", c.getId(), e);
            }
        }
    }

    /**
     * 강의 단위로 성적 산출(점수 계산)만 수행
     * - grades.status = GRADED
     */
    @Transactional
    public void calculateCourseIfReady(Course course, LocalDateTime now) {
        Long courseId = course.getId();

        CourseGradingPolicy policy = courseGradingPolicyRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의 평가비율을 찾을 수 없습니다. courseId=" + courseId));

        // 평가 항목별 Assessment 목록/만점합
        List<Assessment> quizzes = assessmentRepository.findActiveByCourse(courseId, AssessmentType.QUIZ);
        List<Assessment> midterms = assessmentRepository.findActiveByCourse(courseId, AssessmentType.MIDTERM);
        List<Assessment> finals = assessmentRepository.findActiveByCourse(courseId, AssessmentType.FINAL);

        List<Long> quizIds = quizzes.stream().map(Assessment::getId).toList();
        List<Long> midtermIds = midterms.stream().map(Assessment::getId).toList();
        List<Long> finalIds = finals.stream().map(Assessment::getId).toList();

        // “채점 미완료 제출”이 하나라도 있으면 강의 전체 스킵
        if (!midtermIds.isEmpty() && assessmentAttemptRepository.existsUngradedSubmittedByAssessmentIds(midtermIds)) {
            log.info("성적 자동 공개 스킵(중간 채점 미완료) courseId={}", courseId);
            return;
        }
        if (!finalIds.isEmpty() && assessmentAttemptRepository.existsUngradedSubmittedByAssessmentIds(finalIds)) {
            log.info("성적 자동 공개 스킵(기말 채점 미완료) courseId={}", courseId);
            return;
        }

        // 과제 채점 미완료가 있으면 강의 전체 스킵
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();
        if (!assignmentIds.isEmpty() && assignmentSubmissionRepository.existsPendingGradingByAssignmentIds(assignmentIds)) {
            log.info("성적 자동 공개 스킵(과제 채점 미완료) courseId={}", courseId);
            return;
        }

        BigDecimal quizMax = sumTotalScore(quizzes);
        BigDecimal midtermMax = sumTotalScore(midterms);
        BigDecimal finalMax = sumTotalScore(finals);
        BigDecimal assignmentMax = sumAssignmentMaxScore(assignments);

        // 과제 점수(학생별 획득합) 배치 조회
        Map<Long, BigDecimal> assignmentEarnedMap = new HashMap<>();
        if (!assignmentIds.isEmpty()) {
            List<Object[]> rows = assignmentSubmissionRepository.sumGradedScoreByUserGroupByUserId(assignmentIds);
            for (Object[] r : rows) {
                Long userId = (Long) r[0];
                BigDecimal sum = (BigDecimal) r[1];
                assignmentEarnedMap.put(userId, sum != null ? sum : BigDecimal.ZERO);
            }
        }

        long totalWeeks = courseWeekRepository.countByCourseId(courseId);

        // 학생 엔티티를 함께 로딩해서 루프 내 lazy-loading(N+1) 가능성을 제거
        // 주의: 여기서는 학생 "이름"을 직접 조회/복호화하지 않는다(성적 산출에 불필요).
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithStudent(courseId);
        // 1) 수강생별 점수(원점수/정규화/최종점수) 전부 계산
        List<StudentGradeCalc> calcs = new ArrayList<>(enrollments.size());
        for (Enrollment e : enrollments) {
            Long studentId = e.getStudent().getStudentId();

            // 원점수 합(획득합)
            BigDecimal quizEarned = quizIds.isEmpty() ? BigDecimal.ZERO :
                    assessmentAttemptRepository.sumGradedScoreByUserAndAssessmentIds(studentId, quizIds);
            BigDecimal midtermEarned = midtermIds.isEmpty() ? BigDecimal.ZERO :
                    assessmentAttemptRepository.sumGradedScoreByUserAndAssessmentIds(studentId, midtermIds);
            BigDecimal finalEarned = finalIds.isEmpty() ? BigDecimal.ZERO :
                    assessmentAttemptRepository.sumGradedScoreByUserAndAssessmentIds(studentId, finalIds);

            // 출석 점수(A안): 0~100 정규화 점수
            BigDecimal attendanceNormalized = calcAttendanceNormalized(studentId, courseId, totalWeeks);

            // 과제 점수(획득합)
            BigDecimal assignmentEarned = assignmentEarnedMap.getOrDefault(studentId, BigDecimal.ZERO);

            // 각 항목 정규화(0~100)
            BigDecimal quizNormalized = normalizeTo100(quizEarned, quizMax);
            BigDecimal midtermNormalized = normalizeTo100(midtermEarned, midtermMax);
            BigDecimal finalNormalized = normalizeTo100(finalEarned, finalMax);
            BigDecimal assignmentNormalized = normalizeTo100(assignmentEarned, assignmentMax);

            BigDecimal finalScore = weightedFinalScore(
                    quizNormalized, policy.getQuiz(),
                    assignmentNormalized, policy.getAssignment(),
                    midtermNormalized, policy.getMidterm(),
                    finalNormalized, policy.getFinalExam(),
                    attendanceNormalized, policy.getAttendance()
            );

            calcs.add(StudentGradeCalc.builder()
                    .studentId(studentId)
                    .quizEarned(quizEarned)
                    .assignmentEarned(assignmentEarned)
                    .midtermEarned(midtermEarned)
                    .finalEarned(finalEarned)
                    .attendanceNormalized(attendanceNormalized)
                    .finalScore(finalScore)
                    .build());
        }

        // 2) grades GRADED upsert (등급은 publish 단계에서 부여)
        for (StudentGradeCalc c : calcs) {
            Grade grade = gradeRepository.findByCourseIdAndStudentId(courseId, c.getStudentId())
                    .orElse(Grade.builder()
                            .courseId(courseId)
                            .studentId(c.getStudentId())
                            .academicTermId(course.getAcademicTerm().getId())
                            .build());

            grade.markGraded(
                    c.getQuizEarned().setScale(2, RoundingMode.HALF_UP),
                    c.getAssignmentEarned().setScale(2, RoundingMode.HALF_UP),
                    c.getMidtermEarned().setScale(2, RoundingMode.HALF_UP),
                    c.getFinalEarned().setScale(2, RoundingMode.HALF_UP),
                    c.getAttendanceNormalized().setScale(2, RoundingMode.HALF_UP),
                    c.getFinalScore(),
                    now
            );
            gradeRepository.save(grade);
        }

        log.info("성적 산출 완료 courseId={}, gradedAt={}", courseId, now);
    }

    /**
     * 강의 단위로 “공개 가능하면” grades를 PUBLISHED로 확정/공개
     * - strict=true: 공개 불가 사유를 예외로 반환(강의 단위 수동 공개 API)
     * - strict=false: 공개 불가 사유는 스킵(스케줄러/학기 단위 수동 공개)
     */
    @Transactional
    public void publishCourseIfReady(Course course, LocalDateTime now, boolean strict) {
        Long courseId = course.getId();

        courseGradingPolicyRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의 평가비율을 찾을 수 없습니다. courseId=" + courseId));

        // 시험 채점 미완료가 있으면 공개 불가
        List<Assessment> midterms = assessmentRepository.findActiveByCourse(courseId, AssessmentType.MIDTERM);
        List<Assessment> finals = assessmentRepository.findActiveByCourse(courseId, AssessmentType.FINAL);
        List<Long> midtermIds = midterms.stream().map(Assessment::getId).toList();
        List<Long> finalIds = finals.stream().map(Assessment::getId).toList();
        if (!midtermIds.isEmpty() && assessmentAttemptRepository.existsUngradedSubmittedByAssessmentIds(midtermIds)) {
            String msg = "성적 공개 스킵/불가(중간 채점 미완료) courseId=" + courseId;
            if (strict) throw new IllegalArgumentException(msg);
            log.info(msg);
            return;
        }
        if (!finalIds.isEmpty() && assessmentAttemptRepository.existsUngradedSubmittedByAssessmentIds(finalIds)) {
            String msg = "성적 공개 스킵/불가(기말 채점 미완료) courseId=" + courseId;
            if (strict) throw new IllegalArgumentException(msg);
            log.info(msg);
            return;
        }

        // 과제 채점 미완료가 있으면 공개 불가
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();
        if (!assignmentIds.isEmpty() && assignmentSubmissionRepository.existsPendingGradingByAssignmentIds(assignmentIds)) {
            String msg = "성적 공개 스킵/불가(과제 채점 미완료) courseId=" + courseId;
            if (strict) throw new IllegalArgumentException(msg);
            log.info(msg);
            return;
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithStudent(courseId);
        List<Long> studentIds = enrollments.stream().map(e -> e.getStudent().getStudentId()).toList();
        if (studentIds.isEmpty()) {
            log.info("성적 공개 스킵(수강생 없음) courseId={}", courseId);
            return;
        }

        List<Grade> grades = gradeRepository.findByCourseIdAndStudentIdIn(courseId, studentIds);
        if (grades.size() != studentIds.size()) {
            String msg = "성적 공개 스킵/불가(산출되지 않은 수강생 존재: calculate 필요) courseId=" + courseId;
            if (strict) throw new IllegalArgumentException(msg);
            log.info(msg);
            return;
        }
        for (Grade g : grades) {
            if (g.getStatus() != com.mzc.backend.lms.domains.course.grade.enums.GradeStatus.GRADED) {
                String msg = "성적 공개 스킵/불가(GRADED 상태가 아님: calculate 필요) courseId=" + courseId;
                if (strict) throw new IllegalArgumentException(msg);
                log.info(msg);
                return;
            }
            if (g.getFinalScore() == null) {
                String msg = "성적 공개 스킵/불가(finalScore가 없음: calculate 필요) courseId=" + courseId;
                if (strict) throw new IllegalArgumentException(msg);
                log.info(msg);
                return;
            }
        }

        // 결석 3회 이상이면 무조건 F
        // - 상대평가 "대상 집단"에서 제외한 뒤(=F 확정), 남은 학생들끼리 상대평가를 재계산한다.
        long totalWeeks = courseWeekRepository.countByCourseId(courseId);
        Map<Long, String> gradeMap = new HashMap<>(Math.max(16, studentIds.size() * 2));

        Map<Long, Integer> completedMap = new HashMap<>();
        if (totalWeeks > 0) {
            List<Object[]> stats = weekAttendanceRepository.getAttendanceStatsByCourse(courseId);
            for (Object[] r : stats) {
                Long sid = (Long) r[0];
                Number completed = (Number) r[2]; // SUM(CASE...) 결과
                completedMap.put(sid, completed == null ? 0 : completed.intValue());
            }
        }

        List<StudentGradeCalc> eligibleCalcs = new ArrayList<>();
        for (Grade g : grades) {
            long completed = (totalWeeks > 0) ? completedMap.getOrDefault(g.getStudentId(), 0) : 0;
            long absences = (totalWeeks > 0) ? Math.max(0, totalWeeks - completed) : 0;
            if (totalWeeks > 0 && absences >= FAIL_ABSENCE_COUNT) {
                gradeMap.put(g.getStudentId(), "F");
                continue;
            }
            eligibleCalcs.add(StudentGradeCalc.builder()
                    .studentId(g.getStudentId())
                    .finalScore(g.getFinalScore())
                    .build());
        }

        // 상대평가 등급 배정(finalScore 기반) - 결석 F 확정자를 제외한 집단
        Map<Long, String> eligibleMap = assignRelativeGrades(eligibleCalcs);
        gradeMap.putAll(eligibleMap);

        for (Grade g : grades) {
            String finalGrade = gradeMap.get(g.getStudentId());
            g.publishFinalGrade(finalGrade, now);
            gradeRepository.save(g);
        }

        log.info("성적 공개 완료 courseId={}, publishedAt={}", courseId, now);
    }

    private BigDecimal sumTotalScore(List<Assessment> list) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Assessment a : list) {
            if (a.getTotalScore() != null) sum = sum.add(a.getTotalScore());
        }
        return sum;
    }

    private BigDecimal sumAssignmentMaxScore(List<Assignment> list) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Assignment a : list) {
            if (a.getMaxScore() != null) sum = sum.add(a.getMaxScore());
        }
        return sum;
    }

    private BigDecimal calcAttendanceNormalized(Long studentId, Long courseId, long totalWeeks) {
        if (totalWeeks <= 0) return BigDecimal.ZERO;
        int completed = weekAttendanceRepository.countCompletedByStudentAndCourse(studentId, courseId);
        BigDecimal num = new BigDecimal(completed);
        BigDecimal den = new BigDecimal(totalWeeks);
        return num.divide(den, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    /**
     * 획득합/만점합을 0~100으로 정규화
     * - 만점합이 0이면 0 처리
     */
    private BigDecimal normalizeTo100(BigDecimal earned, BigDecimal max) {
        if (earned == null) earned = BigDecimal.ZERO;
        if (max == null || max.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal v = earned.divide(max, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal weightedFinalScore(BigDecimal quizNorm, int quizW,
                                         BigDecimal assignmentNorm, int assignmentW,
                                         BigDecimal midtermNorm, int midtermW,
                                         BigDecimal finalNorm, int finalW,
                                         BigDecimal attendanceNorm, int attendanceW) {
        // (norm * weight) / 100
        BigDecimal sum = BigDecimal.ZERO;
        sum = sum.add(weighted(quizNorm, quizW));
        sum = sum.add(weighted(assignmentNorm, assignmentW));
        sum = sum.add(weighted(midtermNorm, midtermW));
        sum = sum.add(weighted(finalNorm, finalW));
        sum = sum.add(weighted(attendanceNorm, attendanceW));
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal weighted(BigDecimal norm, int w) {
        if (norm == null) return BigDecimal.ZERO;
        if (w <= 0) return BigDecimal.ZERO;
        return norm.multiply(new BigDecimal(w)).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
    }

    /**
     * 상대평가 등급 배정
     * - 기본 비율(총 100%):
     *   A+:10%, A0:15%, A-:5%,
     *   B+:10%, B0:20%, B-:10%,
     *   C+:5%,  C0:10%, C-:5%,
     *   D+:3%,  D0:4%,  D-:3%,
     *   F: 나머지
     *
     * 동점 처리:
     * - 등급 경계에서 동점이 발생하면, 해당 점수대는 상위 등급을 주지 않고 한 단계 아래 등급으로 내림
     *   (예: A0 컷에서 동점이면 그 점수대는 A0 대신 A-로 처리)
     */
    private Map<Long, String> assignRelativeGrades(List<StudentGradeCalc> calcs) {
        int n = calcs.size();
        Map<Long, String> out = new HashMap<>(Math.max(16, n * 2));
        if (n == 0) return out;

        List<StudentGradeCalc> sorted = calcs.stream()
                .sorted(Comparator
                        .comparing(StudentGradeCalc::getFinalScore, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(StudentGradeCalc::getStudentId))
                .toList();

        List<GradeBucket> buckets = List.of(
                new GradeBucket("A+", 0.10),
                new GradeBucket("A0", 0.15),
                new GradeBucket("A-", 0.05),
                new GradeBucket("B+", 0.10),
                new GradeBucket("B0", 0.20),
                new GradeBucket("B-", 0.10),
                new GradeBucket("C+", 0.05),
                new GradeBucket("C0", 0.10),
                new GradeBucket("C-", 0.05),
                new GradeBucket("D+", 0.03),
                new GradeBucket("D0", 0.04),
                new GradeBucket("D-", 0.03)
        );

        int idx = 0;
        for (int bi = 0; bi < buckets.size(); bi++) {
            GradeBucket b = buckets.get(bi);
            String currentLabel = b.label();
            String nextLabel = (bi + 1 < buckets.size()) ? buckets.get(bi + 1).label() : "F";

            int cnt = (int) Math.floor(n * b.ratio());
            int startIdx = idx;
            for (int k = 0; k < cnt && idx < n; k++, idx++) {
                out.put(sorted.get(idx).getStudentId(), currentLabel);
            }

            // 등급 경계 동점 처리:
            // 현재 버킷의 마지막 점수 == 다음 점수이면, 그 점수대(현재 버킷에 들어간 사람들)는 nextLabel로 내림
            if (idx < n && idx > startIdx) {
                BigDecimal lastScore = sorted.get(idx - 1).getFinalScore();
                BigDecimal nextScore = sorted.get(idx).getFinalScore();
                if (lastScore != null && nextScore != null && lastScore.compareTo(nextScore) == 0) {
                    BigDecimal tieScore = lastScore;
                    for (int j = idx - 1; j >= startIdx; j--) {
                        BigDecimal s = sorted.get(j).getFinalScore();
                        if (s == null || s.compareTo(tieScore) != 0) {
                            break;
                        }
                        out.put(sorted.get(j).getStudentId(), nextLabel);
                    }
                }
            }
        }

        // 남은 인원은 F
        while (idx < n) {
            out.put(sorted.get(idx).getStudentId(), "F");
            idx++;
        }

        // 동점 처리(강제 정규화):
        // 같은 finalScore를 가진 학생들은 "가장 낮은 등급"으로 통일
        // (경계 동점에서 상위 등급을 주지 않는 정책을 일반화)
        Map<BigDecimal, String> worstByScore = new HashMap<>();
        Map<String, Integer> rank = gradeRank();
        for (StudentGradeCalc s : sorted) {
            BigDecimal score = s.getFinalScore();
            String g = out.get(s.getStudentId());
            if (g == null) continue;
            String prev = worstByScore.get(score);
            if (prev == null) {
                worstByScore.put(score, g);
            } else {
                int prevRank = rank.getOrDefault(prev, 999);
                int curRank = rank.getOrDefault(g, 999);
                if (curRank > prevRank) {
                    worstByScore.put(score, g);
                }
            }
        }
        for (StudentGradeCalc s : sorted) {
            BigDecimal score = s.getFinalScore();
            String worst = worstByScore.get(score);
            if (worst != null) {
                out.put(s.getStudentId(), worst);
            }
        }

        return out;
    }

    private record GradeBucket(String label, double ratio) {}

    @lombok.Getter
    @lombok.Builder
    private static class StudentGradeCalc {
        private Long studentId;
        private BigDecimal quizEarned;
        private BigDecimal assignmentEarned;
        private BigDecimal midtermEarned;
        private BigDecimal finalEarned;
        private BigDecimal attendanceNormalized;
        private BigDecimal finalScore;
    }

    private Map<String, Integer> gradeRank() {
        // 낮을수록 "좋은 등급"
        Map<String, Integer> r = new HashMap<>();
        String[] order = {"A+", "A0", "A-", "B+", "B0", "B-", "C+", "C0", "C-", "D+", "D0", "D-", "F"};
        for (int i = 0; i < order.length; i++) {
            r.put(order[i], i);
        }
        return r;
    }

}


