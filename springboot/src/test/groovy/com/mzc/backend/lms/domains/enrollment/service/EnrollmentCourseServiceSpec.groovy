package com.mzc.backend.lms.domains.enrollment.service

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository
import com.mzc.backend.lms.domains.course.course.entity.Course
import com.mzc.backend.lms.domains.course.course.entity.CourseType
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository
import com.mzc.backend.lms.domains.enrollment.dto.CourseSearchRequestDto
import com.mzc.backend.lms.domains.enrollment.repository.CourseCartRepository
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.domains.user.organization.entity.Department
import com.mzc.backend.lms.views.UserViewService
import spock.lang.Specification
import spock.lang.Subject as SubjectAnnotation

/**
 * EnrollmentCourseService 테스트
 * 강의 검색 및 필터링 기능 테스트
 */
class EnrollmentCourseServiceSpec extends Specification {

    def courseRepository = Mock(CourseRepository)
    def enrollmentRepository = Mock(EnrollmentRepository)
    def courseCartRepository = Mock(CourseCartRepository)
    def enrollmentPeriodRepository = Mock(EnrollmentPeriodRepository)
    def subjectPrerequisitesRepository = Mock(SubjectPrerequisitesRepository)
    def userViewService = Mock(UserViewService)

    @SubjectAnnotation
    def enrollmentCourseService = new EnrollmentCourseServiceImpl(
            courseRepository,
            enrollmentRepository,
            courseCartRepository,
            enrollmentPeriodRepository,
            subjectPrerequisitesRepository,
            userViewService
    )

    def department
    def courseType
    def subject
    def professor
    def academicTerm
    def enrollmentPeriod
    def course

    def setup() {
        department = Mock(Department) {
            getId() >> 1L
            getDepartmentName() >> "컴퓨터공학과"
        }
        courseType = Mock(CourseType) {
            getTypeCode() >> 1
        }
        subject = Mock(Subject) {
            getId() >> 1L
            getSubjectCode() >> "CS101"
            getSubjectName() >> "프로그래밍 기초"
            getCredits() >> 3
            getCourseType() >> courseType
            getDepartment() >> department
        }
        professor = Mock(Professor) {
            getProfessorId() >> 1001L
        }
        academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2025
            getTermType() >> "1"
        }
        enrollmentPeriod = Mock(EnrollmentPeriod) {
            getId() >> 1L
            getAcademicTerm() >> academicTerm
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
    }

    def "enrollmentPeriodId가 없으면 예외가 발생한다"() {
        given: "enrollmentPeriodId가 없는 요청"
        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> null
        }

        when: "강의를 검색하면"
        enrollmentCourseService.searchCourses(request, "12345")

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "존재하지 않는 수강신청 기간이면 예외가 발생한다"() {
        given: "존재하지 않는 수강신청 기간"
        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 999L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
        }
        enrollmentPeriodRepository.findById(999L) >> Optional.empty()

        when: "강의를 검색하면"
        enrollmentCourseService.searchCourses(request, "12345")

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "강의 목록을 정상적으로 조회한다"() {
        given: "유효한 검색 요청"
        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
            getDepartmentId() >> null
            getCourseType() >> null
            getCredits() >> null
            getKeyword() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course]
        userViewService.getUserName("1001") >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []

        when: "강의를 검색하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "강의 목록이 반환된다"
        result != null
        result.content.size() == 1
        result.content[0].courseCode == "CS101"
        result.content[0].courseName == "프로그래밍 기초"
        result.totalElements == 1
    }

    def "학과 필터로 강의를 검색한다"() {
        given: "학과 필터가 있는 요청"
        def otherDepartment = Mock(Department) {
            getId() >> 2L
            getDepartmentName() >> "전자공학과"
        }
        def otherSubject = Mock(Subject) {
            getId() >> 2L
            getSubjectCode() >> "EE101"
            getSubjectName() >> "회로이론"
            getCredits() >> 3
            getCourseType() >> courseType
            getDepartment() >> otherDepartment
        }
        def otherCourse = Mock(Course) {
            getId() >> 2L
            getSubject() >> otherSubject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 20
            getMaxStudents() >> 40
            getSchedules() >> []
            getAcademicTerm() >> academicTerm
        }

        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
            getDepartmentId() >> 1L  // 컴퓨터공학과만
            getCourseType() >> null
            getCredits() >> null
            getKeyword() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course, otherCourse]
        userViewService.getUserName("1001") >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []

        when: "학과 필터로 검색하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "해당 학과의 강의만 반환된다"
        result.content.size() == 1
        result.content[0].courseCode == "CS101"
    }

    def "학점 필터로 강의를 검색한다"() {
        given: "학점 필터가 있는 요청"
        def twoCreditsSubject = Mock(Subject) {
            getId() >> 2L
            getSubjectCode() >> "CS102"
            getSubjectName() >> "프로그래밍 실습"
            getCredits() >> 2
            getCourseType() >> courseType
            getDepartment() >> department
        }
        def twoCreditsCourse = Mock(Course) {
            getId() >> 2L
            getSubject() >> twoCreditsSubject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 20
            getMaxStudents() >> 40
            getSchedules() >> []
            getAcademicTerm() >> academicTerm
        }

        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
            getDepartmentId() >> null
            getCourseType() >> null
            getCredits() >> 3  // 3학점만
            getKeyword() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course, twoCreditsCourse]
        userViewService.getUserName("1001") >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []

        when: "학점 필터로 검색하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "해당 학점의 강의만 반환된다"
        result.content.size() == 1
        result.content[0].credits == 3
    }

    def "키워드로 강의를 검색한다"() {
        given: "키워드가 있는 요청"
        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
            getDepartmentId() >> null
            getCourseType() >> null
            getCredits() >> null
            getKeyword() >> "프로그래밍"
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course]
        userViewService.getUserName("1001") >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []

        when: "키워드로 검색하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "키워드가 포함된 강의가 반환된다"
        result.content.size() == 1
        result.content[0].courseName.contains("프로그래밍")
    }

    def "정원이 마감된 강의는 canEnroll이 false이다"() {
        given: "정원이 마감된 강의"
        def fullCourse = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getSectionNumber() >> "001"
            getProfessor() >> professor
            getCurrentStudents() >> 40
            getMaxStudents() >> 40
            getSchedules() >> []
            getAcademicTerm() >> academicTerm
        }

        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
            getDepartmentId() >> null
            getCourseType() >> null
            getCredits() >> null
            getKeyword() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [fullCourse]
        userViewService.getUserName("1001") >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []

        when: "정원이 마감된 강의를 조회하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "canEnroll이 false이다"
        result.content[0].canEnroll == false
        result.content[0].enrollment.isFull == true
    }

    def "이미 수강신청한 강의는 isEnrolled가 true이다"() {
        given: "이미 수강신청한 강의"
        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
            getDepartmentId() >> null
            getCourseType() >> null
            getCredits() >> null
            getKeyword() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course]
        userViewService.getUserName("1001") >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> true  // 이미 수강신청
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []

        when: "이미 수강신청한 강의를 조회하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "isEnrolled가 true이다"
        result.content[0].isEnrolled == true
        result.content[0].canEnroll == false
    }

    def "장바구니에 담긴 강의는 isInCart가 true이다"() {
        given: "장바구니에 담긴 강의"
        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 0
            getSize() >> 20
            getSort() >> null
            getDepartmentId() >> null
            getCourseType() >> null
            getCredits() >> null
            getKeyword() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course]
        userViewService.getUserName("1001") >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(12345L, 1L) >> true  // 장바구니에 있음
        enrollmentRepository.existsByStudentIdAndCourseId(12345L, 1L) >> false
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []

        when: "장바구니에 담긴 강의를 조회하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "isInCart가 true이다"
        result.content[0].isInCart == true
    }

    def "페이징이 정상적으로 동작한다"() {
        given: "여러 강의와 페이징 요청"
        def courses = (1..25).collect { i ->
            def s = Mock(Subject) {
                getId() >> (long) i
                getSubjectCode() >> "CS${100 + i}"
                getSubjectName() >> "강의${i}"
                getCredits() >> 3
                getCourseType() >> courseType
                getDepartment() >> department
            }
            Mock(Course) {
                getId() >> (long) i
                getSubject() >> s
                getSectionNumber() >> "001"
                getProfessor() >> professor
                getCurrentStudents() >> 20
                getMaxStudents() >> 40
                getSchedules() >> []
                getAcademicTerm() >> academicTerm
            }
        }

        def request = Mock(CourseSearchRequestDto) {
            getEnrollmentPeriodId() >> 1L
            getPage() >> 1  // 두 번째 페이지
            getSize() >> 10
            getSort() >> null
            getDepartmentId() >> null
            getCourseType() >> null
            getCredits() >> null
            getKeyword() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> courses
        userViewService.getUserName(_) >> "김교수"
        courseCartRepository.existsByStudentIdAndCourseId(_, _) >> false
        enrollmentRepository.existsByStudentIdAndCourseId(_, _) >> false
        subjectPrerequisitesRepository.findBySubjectId(_) >> []

        when: "두 번째 페이지를 요청하면"
        def result = enrollmentCourseService.searchCourses(request, "12345")

        then: "페이징이 적용된 결과가 반환된다"
        result.content.size() == 10
        result.totalElements == 25
        result.totalPages == 3
        result.currentPage == 1
    }
}
