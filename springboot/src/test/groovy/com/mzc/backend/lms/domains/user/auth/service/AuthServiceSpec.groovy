package com.mzc.backend.lms.domains.user.auth.service

import com.mzc.backend.lms.domains.user.auth.dto.*
import com.mzc.backend.lms.domains.user.auth.usecase.*
import spock.lang.Specification
import spock.lang.Subject

/**
 * AuthService Facade 테스트
 * 각 UseCase로의 위임이 올바르게 이루어지는지 검증
 */
class AuthServiceSpec extends Specification {

    def signupUseCase = Mock(SignupUseCase)
    def loginUseCase = Mock(LoginUseCase)
    def refreshTokenUseCase = Mock(RefreshTokenUseCase)
    def logoutUseCase = Mock(LogoutUseCase)
    def checkEmailUseCase = Mock(CheckEmailUseCase)

    @Subject
    def authService = new AuthService(
            signupUseCase,
            loginUseCase,
            refreshTokenUseCase,
            logoutUseCase,
            checkEmailUseCase
    )

    def "회원가입 요청 시 SignupUseCase로 위임한다"() {
        given: "회원가입 요청 정보"
        def dto = Mock(SignupRequestDto) {
            getEmail() >> "test@example.com"
        }
        def expectedUserId = "2025010001"

        when: "회원가입을 요청하면"
        def result = authService.signup(dto)

        then: "SignupUseCase가 호출되고 사용자 ID를 반환한다"
        1 * signupUseCase.execute(dto) >> expectedUserId
        result == expectedUserId
    }

    def "로그인 요청 시 LoginUseCase로 위임한다"() {
        given: "로그인 요청 정보"
        def dto = Mock(LoginRequestDto) {
            getUsername() >> "testuser"
        }
        def ipAddress = "192.168.1.1"
        def expectedResponse = Mock(LoginResponseDto)

        when: "로그인을 요청하면"
        def result = authService.login(dto, ipAddress)

        then: "LoginUseCase가 호출되고 로그인 응답을 반환한다"
        1 * loginUseCase.execute(dto, ipAddress) >> expectedResponse
        result == expectedResponse
    }

    def "토큰 갱신 요청 시 RefreshTokenUseCase로 위임한다"() {
        given: "토큰 갱신 요청 정보"
        def dto = Mock(RefreshTokenRequestDto)
        def expectedResponse = Mock(RefreshTokenResponseDto)

        when: "토큰 갱신을 요청하면"
        def result = authService.refreshToken(dto)

        then: "RefreshTokenUseCase가 호출되고 새 토큰을 반환한다"
        1 * refreshTokenUseCase.execute(dto) >> expectedResponse
        result == expectedResponse
    }

    def "로그아웃 요청 시 LogoutUseCase로 위임한다"() {
        given: "Refresh Token"
        def refreshToken = "valid-refresh-token"

        when: "로그아웃을 요청하면"
        authService.logout(refreshToken)

        then: "LogoutUseCase가 호출된다"
        1 * logoutUseCase.execute(refreshToken)
    }

    def "이메일 사용 가능 여부 확인 시 CheckEmailUseCase로 위임한다"() {
        given: "확인할 이메일"
        def email = "test@example.com"

        when: "이메일 사용 가능 여부를 확인하면"
        def result = authService.isEmailAvailable(email)

        then: "CheckEmailUseCase가 호출되고 결과를 반환한다"
        1 * checkEmailUseCase.execute(email) >> isAvailable
        result == isAvailable

        where:
        isAvailable << [true, false]
    }
}
