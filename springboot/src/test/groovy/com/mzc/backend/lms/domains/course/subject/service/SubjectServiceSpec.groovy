package com.mzc.backend.lms.domains.course.subject.service

import com.mzc.backend.lms.domains.course.course.entity.CourseType
import com.mzc.backend.lms.domains.course.course.repository.CourseTypeRepository
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.course.subject.repository.SubjectRepository
import com.mzc.backend.lms.domains.user.organization.entity.College
import com.mzc.backend.lms.domains.user.organization.entity.Department
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import spock.lang.Specification
import spock.lang.Subject as SubjectAnnotation

/**
 * SubjectService 테스트
 * 과목 조회, 검색 기능 테스트
 */
class SubjectServiceSpec extends Specification {

    def subjectRepository = Mock(SubjectRepository)
    def professorDepartmentRepository = Mock(ProfessorDepartmentRepository)
    def courseTypeRepository = Mock(CourseTypeRepository)

    @SubjectAnnotation
    def subjectService = new SubjectServiceImpl(
            subjectRepository,
            professorDepartmentRepository,
            courseTypeRepository
    )

    def college
    def department
    def courseType
    def subject

    def setup() {
        college = Mock(College) {
            getCollegeName() >> "공과대학"
        }
        department = Mock(Department) {
            getId() >> 1L
            getDepartmentName() >> "컴퓨터공학과"
            getCollege() >> college
        }
        courseType = Mock(CourseType) {
            getId() >> 1L
            getTypeCode() >> 1
            getTypeCodeString() >> "MAJOR"
            getTypeName() >> "전공필수"
            getColor() >> "#FF0000"
        }
        subject = Mock(Subject) {
            getId() >> 1L
            getSubjectCode() >> "CS101"
            getSubjectName() >> "프로그래밍 기초"
            getCredits() >> 3
            getDescription() >> "프로그래밍 입문 과목"
            getDepartment() >> department
            getCourseType() >> courseType
            getPrerequisites() >> []
            getCourses() >> []
            getCreatedAt() >> null
        }
    }

    // ==================== 과목 목록 조회 테스트 ====================

    def "학생은 전체 과목을 조회한다"() {
        given: "학생 사용자"
        def pageable = PageRequest.of(0, 20)
        def subjects = new PageImpl<>([subject], pageable, 1)

        subjectRepository.findSubjectsWithFilters(null, null, null, null, pageable) >> subjects

        when: "과목 목록을 조회하면"
        def result = subjectService.getSubjects(
                100L, "STUDENT", null, null, null, null, null, null, pageable)

        then: "과목 목록이 반환된다"
        result.content.size() == 1
        result.content[0].subjectCode == "CS101"
        result.content[0].subjectName == "프로그래밍 기초"
    }

    def "교수는 기본적으로 자기 학과 과목만 조회한다"() {
        given: "교수 사용자와 소속 학과"
        def pageable = PageRequest.of(0, 20)
        def profDept = Mock(ProfessorDepartment) {
            getDepartment() >> department
        }
        def subjects = new PageImpl<>([subject], pageable, 1)

        professorDepartmentRepository.findByProfessorId(1001L) >> Optional.of(profDept)
        subjectRepository.findSubjectsWithFilters(1L, null, null, null, pageable) >> subjects

        when: "과목 목록을 조회하면"
        def result = subjectService.getSubjects(
                1001L, "PROFESSOR", null, null, false, null, null, null, pageable)

        then: "자기 학과 과목만 반환된다"
        result.content.size() == 1
    }

    def "교수가 전체 학과 조회 옵션을 사용하면 모든 과목이 조회된다"() {
        given: "전체 학과 조회 옵션"
        def pageable = PageRequest.of(0, 20)
        def subjects = new PageImpl<>([subject], pageable, 1)

        subjectRepository.findSubjectsWithFilters(null, null, null, null, pageable) >> subjects

        when: "전체 학과 조회 옵션으로 과목을 조회하면"
        def result = subjectService.getSubjects(
                1001L, "PROFESSOR", null, null, true, null, null, null, pageable)

        then: "모든 과목이 반환된다"
        result.content.size() == 1
        0 * professorDepartmentRepository.findByProfessorId(_)
    }

    def "키워드로 과목을 필터링한다"() {
        given: "키워드 검색"
        def pageable = PageRequest.of(0, 20)
        def subjects = new PageImpl<>([subject], pageable, 1)

        subjectRepository.findSubjectsWithFilters(null, "프로그래밍", null, null, pageable) >> subjects

        when: "키워드로 과목을 조회하면"
        def result = subjectService.getSubjects(
                100L, "STUDENT", "프로그래밍", null, null, null, null, null, pageable)

        then: "필터링된 과목이 반환된다"
        result.content.size() == 1
        result.content[0].subjectName.contains("프로그래밍")
    }

    def "학점으로 과목을 필터링한다"() {
        given: "학점 필터"
        def pageable = PageRequest.of(0, 20)
        def subjects = new PageImpl<>([subject], pageable, 1)

        subjectRepository.findSubjectsWithFilters(null, null, null, 3, pageable) >> subjects

        when: "학점으로 과목을 조회하면"
        def result = subjectService.getSubjects(
                100L, "STUDENT", null, null, null, null, 3, null, pageable)

        then: "해당 학점 과목이 반환된다"
        result.content.size() == 1
        result.content[0].credits == 3
    }

    def "이수구분으로 과목을 필터링한다"() {
        given: "이수구분 필터"
        def pageable = PageRequest.of(0, 20)
        def subjects = new PageImpl<>([subject], pageable, 1)

        courseTypeRepository.findByTypeCode(1) >> Optional.of(courseType)
        subjectRepository.findSubjectsWithFilters(null, null, 1L, null, pageable) >> subjects

        when: "이수구분으로 과목을 조회하면"
        def result = subjectService.getSubjects(
                100L, "STUDENT", null, null, null, "MAJOR_REQ", null, null, pageable)

        then: "해당 이수구분 과목이 반환된다"
        result.content.size() == 1
    }

    // ==================== 과목 상세 조회 테스트 ====================

    def "과목 상세를 조회한다"() {
        given: "존재하는 과목"
        subjectRepository.findByIdWithDetails(1L) >> Optional.of(subject)

        when: "과목 상세를 조회하면"
        def result = subjectService.getSubjectDetail(1L)

        then: "상세 정보가 반환된다"
        result != null
        result.id == 1L
        result.subjectCode == "CS101"
        result.subjectName == "프로그래밍 기초"
        result.credits == 3
        result.department.name == "컴퓨터공학과"
    }

    def "존재하지 않는 과목 조회 시 예외가 발생한다"() {
        given: "존재하지 않는 과목"
        subjectRepository.findByIdWithDetails(999L) >> Optional.empty()

        when: "과목 상세를 조회하면"
        subjectService.getSubjectDetail(999L)

        then: "예외가 발생한다"
        thrown(RuntimeException)
    }

    // ==================== 과목 검색 테스트 ====================

    def "과목을 검색한다"() {
        given: "검색 쿼리"
        def pageable = PageRequest.of(0, 20)
        def subjects = new PageImpl<>([subject], pageable, 1)

        subjectRepository.searchSubjects("프로그래밍", pageable) >> subjects

        when: "과목을 검색하면"
        def result = subjectService.searchSubjects("프로그래밍", pageable)

        then: "검색 결과가 반환된다"
        result.content.size() == 1
        result.content[0].subjectName == "프로그래밍 기초"
    }

    def "검색어가 2글자 미만이면 예외가 발생한다"() {
        given: "짧은 검색어"
        def pageable = PageRequest.of(0, 20)

        when: "1글자로 검색하면"
        subjectService.searchSubjects("프", pageable)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "검색어가 null이면 예외가 발생한다"() {
        given: "null 검색어"
        def pageable = PageRequest.of(0, 20)

        when: "null로 검색하면"
        subjectService.searchSubjects(null, pageable)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "검색어 앞뒤 공백을 제거하고 검색한다"() {
        given: "공백이 포함된 검색어"
        def pageable = PageRequest.of(0, 20)
        def subjects = new PageImpl<>([subject], pageable, 1)

        subjectRepository.searchSubjects("프로그래밍", pageable) >> subjects

        when: "앞뒤 공백이 있는 검색어로 검색하면"
        def result = subjectService.searchSubjects("  프로그래밍  ", pageable)

        then: "공백이 제거된 검색어로 검색된다"
        result.content.size() == 1
    }
}
