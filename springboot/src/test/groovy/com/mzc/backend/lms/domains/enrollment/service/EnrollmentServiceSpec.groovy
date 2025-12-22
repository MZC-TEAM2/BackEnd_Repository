package com.mzc.backend.lms.domains.enrollment.service

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository
import com.mzc.backend.lms.domains.course.course.entity.Course
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule
import com.mzc.backend.lms.domains.course.course.entity.CourseType
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository
import com.mzc.backend.lms.domains.enrollment.dto.CourseIdsRequestDto
import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentBulkCancelRequestDto
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment
import com.mzc.backend.lms.domains.enrollment.repository.CourseCartRepository
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.domains.user.student.entity.Student
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository
import com.mzc.backend.lms.views.UserViewService
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Specification
import spock.lang.Subject as SubjectAnnotation

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * EnrollmentService 테스트
 * 수강신청, 취소, 조회 기능 테스트
 */
class EnrollmentServiceSpec extends Specification {

    def enrollmentRepository = Mock(EnrollmentRepository)
    def courseRepository = Mock(CourseRepository)
    def courseCartRepository = Mock(CourseCartRepository)
    def enrollmentPeriodRepository = Mock(EnrollmentPeriodRepository)
    def subjectPrerequisitesRepository = Mock(SubjectPrerequisitesRepository)
    def studentRepository = Mock(StudentRepository)
    def userViewService = Mock(UserViewService)
    def eventPublisher = Mock(ApplicationEventPublisher)

    @SubjectAnnotation
    def enrollmentService = new EnrollmentServiceImpl(
            enrollmentRepository,
            courseRepository,
            courseCartRepository,
            enrollmentPeriodRepository,
            subjectPrerequisitesRepository,
            studentRepository,
            userViewService,
            eventPublisher
    )

    def subject
    def courseType
    def professor
    def academicTerm
    def course
    def student

    def setup() {
        courseType = Mock(CourseType) {
            getTypeCode() >> 1
        }
        subject = Mock(Subject) {
            getId() >> 1L
            getSubjectCode() >> "CS101"
            getSubjectName() >> "프로그래밍 기초"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        professor = Mock(Professor) {
            getProfessorId() >> 1001L
        }
        academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2025
            getTermType() >> "1"
        }
        course = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 30
            getMaxStudents() >> 40
            getSchedules() >> []
            getAcademicTerm() >> academicTerm
        }
        student = Mock(Student) {
            getId() >> 12345L
        }
    }

    // ==================== 수강신청 테스트 ====================

    def "수강신청 기간이 아니면 예외가 발생한다"() {
        given: "수강신청 기간이 아닌 상태"
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [1L]
        }
        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> false

        when: "수강신청을 시도하면"
        enrollmentService.enrollBulk(request, "12345")

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "학생을 찾을 수 없으면 예외가 발생한다"() {
        given: "존재하지 않는 학생"
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [1L]
        }
        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.empty()

        when: "수강신청을 시도하면"
        enrollmentService.enrollBulk(request, "12345")

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "강의 ID 목록이 비어있으면 예외가 발생한다"() {
        given: "빈 강의 목록"
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> []
        }
        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)

        when: "수강신청을 시도하면"
        enrollmentService.enrollBulk(request, "12345")

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "존재하지 않는 강의가 포함되면 예외가 발생한다"() {
        given: "존재하지 않는 강의 ID가 포함된 요청"
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [1L, 999L]
        }
        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)
        courseRepository.findAllById([1L, 999L]) >> [course] // 1개만 반환

        when: "수강신청을 시도하면"
        enrollmentService.enrollBulk(request, "12345")

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "정원이 마감된 강의는 수강신청에 실패한다"() {
        given: "정원이 마감된 강의"
        def fullCourse = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 40
            getMaxStudents() >> 40
            getSchedules() >> []
        }
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [1L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)
        courseRepository.findAllById([1L]) >> [fullCourse]
        enrollmentRepository.findByStudentId(12345L) >> []
        courseRepository.findByIdWithLock(1L) >> Optional.of(fullCourse)

        when: "수강신청을 시도하면"
        def result = enrollmentService.enrollBulk(request, "12345")

        then: "실패 목록에 포함된다"
        result.failed.size() == 1
        result.failed[0].errorCode == "COURSE_FULL"
        result.succeeded.size() == 0
    }

    def "이미 수강신청한 강의는 실패한다"() {
        given: "이미 수강신청한 강의"
        def existingEnrollment = Mock(Enrollment) {
            getCourse() >> course
        }
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [1L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)
        courseRepository.findAllById([1L]) >> [course]
        enrollmentRepository.findByStudentId(12345L) >> [existingEnrollment]
        courseRepository.findByIdWithLock(1L) >> Optional.of(course)

        when: "동일 강의를 다시 수강신청하면"
        def result = enrollmentService.enrollBulk(request, "12345")

        then: "실패 목록에 포함된다"
        result.failed.size() == 1
        result.failed[0].errorCode == "ALREADY_ENROLLED"
    }

    def "동일 과목의 다른 분반은 수강신청할 수 없다"() {
        given: "이미 해당 과목의 다른 분반을 수강신청한 상태"
        def section2Course = Mock(Course) {
            getId() >> 2L
            getSubject() >> subject  // 같은 과목
            getSectionNumber() >> "002"
            getProfessor() >> professor
            getCurrentStudents() >> 30
            getMaxStudents() >> 40
            getSchedules() >> []
        }
        def existingEnrollment = Mock(Enrollment) {
            getCourse() >> course  // 001분반
        }
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [2L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)
        courseRepository.findAllById([2L]) >> [section2Course]
        enrollmentRepository.findByStudentId(12345L) >> [existingEnrollment]
        courseRepository.findByIdWithLock(2L) >> Optional.of(section2Course)

        when: "동일 과목의 다른 분반을 수강신청하면"
        def result = enrollmentService.enrollBulk(request, "12345")

        then: "실패 목록에 포함된다"
        result.failed.size() == 1
        result.failed[0].errorCode == "DUPLICATE_SUBJECT"
    }

    def "시간표가 충돌하면 수강신청에 실패한다"() {
        given: "시간표가 충돌하는 강의"
        def conflictSchedule = Mock(CourseSchedule) {
            getDayOfWeek() >> DayOfWeek.MONDAY
            getStartTime() >> LocalTime.of(9, 0)
            getEndTime() >> LocalTime.of(10, 30)
        }
        def existingCourse = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getSchedules() >> [conflictSchedule]
        }
        def existingEnrollment = Mock(Enrollment) {
            getCourse() >> existingCourse
        }

        // 다른 과목이지만 같은 시간에 강의
        def anotherSubject = Mock(Subject) {
            getId() >> 2L
            getSubjectCode() >> "CS102"
            getSubjectName() >> "자료구조"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        def newCourseSchedule = Mock(CourseSchedule) {
            getDayOfWeek() >> DayOfWeek.MONDAY
            getStartTime() >> LocalTime.of(10, 0)
            getEndTime() >> LocalTime.of(11, 30)
        }
        def newCourse = Mock(Course) {
            getId() >> 2L
            getSubject() >> anotherSubject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 20
            getMaxStudents() >> 40
            getSchedules() >> [newCourseSchedule]
        }

        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [2L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)
        courseRepository.findAllById([2L]) >> [newCourse]
        enrollmentRepository.findByStudentId(12345L) >> [existingEnrollment]
        courseRepository.findByIdWithLock(2L) >> Optional.of(newCourse)
        subjectPrerequisitesRepository.findBySubjectId(2L) >> []

        when: "시간표가 충돌하는 강의를 수강신청하면"
        def result = enrollmentService.enrollBulk(request, "12345")

        then: "실패 목록에 포함된다"
        result.failed.size() == 1
        result.failed[0].errorCode == "TIME_CONFLICT"
    }

    def "학점 제한을 초과하면 수강신청에 실패한다"() {
        given: "이미 21학점을 수강신청한 상태"
        def highCreditSubject = Mock(Subject) {
            getId() >> 10L
            getCredits() >> 21
            getCourseType() >> courseType
        }
        def highCreditCourse = Mock(Course) {
            getId() >> 10L
            getSubject() >> highCreditSubject
            getSchedules() >> []
        }
        def existingEnrollment = Mock(Enrollment) {
            getCourse() >> highCreditCourse
        }

        def newSubject = Mock(Subject) {
            getId() >> 2L
            getSubjectCode() >> "CS102"
            getSubjectName() >> "자료구조"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        def newCourse = Mock(Course) {
            getId() >> 2L
            getSubject() >> newSubject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 20
            getMaxStudents() >> 40
            getSchedules() >> []
        }

        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [2L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)
        courseRepository.findAllById([2L]) >> [newCourse]
        enrollmentRepository.findByStudentId(12345L) >> [existingEnrollment]
        courseRepository.findByIdWithLock(2L) >> Optional.of(newCourse)
        subjectPrerequisitesRepository.findBySubjectId(2L) >> []

        when: "학점 제한을 초과하는 강의를 수강신청하면"
        def result = enrollmentService.enrollBulk(request, "12345")

        then: "실패 목록에 포함된다"
        result.failed.size() == 1
        result.failed[0].errorCode == "CREDIT_LIMIT_EXCEEDED"
    }

    def "정상적인 수강신청이 성공한다"() {
        given: "유효한 수강신청 요청"
        def newSubject = Mock(Subject) {
            getId() >> 2L
            getSubjectCode() >> "CS102"
            getSubjectName() >> "자료구조"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        def newCourse = Mock(Course) {
            getId() >> 2L
            getSubject() >> newSubject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 20
            getMaxStudents() >> 40
            getSchedules() >> []
        }
        def savedEnrollment = Mock(Enrollment) {
            getId() >> 100L
            getEnrolledAt() >> LocalDateTime.now()
        }

        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [2L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)
        courseRepository.findAllById([2L]) >> [newCourse]
        enrollmentRepository.findByStudentId(12345L) >> []
        courseRepository.findByIdWithLock(2L) >> Optional.of(newCourse)
        subjectPrerequisitesRepository.findBySubjectId(2L) >> []
        courseCartRepository.findByStudentIdAndCourseId(12345L, 2L) >> Optional.empty()

        when: "수강신청을 하면"
        enrollmentService.enrollBulk(request, "12345")

        then: "수강신청이 저장된다"
        1 * enrollmentRepository.save(_) >> savedEnrollment
        1 * courseRepository.save(_)
        1 * eventPublisher.publishEvent(_)
    }

    // ==================== 수강신청 취소 테스트 ====================

    def "수강신청 취소 기간이 아니면 예외가 발생한다"() {
        given: "취소 기간이 아닌 상태"
        def request = Mock(EnrollmentBulkCancelRequestDto) {
            getEnrollmentIds() >> [1L]
        }
        studentRepository.findById(12345L) >> Optional.of(student)
        enrollmentPeriodRepository.findAll() >> []

        when: "수강신청 취소를 시도하면"
        enrollmentService.cancelBulk(request, "12345")

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "본인의 수강신청만 취소할 수 있다"() {
        given: "다른 학생의 수강신청"
        def otherStudent = Mock(Student) {
            getId() >> 99999L
        }
        def otherEnrollment = Mock(Enrollment) {
            getId() >> 1L
            getStudent() >> otherStudent
            getCourse() >> course
        }
        def request = Mock(EnrollmentBulkCancelRequestDto) {
            getEnrollmentIds() >> [1L]
        }
        def activePeriod = Mock(EnrollmentPeriod) {
            getPeriodName() >> "수강신청 기간"
            getStartDatetime() >> LocalDateTime.now().minusDays(1)
            getEndDatetime() >> LocalDateTime.now().plusDays(1)
        }

        studentRepository.findById(12345L) >> Optional.of(student)
        enrollmentPeriodRepository.findAll() >> [activePeriod]
        enrollmentRepository.findById(1L) >> Optional.of(otherEnrollment)
        enrollmentRepository.findByStudentId(12345L) >> []

        when: "다른 학생의 수강신청을 취소하면"
        def result = enrollmentService.cancelBulk(request, "12345")

        then: "실패 목록에 포함된다"
        result.failed.size() == 1
        result.failed[0].errorCode == "UNAUTHORIZED"
    }

    def "정상적인 수강신청 취소가 성공한다"() {
        given: "취소할 수강신청"
        def myEnrollment = Mock(Enrollment) {
            getId() >> 1L
            getStudent() >> student
            getCourse() >> course
        }
        def request = Mock(EnrollmentBulkCancelRequestDto) {
            getEnrollmentIds() >> [1L]
        }
        def activePeriod = Mock(EnrollmentPeriod) {
            getPeriodName() >> "수강신청 기간"
            getStartDatetime() >> LocalDateTime.now().minusDays(1)
            getEndDatetime() >> LocalDateTime.now().plusDays(1)
        }

        studentRepository.findById(12345L) >> Optional.of(student)
        enrollmentPeriodRepository.findAll() >> [activePeriod]
        enrollmentRepository.findById(1L) >> Optional.of(myEnrollment)
        courseRepository.findByIdWithLock(1L) >> Optional.of(course)
        enrollmentRepository.findByStudentId(12345L) >> []

        when: "수강신청을 취소하면"
        def result = enrollmentService.cancelBulk(request, "12345")

        then: "수강신청이 삭제되고 정원이 감소한다"
        1 * enrollmentRepository.delete(myEnrollment)
        1 * courseRepository.save(_)
        1 * eventPublisher.publishEvent(_)
        result.cancelled.size() == 1
    }

    // ==================== 수강신청 조회 테스트 ====================

    def "특정 기간의 수강신청 내역을 조회한다"() {
        given: "수강신청 기간과 수강신청 내역"
        def enrollmentPeriod = Mock(EnrollmentPeriod) {
            getAcademicTerm() >> academicTerm
        }
        def enrollment = Mock(Enrollment) {
            getId() >> 1L
            getCourse() >> course
            getEnrolledAt() >> LocalDateTime.now()
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        enrollmentRepository.findByStudentId(12345L) >> [enrollment]
        enrollmentPeriodRepository.findAll() >> []
        userViewService.getUserName("1001") >> "김교수"

        when: "수강신청 내역을 조회하면"
        def result = enrollmentService.getMyEnrollments("12345", 1L)

        then: "수강신청 목록이 반환된다"
        result != null
        result.term.id == 1L
        result.summary.totalCourses == 1
        result.summary.totalCredits == 3
    }

    def "수강신청 기간을 찾을 수 없으면 예외가 발생한다"() {
        given: "존재하지 않는 수강신청 기간"
        enrollmentPeriodRepository.findById(999L) >> Optional.empty()

        when: "수강신청 내역을 조회하면"
        enrollmentService.getMyEnrollments("12345", 999L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }
}
