package com.mzc.backend.lms.domains.course.course.service

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository
import com.mzc.backend.lms.domains.course.course.dto.CourseSearchRequestDto
import com.mzc.backend.lms.domains.course.course.entity.Course
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule
import com.mzc.backend.lms.domains.course.course.entity.CourseType
import com.mzc.backend.lms.domains.course.course.entity.CourseWeek
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository
import com.mzc.backend.lms.domains.user.organization.entity.Department
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import com.mzc.backend.lms.views.UserViewService
import spock.lang.Specification
import spock.lang.Subject as SpockSubject

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * CourseService Spock 테스트
 */
class CourseServiceSpec extends Specification {

    def courseRepository = Mock(CourseRepository)
    def userViewService = Mock(UserViewService)
    def courseWeekRepository = Mock(CourseWeekRepository)
    def subjectPrerequisitesRepository = Mock(SubjectPrerequisitesRepository)
    def enrollmentPeriodRepository = Mock(EnrollmentPeriodRepository)

    @SpockSubject
    CourseService courseService = new CourseService(
            courseRepository,
            userViewService,
            courseWeekRepository,
            subjectPrerequisitesRepository,
            enrollmentPeriodRepository
    )

    def department
    def courseType
    def subject
    def professor
    def academicTerm
    def enrollmentPeriod
    def course
    def courseSchedule

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
            getSubjectDescription() >> "프로그래밍 기초 과목입니다."
            getDescription() >> "상세 설명"
            getTheoryHours() >> 3
            getPracticeHours() >> 0
        }

        professor = Mock(Professor) {
            getProfessorId() >> 1L
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

        courseSchedule = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()

        course = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getProfessor() >> professor
            getAcademicTerm() >> academicTerm
            getSectionNumber() >> "001"
            getMaxStudents() >> 30
            getCurrentStudents() >> 10
            getDescription() >> "테스트 강의"
            getSchedules() >> [courseSchedule]
        }
    }

    def "searchCourses에서 enrollmentPeriodId가 null이면 IllegalArgumentException이 발생한다"() {
        given: "enrollmentPeriodId가 null인 요청"
        def request = CourseSearchRequestDto.builder()
                .enrollmentPeriodId(null)
                .build()

        when: "searchCourses를 호출하면"
        courseService.searchCourses(request)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "enrollmentPeriodId는 필수입니다."
    }

    def "searchCourses에서 존재하지 않는 enrollmentPeriodId면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 enrollmentPeriodId"
        def request = CourseSearchRequestDto.builder()
                .enrollmentPeriodId(999L)
                .build()

        enrollmentPeriodRepository.findById(999L) >> Optional.empty()

        when: "searchCourses를 호출하면"
        courseService.searchCourses(request)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("수강신청 기간을 찾을 수 없습니다")
    }

    def "searchCourses에서 해당 학기에 강의가 없으면 IllegalArgumentException이 발생한다"() {
        given: "강의가 없는 학기"
        def request = CourseSearchRequestDto.builder()
                .enrollmentPeriodId(1L)
                .build()

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> []

        when: "searchCourses를 호출하면"
        courseService.searchCourses(request)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "해당 학기의 강의가 없습니다."
    }

    def "searchCourses에서 강의 목록을 정상적으로 조회한다"() {
        given: "유효한 요청"
        def request = CourseSearchRequestDto.builder()
                .enrollmentPeriodId(1L)
                .page(0)
                .size(10)
                .build()

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course]
        userViewService.getUserName("1") >> "홍길동"

        when: "searchCourses를 호출하면"
        def result = courseService.searchCourses(request)

        then: "강의 목록이 반환된다"
        result != null
        result.content.size() == 1
        result.totalElements == 1
        result.currentPage == 0
        result.size == 10
    }

    def "searchCourses에서 학과로 필터링한다"() {
        given: "학과 필터가 있는 요청"
        def request = CourseSearchRequestDto.builder()
                .enrollmentPeriodId(1L)
                .departmentId(1L)
                .build()

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course]
        courseRepository.findBySubjectDepartmentId(1L) >> [course]
        userViewService.getUserName("1") >> "홍길동"

        when: "searchCourses를 호출하면"
        def result = courseService.searchCourses(request)

        then: "학과로 필터링된 강의 목록이 반환된다"
        result != null
        result.content.size() == 1
    }

    def "searchCourses에서 존재하지 않는 학과로 필터링하면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 학과 ID"
        def request = CourseSearchRequestDto.builder()
                .enrollmentPeriodId(1L)
                .departmentId(999L)
                .build()

        enrollmentPeriodRepository.findById(1L) >> Optional.of(enrollmentPeriod)
        courseRepository.findByAcademicTermId(1L) >> [course]
        courseRepository.findBySubjectDepartmentId(999L) >> []

        when: "searchCourses를 호출하면"
        courseService.searchCourses(request)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "해당 학과는 존재하지 않습니다."
    }

    def "getCourseDetailById에서 courseId가 null이면 IllegalArgumentException이 발생한다"() {
        when: "courseId가 null로 호출하면"
        courseService.getCourseDetailById(null)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message == "강의 ID는 필수입니다."
    }

    def "getCourseDetailById에서 존재하지 않는 courseId면 IllegalArgumentException이 발생한다"() {
        given: "존재하지 않는 courseId"
        courseRepository.findById(999L) >> Optional.empty()

        when: "getCourseDetailById를 호출하면"
        courseService.getCourseDetailById(999L)

        then: "IllegalArgumentException이 발생한다"
        def exception = thrown(IllegalArgumentException)
        exception.message.contains("강의를 찾을 수 없습니다")
    }

    def "getCourseDetailById에서 강의 상세 정보를 정상적으로 조회한다"() {
        given: "유효한 courseId"
        courseRepository.findById(1L) >> Optional.of(course)
        courseWeekRepository.findByCourseId(1L) >> []
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []
        userViewService.getUserName("1") >> "홍길동"

        when: "getCourseDetailById를 호출하면"
        def result = courseService.getCourseDetailById(1L)

        then: "강의 상세 정보가 반환된다"
        result != null
        result.id == 1L
        result.courseCode == "CS101"
        result.courseName == "프로그래밍 기초"
        result.credits == 3
    }

    def "getCourseDetailById에서 주차 정보도 함께 조회한다"() {
        given: "주차가 있는 강의"
        def courseWeek = Mock(CourseWeek) {
            getId() >> 1L
            getWeekNumber() >> 1
            getWeekTitle() >> "1주차 - 오리엔테이션"
        }

        courseRepository.findById(1L) >> Optional.of(course)
        courseWeekRepository.findByCourseId(1L) >> [courseWeek]
        subjectPrerequisitesRepository.findBySubjectId(1L) >> []
        userViewService.getUserName("1") >> "홍길동"

        when: "getCourseDetailById를 호출하면"
        def result = courseService.getCourseDetailById(1L)

        then: "주차 정보가 포함된다"
        result != null
        result.weeks.size() == 1
        result.weeks[0].weekNumber == 1
    }

    def "convertToCourseDto에서 Course를 CourseDto로 변환한다"() {
        given: "유효한 Course"
        userViewService.getUserName("1") >> "홍길동"

        when: "convertToCourseDto를 호출하면"
        def result = courseService.convertToCourseDto(course)

        then: "CourseDto가 반환된다"
        result != null
        result.id == 1L
        result.courseCode == "CS101"
        result.courseName == "프로그래밍 기초"
        result.section == "001"
        result.professor.name == "홍길동"
        result.department.name == "컴퓨터공학과"
        result.credits == 3
    }

    def "convertToCourseDto에서 스케줄이 정렬되어 반환된다"() {
        given: "여러 스케줄이 있는 Course"
        def schedule1 = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 30))
                .scheduleRoom("공학관 101호")
                .build()

        def schedule2 = CourseSchedule.builder()
                .scheduleId(2L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()

        def courseWithSchedules = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getProfessor() >> professor
            getAcademicTerm() >> academicTerm
            getSectionNumber() >> "001"
            getMaxStudents() >> 30
            getCurrentStudents() >> 10
            getSchedules() >> [schedule1, schedule2]
        }

        userViewService.getUserName("1") >> "홍길동"

        when: "convertToCourseDto를 호출하면"
        def result = courseService.convertToCourseDto(courseWithSchedules)

        then: "스케줄이 요일 순서로 정렬된다"
        result.schedule.size() == 2
        result.schedule[0].dayOfWeek == 1  // MONDAY
        result.schedule[1].dayOfWeek == 3  // WEDNESDAY
    }

    def "convertToCourseDto에서 수강 정원 정보가 올바르게 계산된다"() {
        given: "수강 정원이 가득 찬 Course"
        def fullCourse = Mock(Course) {
            getId() >> 1L
            getSubject() >> subject
            getProfessor() >> professor
            getAcademicTerm() >> academicTerm
            getSectionNumber() >> "001"
            getMaxStudents() >> 30
            getCurrentStudents() >> 30
            getSchedules() >> []
        }

        userViewService.getUserName("1") >> "홍길동"

        when: "convertToCourseDto를 호출하면"
        def result = courseService.convertToCourseDto(fullCourse)

        then: "isFull이 true이다"
        result.enrollment.current == 30
        result.enrollment.max == 30
        result.enrollment.isFull == true
    }

    def "convertToCourseDto에서 교수 이름이 null이면 기본값 '교수'가 설정된다"() {
        given: "교수 이름이 null인 경우"
        userViewService.getUserName("1") >> null

        when: "convertToCourseDto를 호출하면"
        def result = courseService.convertToCourseDto(course)

        then: "교수 이름이 '교수'로 설정된다"
        result.professor.name == "교수"
    }
}
