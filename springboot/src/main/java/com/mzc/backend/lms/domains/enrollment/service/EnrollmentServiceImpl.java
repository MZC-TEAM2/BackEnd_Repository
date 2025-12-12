package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.subject.entity.SubjectPrerequisites;
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository;
import com.mzc.backend.lms.domains.enrollment.dto.*;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import com.mzc.backend.lms.domains.enrollment.repository.CourseCartRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private static final int MAX_CREDITS_PER_TERM = 21; // 학기당 최대 학점

    @Override
    public EnrollmentBulkResponseDto enrollBulk(EnrollmentBulkRequestDto request, String studentId) {
        // 1. 수강신청 기간 체크
        if (!isEnrollmentPeriodActive()) {
            throw new IllegalArgumentException("수강신청 기간이 아닙니다.");
        }

        Long studentIdLong = Long.parseLong(studentId);

        // 2. 학생 정보 조회
        Student student = studentRepository.findById(studentIdLong)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 3. 과목 존재 여부 체크 및 조회
        List<Long> courseIds = request.getCourseIds();
        if (courseIds == null || courseIds.isEmpty()) {
            throw new IllegalArgumentException("강의 ID 목록이 비어있습니다.");
        }

        List<Course> courses = courseRepository.findAllById(courseIds);
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        // 존재하지 않는 강의 체크
        for (Long courseId : courseIds) {
            if (!courseMap.containsKey(courseId)) {
                throw new IllegalArgumentException("강의를 찾을 수 없습니다: " + courseId);
            }
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
            Course course = courseMap.get(courseId);
            if (course == null) {
                continue;
            }

            // 개별 검증
            String errorCode = validateEnrollment(course, studentIdLong, existingEnrollments, 
                    enrolledCourseIds, enrolledSubjectIds, currentCredits + enrolledCredits);
            
            if (errorCode != null) {
                // 실패 처리
                EnrollmentBulkResponseDto.FailedEnrollmentDto failedDto = createFailedDto(
                        course, errorCode, getErrorMessage(errorCode, course));
                failed.add(failedDto);
                continue;
            }

            // 수강신청 처리
            try {
                Enrollment enrollment = Enrollment.builder()
                        .student(student)
                        .course(course)
                        .enrolledAt(now)
                        .build();

                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
                
                // 정원 증가 (낙관적 락 고려 필요)
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

            } catch (Exception e) {
                log.error("수강신청 처리 중 오류 발생: courseId={}, error={}", courseId, e.getMessage(), e);
                EnrollmentBulkResponseDto.FailedEnrollmentDto failedDto = createFailedDto(
                        course, "ENROLLMENT_ERROR", e.getMessage());
                failed.add(failedDto);
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
     * 수강신청 검증
     * @return 에러 코드 (null이면 성공)
     */
    private String validateEnrollment(Course course, Long studentId, 
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

        // 3. 정원 체크
        if (course.getCurrentStudents() >= course.getMaxStudents()) {
            return "COURSE_FULL";
        }

        // 4. 시간표 충돌 체크
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

        // 5. 선수과목 이수 여부 체크
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

        // 6. 학점 제한 체크
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

        LocalTime start1 = schedule1.getStartTime().minusHours(9);
        LocalTime end1 = schedule1.getEndTime().minusHours(9);
        LocalTime start2 = schedule2.getStartTime().minusHours(9);
        LocalTime end2 = schedule2.getEndTime().minusHours(9);

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * 실패 DTO 생성
     */
    private EnrollmentBulkResponseDto.FailedEnrollmentDto createFailedDto(
            Course course, String errorCode, String message) {
        EnrollmentDto enrollmentDto = EnrollmentDto.builder()
                .current(course.getCurrentStudents())
                .max(course.getMaxStudents())
                .isFull(course.getCurrentStudents() >= course.getMaxStudents())
                .availableSeats(course.getMaxStudents() - course.getCurrentStudents())
                .build();

        return EnrollmentBulkResponseDto.FailedEnrollmentDto.builder()
                .courseId(course.getId())
                .courseCode(course.getSubject().getSubjectCode())
                .courseName(course.getSubject().getSubjectName())
                .section(course.getSectionNumber())
                .errorCode(errorCode)
                .message(message)
                .enrollment(enrollmentDto)
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
}
