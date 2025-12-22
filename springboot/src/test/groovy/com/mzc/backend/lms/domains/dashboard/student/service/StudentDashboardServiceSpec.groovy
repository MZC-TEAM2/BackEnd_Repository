package com.mzc.backend.lms.domains.dashboard.student.service

import com.mzc.backend.lms.domains.dashboard.student.dto.EnrollmentSummaryDto
import com.mzc.backend.lms.domains.dashboard.student.dto.NoticeDto
import com.mzc.backend.lms.domains.dashboard.student.dto.PendingAssignmentDto
import com.mzc.backend.lms.domains.dashboard.student.repository.DashboardQueryRepository
import com.mzc.backend.lms.domains.course.course.entity.Course
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile
import com.mzc.backend.lms.domains.user.user.entity.User
import spock.lang.Specification
import spock.lang.Subject as TestSubject

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * StudentDashboardService 테스트
 * 학생 대시보드 기능 테스트
 */
class StudentDashboardServiceSpec extends Specification {

    def dashboardQueryRepository = Mock(DashboardQueryRepository)

    @TestSubject
    def studentDashboardService = new StudentDashboardService(dashboardQueryRepository)

    // ==================== 미제출 과제 목록 조회 테스트 ====================

    def "미제출 과제 목록을 기본 7일 기준으로 조회한다"() {
        given: "미제출 과제가 있는 학생"
        def studentId = 12345L
        def pendingAssignment = new PendingAssignmentDto(
                1L, 1L, "과제1", 1L, "컴퓨터공학 - 1", "컴퓨터공학",
                LocalDateTime.now().plusDays(3), false
        )

        when: "미제출 과제를 조회하면"
        def result = studentDashboardService.getPendingAssignments(studentId)

        then: "7일 기준으로 조회된다"
        1 * dashboardQueryRepository.findPendingAssignments(studentId, 7) >> [pendingAssignment]
        result.size() == 1
        result[0].title == "과제1"
    }

    def "미제출 과제 목록을 지정 기간으로 조회한다"() {
        given: "미제출 과제가 있는 학생"
        def studentId = 12345L
        def withinDays = 14

        when: "14일 기준으로 조회하면"
        def result = studentDashboardService.getPendingAssignments(studentId, withinDays)

        then: "14일 기준으로 조회된다"
        1 * dashboardQueryRepository.findPendingAssignments(studentId, 14) >> []
        result.isEmpty()
    }

    // ==================== 공지사항 조회 테스트 ====================

    def "최신 공지사항을 기본 5개 조회한다"() {
        given: "공지사항이 있는 상태"
        def notice = new NoticeDto(1L, "공지1", LocalDateTime.now(), 100)

        when: "최신 공지사항을 조회하면"
        def result = studentDashboardService.getLatestNotices()

        then: "5개 기준으로 조회된다"
        1 * dashboardQueryRepository.findLatestNotices(5) >> [notice]
        result.size() == 1
        result[0].title == "공지1"
    }

    def "최신 공지사항을 지정 개수로 조회한다"() {
        given: "공지사항이 있는 상태"
        def limit = 10

        when: "10개 기준으로 조회하면"
        def result = studentDashboardService.getLatestNotices(limit)

        then: "10개 기준으로 조회된다"
        1 * dashboardQueryRepository.findLatestNotices(10) >> []
        result.isEmpty()
    }

    // ==================== 수강 현황 요약 조회 테스트 ====================

    def "수강 현황 요약을 조회한다"() {
        given: "수강 중인 학생"
        def studentId = 12345L
        def summary = new EnrollmentSummaryDto(5L, 15L)

        when: "수강 현황을 조회하면"
        def result = studentDashboardService.getEnrollmentSummary(studentId)

        then: "수강 현황 요약이 반환된다"
        1 * dashboardQueryRepository.findEnrollmentSummary(studentId) >> summary
        result.courseCount == 5L
        result.totalCredits == 15L
    }

    def "수강 중인 과목이 없으면 0으로 반환된다"() {
        given: "수강 중인 과목이 없는 학생"
        def studentId = 12345L
        def summary = new EnrollmentSummaryDto(0L, 0L)

        when: "수강 현황을 조회하면"
        def result = studentDashboardService.getEnrollmentSummary(studentId)

        then: "0으로 반환된다"
        1 * dashboardQueryRepository.findEnrollmentSummary(studentId) >> summary
        result.courseCount == 0L
        result.totalCredits == 0L
    }

    // ==================== 오늘의 강의 조회 테스트 ====================

    def "오늘 강의가 없으면 빈 목록을 반환한다"() {
        given: "오늘 강의가 없는 학생"
        def studentId = 12345L

        when: "오늘의 강의를 조회하면"
        def result = studentDashboardService.getTodayCourses(studentId)

        then: "빈 목록이 반환된다"
        1 * dashboardQueryRepository.findTodayEnrollments(studentId) >> []
        result.isEmpty()
    }

    def "오늘 강의가 있으면 시간순으로 정렬된 목록을 반환한다"() {
        given: "오늘 강의가 있는 학생"
        def studentId = 12345L
        def today = LocalDate.now().getDayOfWeek()

        // Mock 설정
        def userProfile = Mock(UserProfile) {
            getName() >> "교수님"
        }
        def user = Mock(User) {
            getUserProfile() >> userProfile
        }
        def professor = Mock(Professor) {
            getProfessorId() >> 1L
            getUser() >> user
        }
        def subject = Mock(Subject) {
            getSubjectCode() >> "CS101"
            getSubjectName() >> "컴퓨터공학개론"
            getCredits() >> 3
            getCourseType() >> null
        }
        def schedule = Mock(CourseSchedule) {
            getDayOfWeek() >> today
            getStartTime() >> LocalTime.of(9, 0)
            getEndTime() >> LocalTime.of(10, 30)
            getScheduleRoom() >> "공학관 101호"
        }
        def course = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getSectionNumber() >> 1
            getProfessor() >> professor
            getSchedules() >> [schedule]
            getCurrentStudents() >> 30
            getMaxStudents() >> 40
        }
        def enrollment = Mock(Enrollment) {
            getId() >> 1L
            getCourse() >> course
        }

        when: "오늘의 강의를 조회하면"
        def result = studentDashboardService.getTodayCourses(studentId)

        then: "강의 목록이 반환된다"
        1 * dashboardQueryRepository.findTodayEnrollments(studentId) >> [enrollment]
        result.size() == 1
        result[0].course.courseName == "컴퓨터공학개론"
        result[0].professor.name == "교수님"
        result[0].schedule.size() == 1
    }
}
