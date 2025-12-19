package com.mzc.backend.lms.domains.attendance.repository;

import com.mzc.backend.lms.domains.attendance.entity.WeekAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 주차별 출석 Repository
 */
@Repository
public interface WeekAttendanceRepository extends JpaRepository<WeekAttendance, Long> {

    /**
     * 학생 ID와 주차 ID로 출석 조회
     */
    Optional<WeekAttendance> findByStudentStudentIdAndWeekId(Long studentId, Long weekId);

    /**
     * 학생의 특정 강의 출석 목록 조회
     */
    List<WeekAttendance> findByStudentStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * 학생의 모든 출석 목록 조회
     */
    List<WeekAttendance> findByStudentStudentId(Long studentId);

    /**
     * 특정 강의의 모든 학생 출석 목록 조회 (교수용)
     */
    List<WeekAttendance> findByCourseId(Long courseId);

    /**
     * 특정 주차의 모든 학생 출석 목록 조회 (교수용)
     */
    List<WeekAttendance> findByWeekId(Long weekId);

    /**
     * 학생의 특정 강의 출석 완료 주차 수 조회
     */
    @Query("SELECT COUNT(wa) FROM WeekAttendance wa " +
            "WHERE wa.student.studentId = :studentId " +
            "AND wa.course.id = :courseId " +
            "AND wa.isCompleted = true")
    int countCompletedByStudentAndCourse(
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId);

    /**
     * 특정 강의의 출석 완료 학생 수 조회 (주차별)
     */
    @Query("SELECT COUNT(wa) FROM WeekAttendance wa " +
            "WHERE wa.week.id = :weekId " +
            "AND wa.isCompleted = true")
    int countCompletedByWeek(@Param("weekId") Long weekId);

    /**
     * 특정 강의, 주차의 미완료 학생 목록 조회
     */
    @Query("SELECT wa FROM WeekAttendance wa " +
            "WHERE wa.week.id = :weekId " +
            "AND wa.isCompleted = false")
    List<WeekAttendance> findIncompleteByWeek(@Param("weekId") Long weekId);

    /**
     * 학생-강의별 출석 통계 조회
     */
    @Query("SELECT wa.student.studentId, " +
            "COUNT(wa), " +
            "SUM(CASE WHEN wa.isCompleted = true THEN 1 ELSE 0 END) " +
            "FROM WeekAttendance wa " +
            "WHERE wa.course.id = :courseId " +
            "GROUP BY wa.student.studentId")
    List<Object[]> getAttendanceStatsByCourse(@Param("courseId") Long courseId);

    /**
     * 학생 ID와 주차 ID 목록으로 출석 조회
     */
    @Query("SELECT wa FROM WeekAttendance wa " +
            "WHERE wa.student.studentId = :studentId " +
            "AND wa.week.id IN :weekIds")
    List<WeekAttendance> findByStudentAndWeekIds(
            @Param("studentId") Long studentId,
            @Param("weekIds") List<Long> weekIds);
}
