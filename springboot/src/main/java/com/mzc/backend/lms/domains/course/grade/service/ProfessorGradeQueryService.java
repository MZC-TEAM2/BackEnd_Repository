package com.mzc.backend.lms.domains.course.grade.service;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.grade.dto.ProfessorCourseGradesResponseDto;
import com.mzc.backend.lms.domains.course.grade.entity.Grade;
import com.mzc.backend.lms.domains.course.grade.enums.GradeStatus;
import com.mzc.backend.lms.domains.course.grade.repository.GradeRepository;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.views.UserViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfessorGradeQueryService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final UserViewService userViewService;

    public enum GradeQueryStatus {
        ALL, PUBLISHED
    }

    @Transactional(readOnly = true)
    public List<ProfessorCourseGradesResponseDto> listCourseGrades(Long courseId, Long professorId, GradeQueryStatus status) {
        Objects.requireNonNull(courseId, "courseId");
        Objects.requireNonNull(professorId, "professorId");

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. courseId=" + courseId));
        if (course.getProfessor() == null || course.getProfessor().getProfessorId() == null
                || !course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("해당 강의 성적 조회 권한이 없습니다.");
        }

        String courseName = (course.getSubject() != null) ? course.getSubject().getSubjectName() : null;
        Long academicTermId = (course.getAcademicTerm() != null) ? course.getAcademicTerm().getId() : null;

        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdWithStudent(courseId);
        List<Long> studentIds = enrollments.stream().map(e -> e.getStudent().getStudentId()).distinct().toList();

        Map<Long, Grade> gradeMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<Grade> grades = (status == GradeQueryStatus.PUBLISHED)
                    ? gradeRepository.findByCourseIdAndStudentIdInAndStatus(courseId, studentIds, GradeStatus.PUBLISHED)
                    : gradeRepository.findByCourseIdAndStudentIdIn(courseId, studentIds);
            for (Grade g : grades) {
                gradeMap.put(g.getStudentId(), g);
            }
        }

        // 이름 복호화(배치)
        Map<Long, String> nameMap = new HashMap<>();
        if (!studentIds.isEmpty()) {
            Map<String, String> raw = userViewService.getUserNames(studentIds.stream().map(String::valueOf).toList());
            for (Map.Entry<String, String> e : raw.entrySet()) {
                try {
                    nameMap.put(Long.parseLong(e.getKey()), e.getValue());
                } catch (NumberFormatException ignore) {
                    // skip
                }
            }
        }

        return enrollments.stream()
                .map(e -> {
                    Long sid = e.getStudent().getStudentId();
                    Grade g = gradeMap.get(sid);
                    return ProfessorCourseGradesResponseDto.builder()
                            .courseId(courseId)
                            .academicTermId(academicTermId)
                            .courseName(courseName)
                            .student(ProfessorCourseGradesResponseDto.StudentDto.builder()
                                    .id(sid)
                                    .studentNumber(e.getStudent().getStudentNumber())
                                    .name(nameMap.get(sid))
                                    .build())
                            .midtermScore(g != null ? g.getMidtermScore() : null)
                            .finalExamScore(g != null ? g.getFinalExamScore() : null)
                            .quizScore(g != null ? g.getQuizScore() : null)
                            .assignmentScore(g != null ? g.getAssignmentScore() : null)
                            .attendanceScore(g != null ? g.getAttendanceScore() : null)
                            .finalScore(g != null ? g.getFinalScore() : null)
                            .finalGrade(g != null ? g.getFinalGrade() : null)
                            .status(g != null && g.getStatus() != null ? g.getStatus().name() : GradeStatus.PENDING.name())
                            .gradedAt(g != null ? g.getGradedAt() : null)
                            .publishedAt(g != null ? g.getPublishedAt() : null)
                            .build();
                })
                .toList();
    }
}


