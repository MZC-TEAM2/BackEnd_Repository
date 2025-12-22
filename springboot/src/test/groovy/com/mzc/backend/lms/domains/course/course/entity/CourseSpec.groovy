package com.mzc.backend.lms.domains.course.course.entity

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm
import com.mzc.backend.lms.domains.course.subject.entity.Subject
import com.mzc.backend.lms.domains.user.professor.entity.Professor
import spock.lang.Specification
import spock.lang.Subject as SpockSubject

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Course 엔티티 Spock 테스트
 */
class CourseSpec extends Specification {

    def subject = Mock(Subject)
    def professor = Mock(Professor)
    def academicTerm = Mock(AcademicTerm)

    @SpockSubject
    Course course

    def setup() {
        course = Course.builder()
                .id(1L)
                .subject(subject)
                .professor(professor)
                .academicTerm(academicTerm)
                .sectionNumber("001")
                .maxStudents(30)
                .currentStudents(0)
                .description("테스트 강의 설명")
                .build()
    }

    def "Course 객체가 올바르게 생성된다"() {
        expect: "모든 필드가 올바르게 설정된다"
        course.id == 1L
        course.subject == subject
        course.professor == professor
        course.academicTerm == academicTerm
        course.sectionNumber == "001"
        course.maxStudents == 30
        course.currentStudents == 0
        course.description == "테스트 강의 설명"
        course.schedules != null
        course.schedules.isEmpty()
    }

    def "addSchedule 메서드로 스케줄을 추가할 수 있다"() {
        given: "새로운 스케줄"
        def schedule = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()

        when: "스케줄을 추가하면"
        course.addSchedule(schedule)

        then: "스케줄이 추가되고 양방향 연관관계가 설정된다"
        course.schedules.size() == 1
        course.schedules.contains(schedule)
        schedule.course == course
    }

    def "addSchedule로 여러 스케줄을 추가할 수 있다"() {
        given: "여러 스케줄"
        def schedule1 = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()

        def schedule2 = CourseSchedule.builder()
                .scheduleId(2L)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()

        when: "여러 스케줄을 추가하면"
        course.addSchedule(schedule1)
        course.addSchedule(schedule2)

        then: "모든 스케줄이 추가된다"
        course.schedules.size() == 2
        course.schedules.containsAll([schedule1, schedule2])
    }

    def "removeSchedule 메서드로 스케줄을 제거할 수 있다"() {
        given: "스케줄이 추가된 상태"
        def schedule = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()
        course.addSchedule(schedule)

        when: "스케줄을 제거하면"
        course.removeSchedule(schedule)

        then: "스케줄이 제거되고 연관관계가 해제된다"
        course.schedules.isEmpty()
        schedule.course == null
    }

    def "Setter로 필드를 수정할 수 있다"() {
        when: "필드를 수정하면"
        course.setSectionNumber("002")
        course.setMaxStudents(50)
        course.setCurrentStudents(10)
        course.setDescription("수정된 설명")

        then: "값이 변경된다"
        course.sectionNumber == "002"
        course.maxStudents == 50
        course.currentStudents == 10
        course.description == "수정된 설명"
    }

    def "빈 스케줄 리스트에서 removeSchedule을 호출해도 오류가 발생하지 않는다"() {
        given: "스케줄이 없는 Course"
        def emptyScheduleCourse = Course.builder()
                .id(2L)
                .subject(subject)
                .professor(professor)
                .academicTerm(academicTerm)
                .sectionNumber("001")
                .maxStudents(30)
                .currentStudents(0)
                .build()

        def schedule = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()

        when: "존재하지 않는 스케줄을 제거하면"
        emptyScheduleCourse.removeSchedule(schedule)

        then: "오류가 발생하지 않는다"
        notThrown(Exception)
        emptyScheduleCourse.schedules.isEmpty()
    }
}
