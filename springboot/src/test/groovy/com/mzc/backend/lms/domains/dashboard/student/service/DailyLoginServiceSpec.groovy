package com.mzc.backend.lms.domains.dashboard.student.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

/**
 * DailyLoginService 테스트
 * Redis를 사용한 일일 로그인 확인 기능 테스트
 */
class DailyLoginServiceSpec extends Specification {

    def redisTemplate = Mock(StringRedisTemplate)
    def valueOperations = Mock(ValueOperations)

    @Subject
    def dailyLoginService = new DailyLoginService(redisTemplate)

    def setup() {
        redisTemplate.opsForValue() >> valueOperations
    }

    // ==================== 첫 로그인 확인 및 마킹 테스트 ====================

    def "오늘 첫 로그인이면 true를 반환하고 마킹한다"() {
        given: "처음 로그인하는 사용자"
        def userId = 1L

        when: "첫 로그인을 확인하면"
        def result = dailyLoginService.checkAndMarkFirstLoginToday(userId)

        then: "true를 반환하고 Redis에 마킹된다"
        1 * valueOperations.setIfAbsent(_, "1", _ as Duration) >> true
        result == true
    }

    def "오늘 이미 로그인했으면 false를 반환한다"() {
        given: "이미 로그인한 사용자"
        def userId = 1L

        when: "로그인을 확인하면"
        def result = dailyLoginService.checkAndMarkFirstLoginToday(userId)

        then: "false를 반환한다"
        1 * valueOperations.setIfAbsent(_, "1", _ as Duration) >> false
        result == false
    }

    // ==================== 로그인 여부 확인 테스트 ====================

    def "오늘 로그인한 적이 있으면 true를 반환한다"() {
        given: "로그인 기록이 있는 사용자"
        def userId = 1L

        when: "로그인 여부를 확인하면"
        def result = dailyLoginService.hasLoggedInToday(userId)

        then: "true를 반환한다"
        1 * redisTemplate.hasKey(_) >> true
        result == true
    }

    def "오늘 로그인한 적이 없으면 false를 반환한다"() {
        given: "로그인 기록이 없는 사용자"
        def userId = 1L

        when: "로그인 여부를 확인하면"
        def result = dailyLoginService.hasLoggedInToday(userId)

        then: "false를 반환한다"
        1 * redisTemplate.hasKey(_) >> false
        result == false
    }
}
