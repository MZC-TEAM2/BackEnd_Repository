package com.mzc.backend.lms.domains.attendance.service

import com.mzc.backend.lms.domains.attendance.entity.WeekAttendance
import com.mzc.backend.lms.domains.attendance.event.ContentCompletedEvent
import com.mzc.backend.lms.domains.attendance.repository.StudentContentProgressRepository
import com.mzc.backend.lms.domains.attendance.repository.WeekAttendanceRepository
import com.mzc.backend.lms.domains.course.course.entity.Course
import com.mzc.backend.lms.domains.course.course.entity.CourseWeek
import com.mzc.backend.lms.domains.course.course.entity.WeekContent
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository
import com.mzc.backend.lms.domains.course.course.repository.WeekContentRepository
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.domains.user.student.entity.Student
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository
import spock.lang.Specification
import spock.lang.Subject as SubjectAnnotation

import java.time.LocalDateTime

/**
 * AttendanceService 테스트
 * 출석 관리, 조회 기능 테스트
 */
class AttendanceServiceSpec extends Specification {

    def weekAttendanceRepository = Mock(WeekAttendanceRepository)
    def progressRepository = Mock(StudentContentProgressRepository)
    def weekContentRepository = Mock(WeekContentRepository)
    def courseWeekRepository = Mock(CourseWeekRepository)
    def courseRepository = Mock(CourseRepository)
    def studentRepository = Mock(StudentRepository)
    def enrollmentRepository = Mock(EnrollmentRepository)

    @SubjectAnnotation
    def attendanceService = new AttendanceService(
            weekAttendanceRepository,
            progressRepository,
            weekContentRepository,
            courseWeekRepository,
            courseRepository,
            studentRepository,
            enrollmentRepository
    )

    def subject
    def professor
    def course
    def student
    def courseWeek
    def videoContent

    def setup() {
        subject = Mock(Subject) {
            getSubjectName() >> "프로그래밍 기초"
        }
        professor = Mock(Professor) {
            getProfessorId() >> 1001L
        }
        course = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getSectionNumber() >> "001"
            getProfessor() >> professor
        }
        student = Mock(Student) {
            getStudentId() >> 12345L
        }
        courseWeek = Mock(CourseWeek) {
            getId() >> 1L
            getWeekNumber() >> 1
            getWeekTitle() >> "1주차: 오리엔테이션"
            getCourse() >> course
        }
        videoContent = Mock(WeekContent) {
            getId() >> 1L
            getContentType() >> "VIDEO"
            getTitle() >> "강의 영상 1"
        }
    }

    // ==================== 콘텐츠 완료 이벤트 처리 테스트 ====================

    def "콘텐츠 완료 이벤트를 처리한다"() {
        given: "콘텐츠 완료 이벤트"
        def event = ContentCompletedEvent.builder()
                .studentId(12345L)
                .contentId(1L)
                .weekId(1L)
                .courseId(1L)
                .build()
        def attendance = Mock(WeekAttendance) {
            isAttendanceCompleted() >> false
        }

        studentRepository.findById(12345L) >> Optional.of(student)
        courseWeekRepository.findById(1L) >> Optional.of(courseWeek)
        courseRepository.findById(1L) >> Optional.of(course)
        weekContentRepository.findByWeekId(1L) >> [videoContent]
        progressRepository.countCompletedVideosByStudentAndWeek(12345L, 1L) >> 1
        weekAttendanceRepository.findByStudentStudentIdAndWeek_Id(12345L, 1L) >> Optional.of(attendance)

        when: "이벤트를 처리하면"
        attendanceService.processContentCompleted(event)

        then: "출석 진행 상황이 업데이트된다"
        1 * attendance.updateProgress(1)
        1 * weekAttendanceRepository.save(attendance)
    }

    def "VIDEO 콘텐츠가 없으면 출석 처리하지 않는다"() {
        given: "VIDEO가 없는 주차"
        def event = ContentCompletedEvent.builder()
                .studentId(12345L)
                .contentId(1L)
                .weekId(1L)
                .courseId(1L)
                .build()
        def textContent = Mock(WeekContent) {
            getContentType() >> "TEXT"
        }

        studentRepository.findById(12345L) >> Optional.of(student)
        courseWeekRepository.findById(1L) >> Optional.of(courseWeek)
        courseRepository.findById(1L) >> Optional.of(course)
        weekContentRepository.findByWeekId(1L) >> [textContent]

        when: "이벤트를 처리하면"
        attendanceService.processContentCompleted(event)

        then: "출석 저장이 호출되지 않는다"
        0 * weekAttendanceRepository.save(_)
    }

    def "이미 완료된 출석은 변경하지 않는다"() {
        given: "이미 완료된 출석"
        def event = ContentCompletedEvent.builder()
                .studentId(12345L)
                .contentId(1L)
                .weekId(1L)
                .courseId(1L)
                .build()
        def completedAttendance = Mock(WeekAttendance) {
            isAttendanceCompleted() >> true
        }

        studentRepository.findById(12345L) >> Optional.of(student)
        courseWeekRepository.findById(1L) >> Optional.of(courseWeek)
        courseRepository.findById(1L) >> Optional.of(course)
        weekContentRepository.findByWeekId(1L) >> [videoContent]
        progressRepository.countCompletedVideosByStudentAndWeek(12345L, 1L) >> 1
        weekAttendanceRepository.findByStudentStudentIdAndWeek_Id(12345L, 1L) >> Optional.of(completedAttendance)

        when: "이벤트를 처리하면"
        attendanceService.processContentCompleted(event)

        then: "업데이트가 호출되지 않는다"
        0 * completedAttendance.updateProgress(_)
        0 * weekAttendanceRepository.save(_)
    }

    def "학생이 존재하지 않으면 예외가 발생한다"() {
        given: "존재하지 않는 학생"
        def event = ContentCompletedEvent.builder()
                .studentId(99999L)
                .contentId(1L)
                .weekId(1L)
                .courseId(1L)
                .build()
        studentRepository.findById(99999L) >> Optional.empty()

        when: "이벤트를 처리하면"
        attendanceService.processContentCompleted(event)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    // ==================== 학생 출석 조회 테스트 ====================

    def "학생의 강의 출석 현황을 조회한다"() {
        given: "수강 중인 학생"
        def attendance = Mock(WeekAttendance) {
            getWeekId() >> 1L
            isAttendanceCompleted() >> true
            getCompletedVideoCount() >> 2
            getCompletedAt() >> LocalDateTime.now()
        }

        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> true
        courseRepository.findById(1L) >> Optional.of(course)
        courseWeekRepository.findByCourseId(1L) >> [courseWeek]
        weekAttendanceRepository.findByStudentStudentIdAndCourse_Id(12345L, 1L) >> [attendance]
        weekContentRepository.findByWeekId(1L) >> [videoContent, videoContent]
        progressRepository.findByStudentStudentIdAndContent_Id(_, _) >> Optional.empty()

        when: "출석 현황을 조회하면"
        def result = attendanceService.getStudentCourseAttendance(12345L, 1L)

        then: "출석 정보가 반환된다"
        result != null
        result.courseId == 1L
        result.courseName == "프로그래밍 기초"
        result.completedWeeks == 1
        result.totalWeeks == 1
        result.attendanceRate == 100.0
    }

    def "수강하지 않는 강의의 출석은 조회할 수 없다"() {
        given: "수강하지 않는 학생"
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false

        when: "출석 현황을 조회하면"
        attendanceService.getStudentCourseAttendance(12345L, 1L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "학생의 전체 출석 현황을 조회한다"() {
        given: "여러 강의를 수강 중인 학생"
        def enrollment = Mock(Enrollment) {
            getCourse() >> course
        }

        enrollmentRepository.findByStudentId(12345L) >> [enrollment]
        courseRepository.findById(1L) >> Optional.of(course)
        courseWeekRepository.findByCourseId(1L) >> [courseWeek]
        weekAttendanceRepository.countCompletedByStudentAndCourse(12345L, 1L) >> 1

        when: "전체 출석 현황을 조회하면"
        def result = attendanceService.getStudentAllAttendance(12345L)

        then: "모든 강의의 출석 정보가 반환된다"
        result.size() == 1
        result[0].courseId == 1L
        result[0].attendanceRate == 100.0
    }

    // ==================== 교수 출석 조회 테스트 ====================

    def "교수가 강의 출석 현황을 조회한다"() {
        given: "교수의 강의"
        def enrollment = Mock(Enrollment) {
            getStudent() >> student
        }

        courseRepository.findById(1L) >> Optional.of(course)
        enrollmentRepository.findByCourseId(1L) >> [enrollment]
        courseWeekRepository.findByCourseId(1L) >> [courseWeek]
        weekAttendanceRepository.countCompletedByWeek(1L) >> 1

        when: "출석 현황을 조회하면"
        def result = attendanceService.getProfessorCourseAttendance(1001L, 1L)

        then: "전체 출석 현황이 반환된다"
        result != null
        result.courseId == 1L
        result.totalStudents == 1
        result.totalWeeks == 1
    }

    def "다른 교수의 강의 출석은 조회할 수 없다"() {
        given: "다른 교수의 강의"
        courseRepository.findById(1L) >> Optional.of(course)

        when: "출석 현황을 조회하면"
        attendanceService.getProfessorCourseAttendance(9999L, 1L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "교수가 학생별 출석 목록을 조회한다"() {
        given: "교수의 강의와 수강생"
        def enrollment = Mock(Enrollment) {
            getStudent() >> student
        }

        courseRepository.findById(1L) >> Optional.of(course)
        enrollmentRepository.findByCourseId(1L) >> [enrollment]
        courseWeekRepository.findByCourseId(1L) >> [courseWeek]
        weekAttendanceRepository.countCompletedByStudentAndCourse(12345L, 1L) >> 1
        studentRepository.findById(12345L) >> Optional.of(student)

        when: "학생별 출석을 조회하면"
        def result = attendanceService.getProfessorStudentAttendances(1001L, 1L)

        then: "학생 목록이 반환된다"
        result.size() == 1
        result[0].studentId == 12345L
    }

    def "교수가 주차별 출석 현황을 조회한다"() {
        given: "교수의 강의와 주차"
        def enrollment = Mock(Enrollment) {
            getStudent() >> student
        }
        def attendance = Mock(WeekAttendance) {
            getStudentId() >> 12345L
            isAttendanceCompleted() >> true
            getCompletedVideoCount() >> 1
            getCompletedAt() >> LocalDateTime.now()
        }

        courseRepository.findById(1L) >> Optional.of(course)
        courseWeekRepository.findById(1L) >> Optional.of(courseWeek)
        enrollmentRepository.findByCourseId(1L) >> [enrollment]
        weekAttendanceRepository.findByWeek_Id(1L) >> [attendance]
        weekContentRepository.findByWeekId(1L) >> [videoContent]
        studentRepository.findById(12345L) >> Optional.of(student)

        when: "주차별 출석을 조회하면"
        def result = attendanceService.getProfessorWeekAttendances(1001L, 1L, 1L)

        then: "학생별 출석 상태가 반환된다"
        result.size() == 1
        result[0].isCompleted == true
    }

    def "다른 강의의 주차는 조회할 수 없다"() {
        given: "다른 강의의 주차"
        def otherCourse = Mock(Course) {
            getId() >> 999L
        }
        def otherWeek = Mock(CourseWeek) {
            getId() >> 1L
            getCourse() >> otherCourse
        }

        courseRepository.findById(1L) >> Optional.of(course)
        courseWeekRepository.findById(1L) >> Optional.of(otherWeek)

        when: "주차별 출석을 조회하면"
        attendanceService.getProfessorWeekAttendances(1001L, 1L, 1L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }
}
