package com.mzc.backend.lms.domains.attendance.service;

import com.mzc.backend.lms.domains.attendance.dto.*;
import com.mzc.backend.lms.domains.attendance.entity.WeekAttendance;
import com.mzc.backend.lms.domains.attendance.event.ContentCompletedEvent;
import com.mzc.backend.lms.domains.attendance.repository.StudentContentProgressRepository;
import com.mzc.backend.lms.domains.attendance.repository.WeekAttendanceRepository;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseWeek;
import com.mzc.backend.lms.domains.course.course.entity.WeekContent;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository;
import com.mzc.backend.lms.domains.course.course.repository.WeekContentRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 출석 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final WeekAttendanceRepository weekAttendanceRepository;
    private final StudentContentProgressRepository progressRepository;
    private final WeekContentRepository weekContentRepository;
    private final CourseWeekRepository courseWeekRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * 콘텐츠 완료 이벤트 처리
     * Video Server에서 발행한 이벤트를 수신하여 출석 상태 갱신
     */
    @Transactional
    public void processContentCompleted(ContentCompletedEvent event) {
        log.info("Processing content completed event: studentId={}, weekId={}, contentId={}",
                event.getStudentId(), event.getWeekId(), event.getContentId());

        Long studentId = event.getStudentId();
        Long weekId = event.getWeekId();
        Long courseId = event.getCourseId();

        // 학생 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // 주차 조회
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("Week not found: " + weekId));

        // 강의 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // 해당 주차의 VIDEO 콘텐츠 수 조회
        int totalVideoCount = countVideoContentsByWeek(weekId);
        if (totalVideoCount == 0) {
            log.info("No VIDEO content in week: {}", weekId);
            return;
        }

        // 학생이 완료한 VIDEO 콘텐츠 수 조회
        int completedVideoCount = progressRepository.countCompletedVideosByStudentAndWeek(studentId, weekId);

        // 출석 레코드 조회 또는 생성
        WeekAttendance attendance = weekAttendanceRepository
                .findByStudentStudentIdAndWeekId(studentId, weekId)
                .orElseGet(() -> {
                    WeekAttendance newAttendance = WeekAttendance.create(student, week, course, totalVideoCount);
                    return weekAttendanceRepository.save(newAttendance);
                });

        // 이미 완료된 출석은 변경하지 않음 (완료 시점 잠금)
        if (attendance.isAttendanceCompleted()) {
            log.info("Attendance already completed: studentId={}, weekId={}", studentId, weekId);
            return;
        }

        // 진행 상황 업데이트
        attendance.updateProgress(completedVideoCount);
        weekAttendanceRepository.save(attendance);

        log.info("Attendance updated: studentId={}, weekId={}, completed={}/{}, isCompleted={}",
                studentId, weekId, completedVideoCount, totalVideoCount, attendance.isAttendanceCompleted());
    }

    /**
     * 주차의 VIDEO 콘텐츠 수 조회
     */
    private int countVideoContentsByWeek(Long weekId) {
        List<WeekContent> contents = weekContentRepository.findByWeekId(weekId);
        return (int) contents.stream()
                .filter(c -> "VIDEO".equals(c.getContentType()))
                .count();
    }

    /**
     * 학생의 특정 강의 출석 현황 조회
     */
    @Transactional(readOnly = true)
    public CourseAttendanceDto getStudentCourseAttendance(Long studentId, Long courseId) {
        // 수강 여부 확인
        validateEnrollment(studentId, courseId);

        // 강의 정보 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // 주차 목록 조회
        List<CourseWeek> weeks = courseWeekRepository.findByCourseId(courseId);

        // 출석 목록 조회
        List<WeekAttendance> attendances = weekAttendanceRepository
                .findByStudentStudentIdAndCourseId(studentId, courseId);

        // 주차별 출석 현황 매핑
        List<WeekAttendanceDto> weekAttendanceDtos = weeks.stream()
                .map(week -> {
                    WeekAttendance attendance = attendances.stream()
                            .filter(a -> a.getWeekId().equals(week.getId()))
                            .findFirst()
                            .orElse(null);

                    int totalVideos = countVideoContentsByWeek(week.getId());

                    return WeekAttendanceDto.builder()
                            .weekId(week.getId())
                            .weekNumber(week.getWeekNumber())
                            .weekTitle(week.getWeekTitle())
                            .isCompleted(attendance != null && attendance.isAttendanceCompleted())
                            .completedVideoCount(attendance != null ? attendance.getCompletedVideoCount() : 0)
                            .totalVideoCount(totalVideos)
                            .completedAt(attendance != null ? attendance.getCompletedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());

        // 출석률 계산
        long completedWeeks = weekAttendanceDtos.stream()
                .filter(WeekAttendanceDto::getIsCompleted)
                .count();
        int totalWeeks = weeks.size();
        double attendanceRate = totalWeeks > 0 ? (completedWeeks * 100.0 / totalWeeks) : 0;

        return CourseAttendanceDto.builder()
                .courseId(courseId)
                .courseName(course.getSubject().getSubjectName())
                .sectionNumber(course.getSectionNumber())
                .completedWeeks((int) completedWeeks)
                .totalWeeks(totalWeeks)
                .attendanceRate(attendanceRate)
                .weekAttendances(weekAttendanceDtos)
                .build();
    }

    /**
     * 학생의 전체 출석 현황 조회
     */
    @Transactional(readOnly = true)
    public List<CourseAttendanceSummaryDto> getStudentAllAttendance(Long studentId) {
        // 학생이 수강 중인 강의 목록 조회
        List<Long> courseIds = enrollmentRepository.findByStudentId(studentId).stream()
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toList());

        return courseIds.stream()
                .map(courseId -> {
                    Course course = courseRepository.findById(courseId).orElse(null);
                    if (course == null) return null;

                    int totalWeeks = courseWeekRepository.findByCourseId(courseId).size();
                    int completedWeeks = weekAttendanceRepository
                            .countCompletedByStudentAndCourse(studentId, courseId);
                    double attendanceRate = totalWeeks > 0 ? (completedWeeks * 100.0 / totalWeeks) : 0;

                    return CourseAttendanceSummaryDto.builder()
                            .courseId(courseId)
                            .courseName(course.getSubject().getSubjectName())
                            .sectionNumber(course.getSectionNumber())
                            .completedWeeks(completedWeeks)
                            .totalWeeks(totalWeeks)
                            .attendanceRate(attendanceRate)
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * 수강 여부 확인
     */
    private void validateEnrollment(Long studentId, Long courseId) {
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!enrolled) {
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }
    }

    // ==================== 교수용 메서드 ====================

    /**
     * 교수의 강의 전체 출석 현황 조회
     */
    @Transactional(readOnly = true)
    public CourseAttendanceOverviewDto getProfessorCourseAttendance(Long professorId, Long courseId) {
        // 교수 권한 확인
        validateProfessorCourse(professorId, courseId);

        // 강의 정보 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // 수강생 수 조회
        int totalStudents = enrollmentRepository.findByCourseId(courseId).size();

        // 주차 목록 조회
        List<CourseWeek> weeks = courseWeekRepository.findByCourseId(courseId);

        // 주차별 출석 통계
        List<CourseAttendanceOverviewDto.WeekAttendanceSummaryDto> weekSummaries = weeks.stream()
                .map(week -> {
                    int completedStudents = weekAttendanceRepository.countCompletedByWeek(week.getId());
                    double completionRate = totalStudents > 0 ? (completedStudents * 100.0 / totalStudents) : 0;

                    return CourseAttendanceOverviewDto.WeekAttendanceSummaryDto.builder()
                            .weekId(week.getId())
                            .weekNumber(week.getWeekNumber())
                            .weekTitle(week.getWeekTitle())
                            .completedStudents(completedStudents)
                            .totalStudents(totalStudents)
                            .completionRate(completionRate)
                            .build();
                })
                .collect(Collectors.toList());

        // 평균 출석률 계산
        double averageAttendanceRate = weekSummaries.stream()
                .mapToDouble(CourseAttendanceOverviewDto.WeekAttendanceSummaryDto::getCompletionRate)
                .average()
                .orElse(0);

        return CourseAttendanceOverviewDto.builder()
                .courseId(courseId)
                .courseName(course.getSubject().getSubjectName())
                .sectionNumber(course.getSectionNumber())
                .totalStudents(totalStudents)
                .totalWeeks(weeks.size())
                .averageAttendanceRate(averageAttendanceRate)
                .weekSummaries(weekSummaries)
                .build();
    }

    /**
     * 교수의 강의 학생별 출석 목록 조회
     */
    @Transactional(readOnly = true)
    public List<StudentAttendanceDto> getProfessorStudentAttendances(Long professorId, Long courseId) {
        // 교수 권한 확인
        validateProfessorCourse(professorId, courseId);

        // 수강생 목록 조회
        var enrollments = enrollmentRepository.findByCourseId(courseId);
        int totalWeeks = courseWeekRepository.findByCourseId(courseId).size();

        return enrollments.stream()
                .map(enrollment -> {
                    Student student = enrollment.getStudent();
                    int completedWeeks = weekAttendanceRepository
                            .countCompletedByStudentAndCourse(student.getStudentId(), courseId);
                    double attendanceRate = totalWeeks > 0 ? (completedWeeks * 100.0 / totalWeeks) : 0;

                    // 학생 이름 조회 (UserProfile에서)
                    String studentName = getStudentName(student.getStudentId());

                    return StudentAttendanceDto.builder()
                            .studentId(student.getStudentId())
                            .studentName(studentName)
                            .completedWeeks(completedWeeks)
                            .totalWeeks(totalWeeks)
                            .attendanceRate(attendanceRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 교수의 주차별 학생 출석 현황 조회
     */
    @Transactional(readOnly = true)
    public List<WeekStudentAttendanceDto> getProfessorWeekAttendances(Long professorId, Long courseId, Long weekId) {
        // 교수 권한 확인
        validateProfessorCourse(professorId, courseId);

        // 주차 확인
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("Week not found: " + weekId));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Week does not belong to this course");
        }

        // 수강생 목록 조회
        var enrollments = enrollmentRepository.findByCourseId(courseId);

        // 해당 주차의 출석 목록 조회
        List<WeekAttendance> attendances = weekAttendanceRepository.findByWeekId(weekId);
        int totalVideos = countVideoContentsByWeek(weekId);

        return enrollments.stream()
                .map(enrollment -> {
                    Student student = enrollment.getStudent();
                    WeekAttendance attendance = attendances.stream()
                            .filter(a -> a.getStudentId().equals(student.getStudentId()))
                            .findFirst()
                            .orElse(null);

                    String studentName = getStudentName(student.getStudentId());

                    return WeekStudentAttendanceDto.builder()
                            .studentId(student.getStudentId())
                            .studentName(studentName)
                            .isCompleted(attendance != null && attendance.isAttendanceCompleted())
                            .completedVideoCount(attendance != null ? attendance.getCompletedVideoCount() : 0)
                            .totalVideoCount(totalVideos)
                            .completedAt(attendance != null ? attendance.getCompletedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 교수 권한 확인
     */
    private void validateProfessorCourse(Long professorId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("Professor is not authorized for this course");
        }
    }

    /**
     * 학생 이름 조회 (UserProfile에서)
     */
    private String getStudentName(Long studentId) {
        return studentRepository.findById(studentId)
                .map(student -> {
                    if (student.getUser() != null && student.getUser().getUserProfile() != null) {
                        return student.getUser().getUserProfile().getName();
                    }
                    return "Unknown";
                })
                .orElse("Unknown");
    }
}
