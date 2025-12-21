package com.mzc.backend.lms.domains.dashboard.student.repository;

import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.dashboard.student.dto.EnrollmentSummaryDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.NoticeDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.PendingAssignmentDto;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 대시보드 전용 조회 Repository
 * EntityManager + JPQL 기반
 */
@Repository
@RequiredArgsConstructor
public class DashboardQueryRepository {

    private final EntityManager em;

    /**
     * 미제출 과제 목록 조회
     * - 학생이 수강 중인 과목의 과제
     * - 마감일이 현재 ~ 지정된 기한 이내
     * - 아직 제출하지 않은 과제
     *
     * @param studentId 학생 ID
     * @param withinDays 마감일 기준 일수 (예: 7일 이내)
     * @return 미제출 과제 목록
     */
    public List<PendingAssignmentDto> findPendingAssignments(Long studentId, int withinDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(withinDays);

        String jpql = """
            SELECT new com.mzc.backend.lms.domains.dashboard.student.dto.PendingAssignmentDto(
                a.id,
                p.id,
                p.title,
                c.id,
                CONCAT(s.subjectName, ' - ', c.sectionNumber),
                s.subjectName,
                a.dueDate,
                a.lateSubmissionAllowed
            )
            FROM Assignment a
            JOIN a.post p
            JOIN Course c ON a.courseId = c.id
            JOIN c.subject s
            WHERE a.courseId IN (
                SELECT e.course.id FROM Enrollment e WHERE e.student.studentId = :studentId
            )
            AND a.dueDate > :now
            AND a.dueDate <= :deadline
            AND a.isDeleted = false
            AND NOT EXISTS (
                SELECT 1 FROM AssignmentSubmission sub
                WHERE sub.assignment.id = a.id
                AND sub.userId = :studentId
                AND sub.isDeleted = false
            )
            ORDER BY a.dueDate ASC
            """;

        TypedQuery<PendingAssignmentDto> query = em.createQuery(jpql, PendingAssignmentDto.class);
        query.setParameter("studentId", studentId);
        query.setParameter("now", now);
        query.setParameter("deadline", deadline);

        return query.getResultList();
    }

    /**
     * 오늘의 강의 목록 조회
     * - 학생이 수강 중인 과목
     * - 오늘 요일에 해당하는 강의
     *
     * @param studentId 학생 ID
     * @return 오늘 수업이 있는 Enrollment 목록
     */
    public List<Enrollment> findTodayEnrollments(Long studentId) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        String jpql = """
            SELECT DISTINCT e
            FROM Enrollment e
            JOIN FETCH e.course c
            JOIN FETCH c.subject s
            JOIN FETCH c.professor p
            JOIN FETCH p.user u
            LEFT JOIN FETCH u.userProfile up
            JOIN FETCH c.schedules cs
            WHERE e.student.studentId = :studentId
            AND cs.dayOfWeek = :today
            """;

        TypedQuery<Enrollment> query = em.createQuery(jpql, Enrollment.class);
        query.setParameter("studentId", studentId);
        query.setParameter("today", today);

        return query.getResultList();
    }

    /**
     * 최신 공지사항 목록 조회
     * - 학교 공지사항 게시판의 게시글
     * - 삭제되지 않은 게시글
     * - 최신순 정렬
     *
     * @param limit 조회할 개수 (기본값: 5)
     * @return 최신 공지사항 목록
     */
    public List<NoticeDto> findLatestNotices(int limit) {
        String jpql = """
            SELECT new com.mzc.backend.lms.domains.dashboard.student.dto.NoticeDto(
                p.id,
                p.title,
                p.createdAt,
                p.viewCount
            )
            FROM Post p
            JOIN p.category c
            WHERE c.boardType = :boardType
            AND p.isDeleted = false
            ORDER BY p.createdAt DESC
            """;

        TypedQuery<NoticeDto> query = em.createQuery(jpql, NoticeDto.class);
        query.setParameter("boardType", BoardType.NOTICE);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    /**
     * 수강 현황 요약 조회
     * - 수강 중인 과목 수
     * - 수강 중인 총 학점
     *
     * @param studentId 학생 ID
     * @return 수강 현황 요약
     */
    public EnrollmentSummaryDto findEnrollmentSummary(Long studentId) {
        String jpql = """
            SELECT new com.mzc.backend.lms.domains.dashboard.student.dto.EnrollmentSummaryDto(
                COUNT(e),
                COALESCE(SUM(s.credits), 0)
            )
            FROM Enrollment e
            JOIN e.course c
            JOIN c.subject s
            WHERE e.student.studentId = :studentId
            """;

        TypedQuery<EnrollmentSummaryDto> query = em.createQuery(jpql, EnrollmentSummaryDto.class);
        query.setParameter("studentId", studentId);

        return query.getSingleResult();
    }
}
