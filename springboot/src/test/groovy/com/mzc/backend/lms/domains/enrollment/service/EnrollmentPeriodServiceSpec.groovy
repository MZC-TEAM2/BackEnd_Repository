package com.mzc.backend.lms.domains.enrollment.service

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod
import com.mzc.backend.lms.domains.academy.entity.PeriodType
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository
import com.mzc.backend.lms.domains.academy.repository.PeriodTypeRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

/**
 * EnrollmentPeriodService 테스트
 * 수강신청 기간 조회 기능 테스트
 */
class EnrollmentPeriodServiceSpec extends Specification {

    def enrollmentPeriodRepository = Mock(EnrollmentPeriodRepository)
    def periodTypeRepository = Mock(PeriodTypeRepository)

    @Subject
    def enrollmentPeriodService = new EnrollmentPeriodServiceImpl(
            enrollmentPeriodRepository,
            periodTypeRepository
    )

    def "현재 활성화된 수강신청 기간이 없으면 isActive=false를 반환한다"() {
        given: "활성화된 기간이 없음"
        enrollmentPeriodRepository.findFirstActivePeriod(_) >> Optional.empty()

        when: "현재 기간을 조회하면"
        def result = enrollmentPeriodService.getCurrentPeriod(null)

        then: "isActive=false가 반환된다"
        result.isActive == false
        result.currentPeriod == null
    }

    def "유효하지 않은 기간 타입 코드로 조회 시 예외가 발생한다"() {
        given: "유효하지 않은 타입 코드"
        def invalidTypeCode = "INVALID_TYPE"
        periodTypeRepository.existsByTypeCode("INVALID_TYPE") >> false

        when: "기간을 조회하면"
        enrollmentPeriodService.getCurrentPeriod(invalidTypeCode)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "활성화된 수강신청 기간이 있으면 정보를 반환한다"() {
        given: "활성화된 수강신청 기간"
        def now = LocalDateTime.now()
        def startDatetime = now.minusDays(1)
        def endDatetime = now.plusDays(5)

        def academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2024
            getTermType() >> "1"
        }

        def periodType = Mock(PeriodType) {
            getId() >> 1L
            getTypeCode() >> "ENROLLMENT"
            getTypeName() >> "수강신청"
            getDescription() >> "수강신청 기간"
        }

        def enrollmentPeriod = Mock(EnrollmentPeriod) {
            getId() >> 1L
            getAcademicTerm() >> academicTerm
            getPeriodName() >> "2024학년도 1학기 수강신청"
            getPeriodType() >> periodType
            getStartDatetime() >> startDatetime
            getEndDatetime() >> endDatetime
            getTargetYear() >> 0
        }

        enrollmentPeriodRepository.findFirstActivePeriod(_) >> Optional.of(enrollmentPeriod)

        when: "현재 기간을 조회하면"
        def result = enrollmentPeriodService.getCurrentPeriod(null)

        then: "기간 정보가 반환된다"
        result.isActive == true
        result.currentPeriod != null
        result.currentPeriod.id == 1L
        result.currentPeriod.periodName == "2024학년도 1학기 수강신청"
    }

    def "특정 타입 코드로 활성화된 기간을 조회한다"() {
        given: "특정 타입의 활성화된 기간"
        def typeCode = "ENROLLMENT"
        def now = LocalDateTime.now()

        def academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2024
            getTermType() >> "2"
        }

        def periodType = Mock(PeriodType) {
            getId() >> 1L
            getTypeCode() >> "ENROLLMENT"
            getTypeName() >> "수강신청"
            getDescription() >> "수강신청 기간"
        }

        def enrollmentPeriod = Mock(EnrollmentPeriod) {
            getId() >> 2L
            getAcademicTerm() >> academicTerm
            getPeriodName() >> "2024학년도 2학기 수강신청"
            getPeriodType() >> periodType
            getStartDatetime() >> now.minusHours(1)
            getEndDatetime() >> now.plusDays(3)
            getTargetYear() >> 2
        }

        periodTypeRepository.existsByTypeCode("ENROLLMENT") >> true
        enrollmentPeriodRepository.findFirstActivePeriodByTypeCode("ENROLLMENT", _) >> Optional.of(enrollmentPeriod)

        when: "특정 타입으로 기간을 조회하면"
        def result = enrollmentPeriodService.getCurrentPeriod(typeCode)

        then: "해당 타입의 기간 정보가 반환된다"
        result.isActive == true
        result.currentPeriod != null
        result.currentPeriod.periodType.typeCode == "ENROLLMENT"
        result.currentPeriod.targetYear == 2
    }

    def "학기 타입 이름이 올바르게 변환된다"() {
        given: "다양한 학기 타입"
        def now = LocalDateTime.now()

        def academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2024
            getTermType() >> termType
        }

        def enrollmentPeriod = Mock(EnrollmentPeriod) {
            getId() >> 1L
            getAcademicTerm() >> academicTerm
            getPeriodName() >> "테스트 기간"
            getPeriodType() >> null
            getStartDatetime() >> now.minusHours(1)
            getEndDatetime() >> now.plusDays(1)
            getTargetYear() >> 0
        }

        enrollmentPeriodRepository.findFirstActivePeriod(_) >> Optional.of(enrollmentPeriod)

        when: "기간을 조회하면"
        def result = enrollmentPeriodService.getCurrentPeriod(null)

        then: "학기 이름이 올바르게 변환된다"
        result.currentPeriod.term.termName == expectedTermName

        where:
        termType | expectedTermName
        "1"      | "2024학년도 1학기"
        "2"      | "2024학년도 2학기"
        "SUMMER" | "2024학년도 여름학기"
        "WINTER" | "2024학년도 겨울학기"
    }

    def "남은 시간이 올바르게 계산된다"() {
        given: "활성화된 기간 (종료까지 2일 3시간 30분 남음)"
        def now = LocalDateTime.now()
        def endDatetime = now.plusDays(2).plusHours(3).plusMinutes(30)

        def academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2024
            getTermType() >> "1"
        }

        def enrollmentPeriod = Mock(EnrollmentPeriod) {
            getId() >> 1L
            getAcademicTerm() >> academicTerm
            getPeriodName() >> "테스트"
            getPeriodType() >> null
            getStartDatetime() >> now.minusDays(1)
            getEndDatetime() >> endDatetime
            getTargetYear() >> 0
        }

        enrollmentPeriodRepository.findFirstActivePeriod(_) >> Optional.of(enrollmentPeriod)

        when: "기간을 조회하면"
        def result = enrollmentPeriodService.getCurrentPeriod(null)

        then: "남은 시간이 반환된다"
        result.currentPeriod.remainingTime != null
        result.currentPeriod.remainingTime.days >= 2
        result.currentPeriod.remainingTime.totalSeconds > 0
    }

    def "getCurrentEnrollmentPeriod()은 ENROLLMENT 타입으로 조회한다"() {
        given: "활성화된 수강신청 기간"
        def now = LocalDateTime.now()

        def academicTerm = Mock(AcademicTerm) {
            getId() >> 1L
            getYear() >> 2024
            getTermType() >> "1"
        }

        def periodType = Mock(PeriodType) {
            getId() >> 1L
            getTypeCode() >> "ENROLLMENT"
            getTypeName() >> "수강신청"
            getDescription() >> "수강신청 기간"
        }

        def enrollmentPeriod = Mock(EnrollmentPeriod) {
            getId() >> 1L
            getAcademicTerm() >> academicTerm
            getPeriodName() >> "수강신청 기간"
            getPeriodType() >> periodType
            getStartDatetime() >> now.minusHours(1)
            getEndDatetime() >> now.plusDays(1)
            getTargetYear() >> 0
        }

        periodTypeRepository.existsByTypeCode("ENROLLMENT") >> true
        enrollmentPeriodRepository.findFirstActivePeriodByTypeCode("ENROLLMENT", _) >> Optional.of(enrollmentPeriod)

        when: "deprecated 메서드를 호출하면"
        def result = enrollmentPeriodService.getCurrentEnrollmentPeriod()

        then: "ENROLLMENT 타입으로 조회된다"
        result.isActive == true
        result.currentPeriod.periodType.typeCode == "ENROLLMENT"
    }
}
