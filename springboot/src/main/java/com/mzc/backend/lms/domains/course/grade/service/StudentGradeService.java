package com.mzc.backend.lms.domains.course.grade.service;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.grade.dto.StudentGradeResponseDto;
import com.mzc.backend.lms.domains.course.grade.entity.Grade;
import com.mzc.backend.lms.domains.course.grade.enums.GradeStatus;
import com.mzc.backend.lms.domains.course.grade.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentGradeService {

    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<StudentGradeResponseDto> listPublishedGrades(Long studentId, Long academicTermId) {
        Objects.requireNonNull(studentId, "studentId");

        List<Grade> grades = (academicTermId == null)
                ? gradeRepository.findByStudentIdAndStatusOrderByAcademicTermIdDescCourseIdAsc(studentId, GradeStatus.PUBLISHED)
                : gradeRepository.findByStudentIdAndAcademicTermIdAndStatusOrderByCourseIdAsc(studentId, academicTermId, GradeStatus.PUBLISHED);

        List<Long> courseIds = grades.stream().map(Grade::getCourseId).distinct().toList();
        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            for (Course c : courseRepository.findByIdInWithSubject(courseIds)) {
                courseMap.put(c.getId(), c);
            }
        }

        return grades.stream()
                .map(g -> {
                    Course c = courseMap.get(g.getCourseId());
                    String courseName = (c != null && c.getSubject() != null) ? c.getSubject().getSubjectName() : null;
                    Integer courseCredits = (c != null && c.getSubject() != null) ? c.getSubject().getCredits() : null;
                    return StudentGradeResponseDto.builder()
                            .academicTermId(g.getAcademicTermId())
                            .courseId(g.getCourseId())
                            .courseName(courseName)
                            .courseCredits(courseCredits)
                            .status(g.getStatus() != null ? g.getStatus().name() : null)
                            .midtermScore(g.getMidtermScore())
                            .finalExamScore(g.getFinalExamScore())
                            .quizScore(g.getQuizScore())
                            .assignmentScore(g.getAssignmentScore())
                            .attendanceScore(g.getAttendanceScore())
                            .finalScore(g.getFinalScore())
                            .finalGrade(g.getFinalGrade())
                            .publishedAt(g.getPublishedAt())
                            .build();
                })
                .toList();
    }
}


