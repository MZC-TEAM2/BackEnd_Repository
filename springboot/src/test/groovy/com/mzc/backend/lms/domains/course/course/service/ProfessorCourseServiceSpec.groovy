package com.mzc.backend.lms.domains.course.course.service

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod
import com.mzc.backend.lms.domains.academy.repository.AcademicTermRepository
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository
import com.mzc.backend.lms.domains.course.course.dto.*
import com.mzc.backend.lms.domains.course.course.entity.Course
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule
import com.mzc.backend.lms.domains.course.course.entity.CourseType
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository
import com.mzc.backend.lms.domains.course.course.repository.CourseTypeRepository
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.course.subject.repository.SubjectRepository
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository
import com.mzc.backend.lms.domains.user.organization.entity.Department
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository
import spock.lang.Specification
import spock.lang.Subject as SpockSubject

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * ProfessorCourseService Spock 테스트
 */
class ProfessorCourseServiceSpec extends Specification {

    def courseRepository = Mock(CourseRepository)
    def subjectRepository = Mock(SubjectRepository)
    def academicTermRepository = Mock(AcademicTermRepository)
    def professorRepository = Mock(ProfessorRepository)
    def professorDepartmentRepository = Mock(ProfessorDepartmentRepository)
    def enrollmentRepository = Mock(EnrollmentRepository)
    def enrollmentPeriodRepository = Mock(EnrollmentPeriodRepository)
    def courseService = Mock(CourseService)
    def courseTypeRepository = Mock(CourseTypeRepository)
    def courseWeekRepository = Mock(CourseWeekRepository)

    @SpockSubject
    ProfessorCourseService professorCourseService = new ProfessorCourseService(
            courseRepository,
            subjectRepository,
            academicTermRepository,
            professorRepository,
            professorDepartmentRepository,
            enrollmentRepository,
            enrollmentPeriodRepository,
            courseService,
            courseTypeRepository,
            courseWeekRepository
    )

    def department
    def courseType
    def subject
    def professor
    def professorDepartment
    def academicTerm
    def enrollmentPeriod
    def course

    def setup() {
        department = Mock(Department) {
            getId() >> 1L
            getDepartmentName() >> "컴퓨터공학과"
        }

        courseType = Mock(CourseType) {
            getId() >> 1L
            getTypeCode() >> 1
            getCategory() >> 0
        }

        subject = Mock(Subject) {
            getId() >> 1L
            getSubjectCode() >> "CS101"
            getSubjectName() >> "프로그래밍 기초"
            getCredits() >> 3
            getDepartment() >> department
            getCourseType() >> courseType
        }

        professor = Mock(Professor) {
            getProfessorId() >> 1L
        }

        professorDepartment = Mock(ProfessorDepartment) {
            getDepartment() >> department
            getIsPrimary() >> true
        }

        academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2025
            getTermType() >> "1학기"
            getStartDate() >> LocalDate.of(2025, 3, 1)
            getEndDate() >> LocalDate.of(2025, 6, 30)
        }

        enrollmentPeriod = Mock(EnrollmentPeriod) {
            getId() >> 1L
            getAcademicTerm() >> academicTerm
        }

        course = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getProfessor() >> professor
            getAcademicTerm() >> academicTerm
            getSectionNumber() >> "001"
            getMaxStudents() >> 30
            getCurrentStudents() >> 0
            getSchedules() >> []
            getCreatedAt() >> null
        }
    }

    // createCourse 테스트

    def "createCourse에서 request가 null이면 IllegalArgumentException이 발생한다"() {
        when: "request가 null로 호출하면"
        professorCourseService.createCourse(null, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "요청 정보는 필수입니다."
    }

    def "createCourse에서 professorId가 null이면 IllegalArgumentException이 발생한다"() {
        given: "유효한 request"
        def request = CreateCourseRequestDto.builder()
                .enrollmentPeriodId(1L)
                .subjectId(1L)
                .section("001")
                .maxStudents(30)
                .build()

        when: "professorId가 null로 호출하면"
        professorCourseService.createCourse(request, null)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "교수 ID는 필수입니다."
    }

    def "createCourse에서 subjectId와 subject 둘 다 없으면 IllegalArgumentException이 발생한다"() {
        given: "subjectId와 subject 둘 다 없는 request"
        def request = CreateCourseRequestDto.builder()
                .enrollmentPeriodId(1L)
                .section("001")
                .maxStudents(30)
                .build()

        when: "createCourse를 호출하면"
        professorCourseService.createCourse(request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "subjectId와 subject 중 하나만 제공해야 합니다."
    }

    def "createCourse에서 subjectId와 subject 둘 다 있으면 IllegalArgumentException이 발생한다"() {
        given: "subjectId와 subject 둘 다 있는 request"
        def subjectRequest = CreateCourseRequestDto.SubjectRequestDto.builder()
                .subjectCode("CS102")
                .subjectName("새 과목")
                .credits(3)
                .courseType("MAJOR_REQ")
                .build()

        def request = CreateCourseRequestDto.builder()
                .enrollmentPeriodId(1L)
                .subjectId(1L)
                .subject(subjectRequest)
                .section("001")
                .maxStudents(30)
                .build()

        when: "createCourse를 호출하면"
        professorCourseService.createCourse(request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "subjectId와 subject 중 하나만 제공해야 합니다."
    }

    def "createCourse에서 enrollmentPeriodId가 null이면 IllegalArgumentException이 발생한다"() {
        given: "enrollmentPeriodId가 null인 request"
        def request = CreateCourseRequestDto.builder()
                .enrollmentPeriodId(null)
                .subjectId(1L)
                .section("001")
                .maxStudents(30)
                .build()

        when: "createCourse를 호출하면"
        professorCourseService.createCourse(request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "수강신청 기간 ID는 필수입니다."
    }

    def "createCourse에서 존재하지 않는 enrollmentPeriodId면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 enrollmentPeriodId"
        def request = CreateCourseRequestDto.builder()
                .enrollmentPeriodId(999L)
                .subjectId(1L)
                .section("001")
                .maxStudents(30)
                .build()

        enrollmentPeriodRepository.findById(999L) >> Optional.empty()

        when: "createCourse를 호출하면"
        professorCourseService.createCourse(request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "수강신청 기간을 찾을 수 없습니다."
    }

    def "createCourse에서 academicTerm이 null이면 IllegalArgumentException이 발생한다"() {
        given: "academicTerm이 null인 enrollmentPeriod"
        def request = CreateCourseRequestDto.builder()
                .enrollmentPeriodId(1L)
                .subjectId(1L)
                .section("001")
                .maxStudents(30)
                .build()

        def noTermEnrollmentPeriod = Mock(EnrollmentPeriod) {
            getAcademicTerm() >> null
        }

        enrollmentPeriodRepository.findById(1L) >> Optional.of(noTermEnrollmentPeriod)

        when: "createCourse를 호출하면"
        professorCourseService.createCourse(request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "수강신청 기간에 연결된 학기 정보가 없습니다."
    }

    def "createCourse에서 교수를 찾을 수 없으면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 professorId"
        def request = CreateCourseRequestDto.builder()
                .enrollmentPeriodId(1L)
                .subjectId(1L)
                .section("001")
                .maxStudents(30)
                .build()

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        professorRepository.findById(999L) >> Optional.empty()

        when: "createCourse를 호출하면"
        professorCourseService.createCourse(request, 999L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "교수를 찾을 수 없습니다."
    }

    // updateCourse 테스트

    def "updateCourse에서 courseId가 null이면 IllegalArgumentException이 발생한다"() {
        given: "유효한 request"
        def request = UpdateCourseRequestDto.builder()
                .maxStudents(40)
                .build()

        when: "courseId가 null로 호출하면"
        professorCourseService.updateCourse(null, request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의 ID는 필수입니다."
    }

    def "updateCourse에서 professorId가 null이면 IllegalArgumentException이 발생한다"() {
        given: "유효한 request"
        def request = UpdateCourseRequestDto.builder()
                .maxStudents(40)
                .build()

        when: "professorId가 null로 호출하면"
        professorCourseService.updateCourse(1L, request, null)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "교수 ID는 필수입니다."
    }

    def "updateCourse에서 request가 null이면 IllegalArgumentException이 발생한다"() {
        when: "request가 null로 호출하면"
        professorCourseService.updateCourse(1L, null, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "요청 정보는 필수입니다."
    }

    def "updateCourse에서 강의를 찾을 수 없으면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 courseId"
        def request = UpdateCourseRequestDto.builder()
                .maxStudents(40)
                .build()

        courseRepository.findById(999L) >> Optional.empty()

        when: "updateCourse를 호출하면"
        professorCourseService.updateCourse(999L, request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의를 찾을 수 없습니다."
    }

    def "updateCourse에서 권한이 없으면 IllegalArgumentException이 발생한다"() {
        given: "다른 교수의 강의"
        def request = UpdateCourseRequestDto.builder()
                .maxStudents(40)
                .build()

        courseRepository.findById(1L) >> Optional.of(course)

        when: "다른 교수가 수정을 시도하면"
        professorCourseService.updateCourse(1L, request, 999L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의 수정 권한이 없습니다."
    }

    def "updateCourse에서 정원이 현재 수강생보다 적으면 IllegalArgumentException이 발생한다"() {
        given: "현재 수강생이 있는 강의"
        def request = UpdateCourseRequestDto.builder()
                .maxStudents(5)
                .build()

        def courseWithStudents = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getProfessor() >> professor
            getAcademicTerm() >> academicTerm
            getSectionNumber() >> "001"
            getMaxStudents() >> 30
            getCurrentStudents() >> 10
            getSchedules() >> []
        }

        courseRepository.findById(1L) >> Optional.of(courseWithStudents)

        when: "정원을 현재 수강생보다 적게 수정하면"
        professorCourseService.updateCourse(1L, request, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("현재 수강생 수")
    }

    // cancelCourse 테스트

    def "cancelCourse에서 강의를 찾을 수 없으면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 courseId"
        courseRepository.findById(999L) >> Optional.empty()

        when: "cancelCourse를 호출하면"
        professorCourseService.cancelCourse(999L, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의를 찾을 수 없습니다."
    }

    def "cancelCourse에서 권한이 없으면 IllegalArgumentException이 발생한다"() {
        given: "다른 교수의 강의"
        courseRepository.findById(1L) >> Optional.of(course)

        when: "다른 교수가 취소를 시도하면"
        professorCourseService.cancelCourse(1L, 999L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의 취소 권한이 없습니다."
    }

    def "cancelCourse에서 수강생이 있으면 IllegalArgumentException이 발생한다"() {
        given: "수강생이 있는 강의"
        courseRepository.findById(1L) >> Optional.of(course)
        enrollmentRepository.findByCourseId(1L) >> [Mock(Object)]

        when: "cancelCourse를 호출하면"
        professorCourseService.cancelCourse(1L, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("수강생이")
    }

    def "cancelCourse에서 수강생이 없으면 정상적으로 삭제된다"() {
        given: "수강생이 없는 강의"
        courseRepository.findById(1L) >> Optional.of(course)
        enrollmentRepository.findByCourseId(1L) >> []

        when: "cancelCourse를 호출하면"
        professorCourseService.cancelCourse(1L, 1L)

        then: "강의가 삭제된다"
        1 * courseRepository.delete(course)
    }

    // getMyCourses 테스트

    def "getMyCourses에서 academicTermId가 null이면 모든 강의를 조회한다"() {
        given: "academicTermId가 null"
        courseRepository.findByProfessorProfessorId(1L) >> [course]
        courseService.convertToCourseDto(course) >> CourseDto.builder()
                .id(1L)
                .courseCode("CS101")
                .courseName("프로그래밍 기초")
                .build()

        when: "getMyCourses를 호출하면"
        def result = professorCourseService.getMyCourses(1L, null)

        then: "모든 강의가 조회된다"
        result != null
        result.totalCourses == 1
        result.term == null
    }

    def "getMyCourses에서 academicTermId가 있으면 해당 학기 강의만 조회한다"() {
        given: "academicTermId가 있음"
        academicTermRepository.findById(1L) >> Optional.of(academicTerm)
        courseRepository.findByProfessorProfessorIdAndAcademicTermId(1L, 1L) >> [course]
        courseService.convertToCourseDto(course) >> CourseDto.builder()
                .id(1L)
                .courseCode("CS101")
                .courseName("프로그래밍 기초")
                .build()

        when: "getMyCourses를 호출하면"
        def result = professorCourseService.getMyCourses(1L, 1L)

        then: "해당 학기 강의만 조회된다"
        result != null
        result.totalCourses == 1
        result.term != null
        result.term.id == 1L
    }

    def "getMyCourses에서 존재하지 않는 academicTermId면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 academicTermId"
        academicTermRepository.findById(999L) >> Optional.empty()

        when: "getMyCourses를 호출하면"
        professorCourseService.getMyCourses(1L, 999L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "학기를 찾을 수 없습니다."
    }

    // getCourseDetail 테스트

    def "getCourseDetail에서 강의를 찾을 수 없으면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 courseId"
        courseRepository.findById(999L) >> Optional.empty()

        when: "getCourseDetail을 호출하면"
        professorCourseService.getCourseDetail(999L, 1L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의를 찾을 수 없습니다."
    }

    def "getCourseDetail에서 권한이 없으면 IllegalArgumentException이 발생한다"() {
        given: "다른 교수의 강의"
        courseRepository.findById(1L) >> Optional.of(course)

        when: "다른 교수가 조회를 시도하면"
        professorCourseService.getCourseDetail(1L, 999L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의 조회 권한이 없습니다."
    }

    def "getCourseDetail에서 정상적으로 상세 정보를 조회한다"() {
        given: "유효한 courseId"
        courseRepository.findById(1L) >> Optional.of(course)
        courseService.convertToCourseDto(course) >> CourseDto.builder()
                .id(1L)
                .courseCode("CS101")
                .courseName("프로그래밍 기초")
                .section("001")
                .department(DepartmentDto.builder().id(1L).name("컴퓨터공학과").build())
                .credits(3)
                .courseType(CourseTypeDto.builder().code("MAJOR_REQ").name("전공필수").build())
                .schedule([])
                .build()

        when: "getCourseDetail을 호출하면"
        def result = professorCourseService.getCourseDetail(1L, 1L)

        then: "강의 상세 정보가 반환된다"
        result != null
        result.id == 1L
        result.courseCode == "CS101"
        result.courseName == "프로그래밍 기초"
        result.maxStudents == 30
        result.currentStudents == 0
    }
}
