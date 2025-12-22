package com.mzc.backend.lms.domains.enrollment.service

import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository
import com.mzc.backend.lms.domains.course.course.entity.Course
import com.mzc.backend.lms.domains.course.course.entity.CourseType
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository
import com.mzc.backend.lms.domains.enrollment.dto.CartBulkDeleteRequestDto
import com.mzc.backend.lms.domains.enrollment.dto.CourseIdsRequestDto
import com.mzc.backend.lms.domains.enrollment.entity.CourseCart
import com.mzc.backend.lms.domains.enrollment.repository.CourseCartRepository
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.domains.user.student.entity.Student
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository
import com.mzc.backend.lms.views.UserViewService
import spock.lang.Specification
import spock.lang.Subject as SubjectAnnotation

import java.time.LocalDateTime

/**
 * CartService 테스트
 * 장바구니 조회, 추가, 삭제 기능 테스트
 */
class CartServiceSpec extends Specification {

    def courseCartRepository = Mock(CourseCartRepository)
    def courseRepository = Mock(CourseRepository)
    def enrollmentRepository = Mock(EnrollmentRepository)
    def enrollmentPeriodRepository = Mock(EnrollmentPeriodRepository)
    def subjectPrerequisitesRepository = Mock(SubjectPrerequisitesRepository)
    def studentRepository = Mock(StudentRepository)
    def userViewService = Mock(UserViewService)

    @SubjectAnnotation
    def cartService = new CartServiceImpl(
            courseCartRepository,
            courseRepository,
            enrollmentRepository,
            enrollmentPeriodRepository,
            subjectPrerequisitesRepository,
            studentRepository,
            userViewService
    )

    def "학생의 장바구니를 조회한다"() {
        given: "장바구니에 강의가 있는 학생"
        def studentId = "12345"
        def courseType = Mock(CourseType) {
            getTypeCode() >> 1
        }
        def subject = Mock(Subject) {
            getSubjectCode() >> "CS101"
            getSubjectName() >> "프로그래밍 기초"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        def professor = Mock(Professor) {
            getProfessorId() >> 1001L
        }
        def course = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 30
            getMaxStudents() >> 40
            getSchedules() >> []
        }
        def cart = Mock(CourseCart) {
            getId() >> 1L
            getCourse() >> course
            getAddedAt() >> LocalDateTime.now()
        }

        courseCartRepository.findByStudentId(12345L) >> [cart]
        userViewService.getUserName("1001") >> "김교수"

        when: "장바구니를 조회하면"
        def result = cartService.getCart(studentId)

        then: "장바구니 정보가 반환된다"
        result != null
        result.totalCourses == 1
        result.totalCredits == 3
        result.courses.size() == 1
    }

    def "빈 장바구니를 조회한다"() {
        given: "빈 장바구니"
        def studentId = "12345"

        courseCartRepository.findByStudentId(12345L) >> []

        when: "장바구니를 조회하면"
        def result = cartService.getCart(studentId)

        then: "빈 결과가 반환된다"
        result.totalCourses == 0
        result.totalCredits == 0
        result.courses.isEmpty()
    }

    def "수강신청 기간이 아니면 장바구니 추가 시 예외가 발생한다"() {
        given: "수강신청 기간이 아님"
        def studentId = "12345"
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [1L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> false

        when: "장바구니에 추가하면"
        cartService.addToCartBulk(request, studentId)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "빈 강의 목록으로 장바구니 추가 시 예외가 발생한다"() {
        given: "빈 강의 목록"
        def studentId = "12345"
        def student = Mock(Student) {
            getStudentId() >> 12345L
        }
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> []
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(12345L) >> Optional.of(student)

        when: "장바구니에 추가하면"
        cartService.addToCartBulk(request, studentId)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "존재하지 않는 학생으로 장바구니 추가 시 예외가 발생한다"() {
        given: "존재하지 않는 학생"
        def studentId = "99999"
        def request = Mock(CourseIdsRequestDto) {
            getCourseIds() >> [1L]
        }

        enrollmentPeriodRepository.existsActiveEnrollmentPeriod(_) >> true
        studentRepository.findById(99999L) >> Optional.empty()

        when: "장바구니에 추가하면"
        cartService.addToCartBulk(request, studentId)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "장바구니에서 항목을 삭제한다"() {
        given: "장바구니 항목"
        def studentId = "12345"
        def courseType = Mock(CourseType) {
            getTypeCode() >> 1
        }
        def subject = Mock(Subject) {
            getSubjectCode() >> "CS101"
            getSubjectName() >> "프로그래밍"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        def course = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
        }
        def student = Mock(Student) {
            getStudentId() >> 12345L
        }
        def cart = Mock(CourseCart) {
            getId() >> 1L
            getCourse() >> course
            getStudent() >> student
        }
        def request = Mock(CartBulkDeleteRequestDto) {
            getCartIds() >> [1L]
        }

        courseCartRepository.findAllById([1L]) >> [cart]

        when: "장바구니에서 삭제하면"
        def result = cartService.deleteFromCartBulk(request, studentId)

        then: "삭제 결과가 반환된다"
        1 * courseCartRepository.deleteAll(_)
        result.removedCount == 1
        result.removedCredits == 3
    }

    def "빈 장바구니 ID 목록으로 삭제 시 예외가 발생한다"() {
        given: "빈 cartIds"
        def studentId = "12345"
        def request = Mock(CartBulkDeleteRequestDto) {
            getCartIds() >> []
        }

        when: "삭제를 시도하면"
        cartService.deleteFromCartBulk(request, studentId)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "다른 학생의 장바구니 항목 삭제 시 예외가 발생한다"() {
        given: "다른 학생의 장바구니"
        def studentId = "12345"
        def otherStudent = Mock(Student) {
            getStudentId() >> 99999L  // 다른 학생
        }
        def subject = Mock(Subject)
        def course = Mock(Course) {
            getSubject() >> subject
        }
        def cart = Mock(CourseCart) {
            getId() >> 1L
            getCourse() >> course
            getStudent() >> otherStudent
        }
        def request = Mock(CartBulkDeleteRequestDto) {
            getCartIds() >> [1L]
        }

        courseCartRepository.findAllById([1L]) >> [cart]

        when: "삭제를 시도하면"
        cartService.deleteFromCartBulk(request, studentId)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "장바구니 전체를 삭제한다"() {
        given: "장바구니에 여러 항목"
        def studentId = "12345"
        def courseType = Mock(CourseType) {
            getTypeCode() >> 1
        }
        def subject1 = Mock(Subject) {
            getSubjectCode() >> "CS101"
            getSubjectName() >> "과목1"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        def subject2 = Mock(Subject) {
            getSubjectCode() >> "CS102"
            getSubjectName() >> "과목2"
            getCredits() >> 3
            getCourseType() >> courseType
        }
        def course1 = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject1
        }
        def course2 = Mock(Course) {
            getId() >> 2L
            getSubject() >> subject2
        }
        def cart1 = Mock(CourseCart) {
            getId() >> 1L
            getCourse() >> course1
        }
        def cart2 = Mock(CourseCart) {
            getId() >> 2L
            getCourse() >> course2
        }

        courseCartRepository.findByStudentId(12345L) >> [cart1, cart2]

        when: "전체 삭제하면"
        def result = cartService.deleteAllCart(studentId)

        then: "모든 항목이 삭제된다"
        1 * courseCartRepository.deleteByStudentId(12345L)
        result.removedCount == 2
        result.removedCredits == 6
    }
}
