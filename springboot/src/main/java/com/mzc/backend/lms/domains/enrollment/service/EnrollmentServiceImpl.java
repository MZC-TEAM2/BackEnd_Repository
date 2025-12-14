package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.course.constants.CourseConstants;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.subject.entity.SubjectPrerequisites;
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository;
import com.mzc.backend.lms.domains.enrollment.dto.*;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import com.mzc.backend.lms.domains.enrollment.repository.CourseCartRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.views.UserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 수강신청 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseCartRepository courseCartRepository;
    private final EnrollmentPeriodRepository enrollmentPeriodRepository;
    private final SubjectPrerequisitesRepository subjectPrerequisitesRepository;
    private final StudentRepository studentRepository;
    private final UserViewService userViewService;

    private static final int MAX_CREDITS_PER_TERM = 21; // 학기당 최대 학점

    @Override
    public EnrollmentBulkResponseDto enrollBulk(CourseIdsRequestDto request, String studentId) {
        // 1. 수강신청 기간 체크
        if (!isEnrollmentPeriodActive()) {
            throw new IllegalArgumentException("수강신청 기간이 아닙니다.");
        }

        Long studentIdLong = Long.parseLong(studentId);

        // 2. 학생 정보 조회
        Student student = studentRepository.findById(studentIdLong)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 3. 과목 존재 여부 체크
        List<Long> courseIds = request.getCourseIds();
        if (courseIds == null || courseIds.isEmpty()) {
            throw new IllegalArgumentException("강의 ID 목록이 비어있습니다.");
        }

        // 존재하지 않는 강의 체크 (락 없이 먼저 확인)
        List<Course> courses = courseRepository.findAllById(courseIds);
        if (courses.size() != courseIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 강의가 포함되어 있습니다.");
        }

        // 4. 기존 수강신청 정보 조회
        List<Enrollment> existingEnrollments = enrollmentRepository.findByStudentId(studentIdLong);
        Set<Long> enrolledCourseIds = existingEnrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());
        Set<Long> enrolledSubjectIds = existingEnrollments.stream()
                .map(enrollment -> enrollment.getCourse().getSubject().getId())
                .collect(Collectors.toSet());

        // 5. 현재 수강 학점 계산
        int currentCredits = existingEnrollments.stream()
                .mapToInt(enrollment -> enrollment.getCourse().getSubject().getCredits())
                .sum();

        // 6. 각 강의에 대해 개별 검증 및 수강신청 처리
        List<EnrollmentBulkResponseDto.SucceededEnrollmentDto> succeeded = new ArrayList<>();
        List<EnrollmentBulkResponseDto.FailedEnrollmentDto> failed = new ArrayList<>();
        int enrolledCredits = 0;

        LocalDateTime now = LocalDateTime.now();

        for (Long courseId : courseIds) {
            try {
                // 비관적 락으로 Course 조회 (동시성 제어)
                Course course = courseRepository.findByIdWithLock(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다: " + courseId));

                // 정원 체크 (락이 걸린 상태에서 체크)
                if (course.getCurrentStudents() >= course.getMaxStudents()) {
                    EnrollmentBulkResponseDto.FailedEnrollmentDto failedDto = createFailedDto(
                            course, "COURSE_FULL", "수강 정원이 마감되었습니다");
                    failed.add(failedDto);
                    continue;
                }

                // 개별 검증 (정원 제외, 이미 체크함)
                String errorCode = validateEnrollmentWithoutCapacity(course, studentIdLong, existingEnrollments, 
                        enrolledCourseIds, enrolledSubjectIds, currentCredits + enrolledCredits);
                
                if (errorCode != null) {
                    // 실패 처리
                    EnrollmentBulkResponseDto.FailedEnrollmentDto failedDto = createFailedDto(
                            course, errorCode, getErrorMessage(errorCode, course));
                    failed.add(failedDto);
                    continue;
                }

                // 수강신청 처리 (락이 걸린 상태에서 원자적으로 처리)
                Enrollment enrollment = Enrollment.builder()
                        .student(student)
                        .course(course)
                        .enrolledAt(now)
                        .build();

                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
                
                // 정원 증가 (락이 걸린 상태에서 안전하게 증가)
                course.setCurrentStudents(course.getCurrentStudents() + 1);
                courseRepository.save(course);

                // 장바구니에서 제거 (있는 경우)
                courseCartRepository.findByStudentIdAndCourseId(studentIdLong, courseId)
                        .ifPresent(courseCartRepository::delete);

                // 성공 처리
                EnrollmentBulkResponseDto.SucceededEnrollmentDto succeededDto = 
                        EnrollmentBulkResponseDto.SucceededEnrollmentDto.builder()
                                .enrollmentId(savedEnrollment.getId())
                                .courseId(course.getId())
                                .courseCode(course.getSubject().getSubjectCode())
                                .courseName(course.getSubject().getSubjectName())
                                .section(course.getSectionNumber())
                                .credits(course.getSubject().getCredits())
                                .enrolledAt(savedEnrollment.getEnrolledAt())
                                .build();
                succeeded.add(succeededDto);
                enrolledCredits += course.getSubject().getCredits();

                // 기존 목록 업데이트
                existingEnrollments.add(savedEnrollment);
                enrolledCourseIds.add(course.getId());
                enrolledSubjectIds.add(course.getSubject().getId());

            } catch (IllegalArgumentException e) {
                log.warn("수강신청 검증 실패: courseId={}, error={}", courseId, e.getMessage());
                // Course를 다시 조회하여 실패 DTO 생성
                courseRepository.findById(courseId).ifPresent(course -> {
                    EnrollmentBulkResponseDto.FailedEnrollmentDto failedDto = createFailedDto(
                            course, "VALIDATION_ERROR", e.getMessage());
                    failed.add(failedDto);
                });
            } catch (Exception e) {
                log.error("수강신청 처리 중 오류 발생: courseId={}, error={}", courseId, e.getMessage(), e);
                // Course를 다시 조회하여 실패 DTO 생성
                courseRepository.findById(courseId).ifPresent(course -> {
                    EnrollmentBulkResponseDto.FailedEnrollmentDto failedDto = createFailedDto(
                            course, "ENROLLMENT_ERROR", e.getMessage());
                    failed.add(failedDto);
                });
            }
        }

        // 7. 응답 생성
        int totalAttempted = courseIds.size();
        int successCount = succeeded.size();
        int failedCount = failed.size();
        int totalCredits = currentCredits + enrolledCredits;

        EnrollmentBulkResponseDto.SummaryDto summary = EnrollmentBulkResponseDto.SummaryDto.builder()
                .totalAttempted(totalAttempted)
                .successCount(successCount)
                .failedCount(failedCount)
                .enrolledCredits(enrolledCredits)
                .totalCredits(totalCredits)
                .build();

        return EnrollmentBulkResponseDto.builder()
                .summary(summary)
                .succeeded(succeeded)
                .failed(failed)
                .build();
    }

    /**
     * 수강신청 검증 (정원 체크 제외, 락이 걸린 상태에서 별도로 체크)
     * @return 에러 코드 (null이면 성공)
     */
    private String validateEnrollmentWithoutCapacity(Course course, Long studentId, 
                                                    List<Enrollment> existingEnrollments,
                                                    Set<Long> enrolledCourseIds,
                                                    Set<Long> enrolledSubjectIds,
                                                    int currentTotalCredits) {
        // 1. 이미 수강신청했는지 체크
        if (enrolledCourseIds.contains(course.getId())) {
            return "ALREADY_ENROLLED";
        }

        // 2. 동일 과목 다른 분반 체크
        Long subjectId = course.getSubject().getId();
        if (enrolledSubjectIds.contains(subjectId)) {
            return "DUPLICATE_SUBJECT";
        }

        // 3. 시간표 충돌 체크
        List<CourseSchedule> existingSchedules = existingEnrollments.stream()
                .flatMap(enrollment -> enrollment.getCourse().getSchedules().stream())
                .collect(Collectors.toList());

        for (CourseSchedule newSchedule : course.getSchedules()) {
            for (CourseSchedule existingSchedule : existingSchedules) {
                if (hasScheduleConflict(newSchedule, existingSchedule)) {
                    return "TIME_CONFLICT";
                }
            }
        }

        // 4. 선수과목 이수 여부 체크
        List<SubjectPrerequisites> prerequisites = subjectPrerequisitesRepository.findBySubjectId(subjectId);
        for (SubjectPrerequisites prerequisite : prerequisites) {
            if (prerequisite.getIsMandatory()) {
                Long prerequisiteSubjectId = prerequisite.getPrerequisite().getId();
                boolean hasPrerequisite = existingEnrollments.stream()
                        .anyMatch(enrollment -> enrollment.getCourse().getSubject().getId().equals(prerequisiteSubjectId));
                
                if (!hasPrerequisite) {
                    return "PREREQUISITE_NOT_MET";
                }
            }
        }

        // 5. 학점 제한 체크
        int newCredits = course.getSubject().getCredits();
        if (currentTotalCredits + newCredits > MAX_CREDITS_PER_TERM) {
            return "CREDIT_LIMIT_EXCEEDED";
        }

        return null; // 검증 통과
    }

    /**
     * 시간표 충돌 확인
     */
    private boolean hasScheduleConflict(CourseSchedule schedule1, CourseSchedule schedule2) {
        if (!schedule1.getDayOfWeek().equals(schedule2.getDayOfWeek())) {
            return false;
        }

        LocalTime start1 = schedule1.getStartTime();
        LocalTime end1 = schedule1.getEndTime();
        LocalTime start2 = schedule2.getStartTime();
        LocalTime end2 = schedule2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * 실패 DTO 생성
     */
    private EnrollmentBulkResponseDto.FailedEnrollmentDto createFailedDto(
            Course course, String errorCode, String message) {
        return EnrollmentBulkResponseDto.FailedEnrollmentDto.builder()
                .courseId(course.getId())
                .courseCode(course.getSubject().getSubjectCode())
                .courseName(course.getSubject().getSubjectName())
                .section(course.getSectionNumber())
                .errorCode(errorCode)
                .message(message)
                // enrollment 필드 제거
                .build();
    }

    /**
     * 에러 메시지 생성
     */
    private String getErrorMessage(String errorCode, Course course) {
        return switch (errorCode) {
            case "ALREADY_ENROLLED" -> "이미 수강신청했습니다";
            case "DUPLICATE_SUBJECT" -> "동일 과목 다른 분반 이미 신청";
            case "COURSE_FULL" -> "수강 정원이 마감되었습니다";
            case "TIME_CONFLICT" -> "시간표가 충돌합니다";
            case "PREREQUISITE_NOT_MET" -> "선수과목을 이수하지 않았습니다";
            case "CREDIT_LIMIT_EXCEEDED" -> "학점 제한을 초과합니다";
            default -> "수강신청에 실패했습니다";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public MyEnrollmentsResponseDto getMyEnrollments(String studentId, Long enrollmentPeriodId) {
        Long studentIdLong = Long.parseLong(studentId);

        // 1. 수강신청 기간 조회
        EnrollmentPeriod enrollmentPeriod;
        if (enrollmentPeriodId != null) {
            enrollmentPeriod = enrollmentPeriodRepository.findById(enrollmentPeriodId)
                    .orElseThrow(() -> new IllegalArgumentException("수강신청 기간을 찾을 수 없습니다: " + enrollmentPeriodId));
        } else {
            // 현재 활성화된 기간 조회
            LocalDateTime now = LocalDateTime.now();
            enrollmentPeriod = enrollmentPeriodRepository.findAll().stream()
                    .filter(period -> {
                        LocalDateTime start = period.getStartDatetime();
                        LocalDateTime end = period.getEndDatetime();
                        return !now.isBefore(start) && !now.isAfter(end);
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("현재 활성화된 수강신청 기간이 없습니다."));
        }

        AcademicTerm academicTerm = enrollmentPeriod.getAcademicTerm();

        // 2. 학생의 수강신청 목록 조회 (해당 학기 강의만 필터링)
        List<Enrollment> allEnrollments = enrollmentRepository.findByStudentId(studentIdLong);
        List<Enrollment> termEnrollments = allEnrollments.stream()
                .filter(enrollment -> enrollment.getCourse().getAcademicTerm().getId().equals(academicTerm.getId()))
                .collect(Collectors.toList());

        // 3. 학기 정보 구성
        String termName = String.format("%d학년도 %s", academicTerm.getYear(), getTermTypeName(academicTerm.getTermType()));
        MyEnrollmentsResponseDto.TermDto termDto = MyEnrollmentsResponseDto.TermDto.builder()
                .id(academicTerm.getId())
                .year(academicTerm.getYear())
                .termType(academicTerm.getTermType())
                .termName(termName)
                .build();

        // 4. 요약 정보 구성
        int totalCourses = termEnrollments.size();
        int totalCredits = termEnrollments.stream()
                .mapToInt(enrollment -> enrollment.getCourse().getSubject().getCredits())
                .sum();
        int remainingCredits = MAX_CREDITS_PER_TERM - totalCredits;

        MyEnrollmentsResponseDto.SummaryDto summaryDto = MyEnrollmentsResponseDto.SummaryDto.builder()
                .totalCourses(totalCourses)
                .totalCredits(totalCredits)
                .maxCredits(MAX_CREDITS_PER_TERM)
                .remainingCredits(Math.max(0, remainingCredits))
                .build();

        // 5. 취소 가능 여부 확인
        boolean canCancel = canCancelEnrollment();

        // 6. 수강신청 목록 구성
        List<MyEnrollmentsResponseDto.EnrollmentItemDto> enrollmentItems = termEnrollments.stream()
                .map(enrollment -> convertToEnrollmentItemDto(enrollment, canCancel))
                .collect(Collectors.toList());

        return MyEnrollmentsResponseDto.builder()
                .term(termDto)
                .summary(summaryDto)
                .enrollments(enrollmentItems)
                .build();
    }

    /**
     * Enrollment를 EnrollmentItemDto로 변환
     */
    private MyEnrollmentsResponseDto.EnrollmentItemDto convertToEnrollmentItemDto(
            Enrollment enrollment, boolean canCancel) {
        Course course = enrollment.getCourse();

        // 교수 이름 조회
        String professorName = userViewService.getUserName(
                course.getProfessor().getProfessorId().toString());

        // 스케줄 변환
        List<ScheduleDto> schedules = course.getSchedules().stream()
                .map(this::convertToScheduleDto)
                .sorted(Comparator.comparing(ScheduleDto::getDayOfWeek)
                        .thenComparing(ScheduleDto::getStartTime))
                .collect(Collectors.toList());

        // CourseType 변환
        CourseTypeDto courseTypeDto = convertToCourseTypeDto(course.getSubject().getCourseType());

        // CourseInfo 구성
        MyEnrollmentsResponseDto.CourseInfoDto courseInfo = MyEnrollmentsResponseDto.CourseInfoDto.builder()
                .id(course.getId())
                .courseCode(course.getSubject().getSubjectCode())
                .courseName(course.getSubject().getSubjectName())
                .section(course.getSectionNumber())
                .credits(course.getSubject().getCredits())
                .courseType(courseTypeDto)
                .currentStudents(course.getCurrentStudents())  // 수강인원 추가
                .maxStudents(course.getMaxStudents())          // 전체 인원 추가
                .build();

        // Professor 구성
        ProfessorDto professorDto = ProfessorDto.builder()
                .id(course.getProfessor().getProfessorId())
                .name(professorName != null ? professorName : "교수")
                .build();

        return MyEnrollmentsResponseDto.EnrollmentItemDto.builder()
                .enrollmentId(enrollment.getId())
                .course(courseInfo)
                .professor(professorDto)
                .schedule(schedules)
                .enrolledAt(enrollment.getEnrolledAt())
                .canCancel(canCancel)
                .build();
    }

    /**
     * ScheduleDto 변환
     */
    private ScheduleDto convertToScheduleDto(CourseSchedule schedule) {
        DayOfWeek dayOfWeek = schedule.getDayOfWeek();
        LocalTime startTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();
        return ScheduleDto.builder()
                .dayOfWeek(dayOfWeek.getValue())
                .dayName(CourseConstants.DAY_NAME_MAP.get(dayOfWeek))
                .startTime(startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .endTime(endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .classroom(schedule.getScheduleRoom())
                .build();
    }

    /**
     * CourseTypeDto 변환
     */
    private CourseTypeDto convertToCourseTypeDto(CourseType courseType) {
        String code = CourseConstants.COURSE_TYPE_CODE_MAP.get(courseType.getTypeCode());
        String name = CourseConstants.COURSE_TYPE_NAME_MAP.get(courseType.getTypeCode());
        String color = CourseConstants.getCourseTypeColor(code);
        return CourseTypeDto.builder()
                .code(code)
                .name(name)
                .color(color)
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

    /**
     * 취소 가능 여부 확인
     * 수강신청 기간, 정정기간, 수강철회기간이면 취소 가능
     */
    private boolean canCancelEnrollment() {
        LocalDateTime now = LocalDateTime.now();
        List<EnrollmentPeriod> periods = enrollmentPeriodRepository.findAll();
        
        return periods.stream()
                .anyMatch(period -> {
                    LocalDateTime start = period.getStartDatetime();
                    LocalDateTime end = period.getEndDatetime();
                    boolean isActive = !now.isBefore(start) && !now.isAfter(end);
                    
                    // 수강신청 기간, 정정기간, 수강철회기간인지 확인
                    String periodName = period.getPeriodName();
                    return isActive && (periodName.contains("수강신청") 
                            || periodName.contains("정정") 
                            || periodName.contains("수강철회"));
                });
    }

    /**
     * 수강신청 기간 활성화 여부 확인
     */
    private boolean isEnrollmentPeriodActive() {
        LocalDateTime now = LocalDateTime.now();
        List<EnrollmentPeriod> periods = enrollmentPeriodRepository.findAll();
        return periods.stream()
                .anyMatch(period -> now.isAfter(period.getStartDatetime()) 
                        && now.isBefore(period.getEndDatetime()));
    }

    @Override
    @Transactional
    public EnrollmentBulkCancelResponseDto cancelBulk(EnrollmentBulkCancelRequestDto request, String studentId) {
        // 1. 학생 정보 조회
        Long studentIdLong = Long.parseLong(studentId);
        Student student = studentRepository.findById(studentIdLong)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 2. 취소 가능 여부 체크
        if (!canCancelEnrollment()) {
            throw new IllegalArgumentException("수강신청 취소 기간이 아닙니다.");
        }

        // 3. 수강신청 ID 목록 확인
        List<Long> enrollmentIds = request.getEnrollmentIds();
        if (enrollmentIds == null || enrollmentIds.isEmpty()) {
            throw new IllegalArgumentException("수강신청 ID 목록이 비어있습니다.");
        }

        // 4. 각 수강신청에 대해 취소 처리
        List<EnrollmentBulkCancelResponseDto.CancelledEnrollmentDto> cancelled = new ArrayList<>();
        List<EnrollmentBulkCancelResponseDto.FailedCancelDto> failed = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long enrollmentId : enrollmentIds) {
            try {
                // 수강신청 조회
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                        .orElseThrow(() -> new IllegalArgumentException("수강신청 내역을 찾을 수 없습니다: " + enrollmentId));

                // 본인의 수강신청인지 확인
                if (!enrollment.getStudent().getId().equals(studentIdLong)) {
                    EnrollmentBulkCancelResponseDto.FailedCancelDto failedDto = 
                            EnrollmentBulkCancelResponseDto.FailedCancelDto.builder()
                                    .enrollmentId(enrollmentId)
                                    .courseId(enrollment.getCourse().getId())
                                    .errorCode("UNAUTHORIZED")
                                    .message("본인의 수강신청만 취소할 수 있습니다")
                                    .build();
                    failed.add(failedDto);
                    continue;
                }

                // 비관적 락으로 Course 조회 (동시성 제어)
                Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getId())
                        .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다: " + enrollment.getCourse().getId()));

                // 수강신청 취소 (정원 감소 - 락이 걸린 상태에서 안전하게 감소)
                if (course.getCurrentStudents() > 0) {
                    course.setCurrentStudents(course.getCurrentStudents() - 1);
                    courseRepository.save(course);
                } else {
                    log.warn("정원이 0인 강의 취소 시도: courseId={}, enrollmentId={}", course.getId(), enrollmentId);
                }

                // 수강신청 삭제
                enrollmentRepository.delete(enrollment);

                // 성공 처리
                EnrollmentBulkCancelResponseDto.CancelledEnrollmentDto cancelledDto = 
                        EnrollmentBulkCancelResponseDto.CancelledEnrollmentDto.builder()
                                .enrollmentId(enrollment.getId())
                                .courseId(course.getId())
                                .courseCode(course.getSubject().getSubjectCode())
                                .courseName(course.getSubject().getSubjectName())
                                .credits(course.getSubject().getCredits())
                                .cancelledAt(now)
                                .build();
                cancelled.add(cancelledDto);

            } catch (IllegalArgumentException e) {
                log.warn("수강신청 취소 실패: enrollmentId={}, error={}", enrollmentId, e.getMessage());
                // Enrollment를 찾지 못한 경우 courseId를 null로 설정
                Long courseId = null;
                try {
                    Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
                    if (enrollment != null) {
                        courseId = enrollment.getCourse().getId();
                    }
                } catch (Exception ex) {
                    // 무시
                }
                
                EnrollmentBulkCancelResponseDto.FailedCancelDto failedDto = 
                        EnrollmentBulkCancelResponseDto.FailedCancelDto.builder()
                                .enrollmentId(enrollmentId)
                                .courseId(courseId)
                                .errorCode("ENROLLMENT_NOT_FOUND")
                                .message(e.getMessage())
                                .build();
                failed.add(failedDto);
            } catch (Exception e) {
                log.error("수강신청 취소 처리 중 오류 발생: enrollmentId={}, error={}", enrollmentId, e.getMessage(), e);
                Long courseId = null;
                try {
                    Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElse(null);
                    if (enrollment != null) {
                        courseId = enrollment.getCourse().getId();
                    }
                } catch (Exception ex) {
                    // 무시
                }
                
                EnrollmentBulkCancelResponseDto.FailedCancelDto failedDto = 
                        EnrollmentBulkCancelResponseDto.FailedCancelDto.builder()
                                .enrollmentId(enrollmentId)
                                .courseId(courseId)
                                .errorCode("CANCEL_ERROR")
                                .message(e.getMessage())
                                .build();
                failed.add(failedDto);
            }
        }

        // 5. 취소 후 남은 수강신청 요약 계산
        List<Enrollment> remainingEnrollments = enrollmentRepository.findByStudentId(studentIdLong);
        int totalCourses = remainingEnrollments.size();
        int totalCredits = remainingEnrollments.stream()
                .mapToInt(enrollment -> enrollment.getCourse().getSubject().getCredits())
                .sum();

        EnrollmentBulkCancelResponseDto.EnrollmentSummaryDto enrollmentSummary = 
                EnrollmentBulkCancelResponseDto.EnrollmentSummaryDto.builder()
                        .totalCourses(totalCourses)
                        .totalCredits(totalCredits)
                        .build();

        // 6. 응답 생성
        int totalAttempted = enrollmentIds.size();
        int successCount = cancelled.size();
        int failedCount = failed.size();

        EnrollmentBulkCancelResponseDto.SummaryDto summary = 
                EnrollmentBulkCancelResponseDto.SummaryDto.builder()
                        .totalAttempted(totalAttempted)
                        .successCount(successCount)
                        .failedCount(failedCount)
                        .build();

        return EnrollmentBulkCancelResponseDto.builder()
                .summary(summary)
                .cancelled(cancelled)
                .failed(failed)
                .enrollmentSummary(enrollmentSummary)
                .build();
    }
}
