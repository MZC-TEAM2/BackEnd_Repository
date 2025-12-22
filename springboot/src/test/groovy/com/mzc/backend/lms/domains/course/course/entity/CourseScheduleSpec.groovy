package com.mzc.backend.lms.domains.course.course.entity

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * CourseSchedule 엔티티 Spock 테스트
 */
class CourseScheduleSpec extends Specification {

    def course = Mock(Course)

    @Subject
    CourseSchedule schedule

    def setup() {
        schedule = CourseSchedule.builder()
                .scheduleId(1L)
                .course(course)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()
    }

    def "CourseSchedule 객체가 올바르게 생성된다"() {
        expect: "모든 필드가 올바르게 설정된다"
        schedule.scheduleId == 1L
        schedule.course == course
        schedule.dayOfWeek == DayOfWeek.MONDAY
        schedule.startTime == LocalTime.of(9, 0)
        schedule.endTime == LocalTime.of(10, 30)
        schedule.scheduleRoom == "공학관 101호"
    }

    @Unroll
    def "요일 #dayOfWeek로 스케줄을 생성할 수 있다"() {
        given: "특정 요일의 스케줄"
        def weekdaySchedule = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .scheduleRoom("공학관 101호")
                .build()

        expect: "요일이 올바르게 설정된다"
        weekdaySchedule.dayOfWeek == dayOfWeek
        weekdaySchedule.dayOfWeek.value == dayValue

        where:
        dayOfWeek          | dayValue
        DayOfWeek.MONDAY   | 1
        DayOfWeek.TUESDAY  | 2
        DayOfWeek.WEDNESDAY| 3
        DayOfWeek.THURSDAY | 4
        DayOfWeek.FRIDAY   | 5
        DayOfWeek.SATURDAY | 6
        DayOfWeek.SUNDAY   | 7
    }

    def "Setter로 필드를 수정할 수 있다"() {
        when: "필드를 수정하면"
        schedule.setDayOfWeek(DayOfWeek.FRIDAY)
        schedule.setStartTime(LocalTime.of(14, 0))
        schedule.setEndTime(LocalTime.of(15, 30))
        schedule.setScheduleRoom("공학관 202호")

        then: "값이 변경된다"
        schedule.dayOfWeek == DayOfWeek.FRIDAY
        schedule.startTime == LocalTime.of(14, 0)
        schedule.endTime == LocalTime.of(15, 30)
        schedule.scheduleRoom == "공학관 202호"
    }

    def "Course 연관관계를 설정할 수 있다"() {
        given: "새로운 Course"
        def newCourse = Mock(Course)

        when: "Course를 변경하면"
        schedule.setCourse(newCourse)

        then: "연관관계가 변경된다"
        schedule.course == newCourse
    }

    def "Course 연관관계를 null로 해제할 수 있다"() {
        when: "Course를 null로 설정하면"
        schedule.setCourse(null)

        then: "연관관계가 해제된다"
        schedule.course == null
    }

    @Unroll
    def "시작 시간 #startTime, 종료 시간 #endTime으로 스케줄을 생성할 수 있다"() {
        given: "특정 시간의 스케줄"
        def timeSchedule = CourseSchedule.builder()
                .scheduleId(1L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(startTime)
                .endTime(endTime)
                .scheduleRoom("공학관 101호")
                .build()

        expect: "시간이 올바르게 설정된다"
        timeSchedule.startTime == startTime
        timeSchedule.endTime == endTime

        where:
        startTime            | endTime
        LocalTime.of(9, 0)   | LocalTime.of(10, 30)
        LocalTime.of(10, 30) | LocalTime.of(12, 0)
        LocalTime.of(13, 0)  | LocalTime.of(14, 30)
        LocalTime.of(14, 30) | LocalTime.of(16, 0)
        LocalTime.of(18, 0)  | LocalTime.of(20, 45)
    }

    def "다양한 강의실 이름으로 스케줄을 생성할 수 있다"() {
        given: "다양한 강의실 이름"
        def rooms = ["공학관 101호", "인문관 A동 301호", "과학관 B-201", "온라인"]

        expect: "각 강의실 이름으로 스케줄을 생성할 수 있다"
        rooms.each { room ->
            def roomSchedule = CourseSchedule.builder()
                    .scheduleId(1L)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(10, 30))
                    .scheduleRoom(room)
                    .build()
            assert roomSchedule.scheduleRoom == room
        }
    }
}
