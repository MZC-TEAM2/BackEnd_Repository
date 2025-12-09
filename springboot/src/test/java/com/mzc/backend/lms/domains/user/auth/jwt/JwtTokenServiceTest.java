package com.mzc.backend.lms.domains.user.auth.jwt;

import com.mzc.backend.lms.domains.user.auth.jwt.service.JwtTokenService;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.user.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * JWT 토큰 서비스 테스트
 */
@DisplayName("JWT 토큰 서비스 테스트")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    private User user;
    private Student student;
    private Professor professor;

    @BeforeEach
    void setUp() {
        // JWT 서비스 인스턴스 생성
        jwtTokenService = new JwtTokenService();

        // JWT 서비스 필드 초기화
        ReflectionTestUtils.setField(jwtTokenService, "secretKey",
            "testSecretKeyForTestingPurposeOnlyMustBeLongEnough1234567890");
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenExpiration", 1800000L);
        ReflectionTestUtils.setField(jwtTokenService, "refreshTokenExpiration", 604800000L);

        // 테스트용 User 생성
        user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("test@example.com");

        // 테스트용 Student 생성
        student = mock(Student.class);
        when(student.getStudentNumber()).thenReturn(2024123456L);

        // 테스트용 Professor 생성
        professor = mock(Professor.class);
        when(professor.getProfessorNumber()).thenReturn(1001L);
    }

    @Test
    @DisplayName("학생용 Access Token 생성 테스트")
    void generateStudentAccessToken() {
        // when
        String token = jwtTokenService.generateStudentAccessToken(user, student);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 토큰 검증
        Claims claims = jwtTokenService.extractAllClaims(token);
        assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
        assertThat(claims.getSubject()).isEqualTo("test@example.com");
        assertThat(claims.get("userType", String.class)).isEqualTo("STUDENT");
        assertThat(claims.get("userNumber", String.class)).isEqualTo("2024123456");
    }

    @Test
    @DisplayName("교수용 Access Token 생성 테스트")
    void generateProfessorAccessToken() {
        // when
        String token = jwtTokenService.generateProfessorAccessToken(user, professor);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 토큰 검증
        Claims claims = jwtTokenService.extractAllClaims(token);
        assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
        assertThat(claims.getSubject()).isEqualTo("test@example.com");
        assertThat(claims.get("userType", String.class)).isEqualTo("PROFESSOR");
        assertThat(claims.get("userNumber", String.class)).isEqualTo("1001");
    }

    @Test
    @DisplayName("Refresh Token 생성 테스트")
    void generateRefreshToken() {
        // when
        String token = jwtTokenService.generateRefreshToken(user);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 토큰 검증
        Claims claims = jwtTokenService.extractAllClaims(token);
        assertThat(claims.get("userId", Long.class)).isEqualTo(1L);
        assertThat(claims.get("tokenType", String.class)).isEqualTo("REFRESH");
        assertThat(claims.getSubject()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 테스트")
    void extractUserId() {
        // given
        String token = jwtTokenService.generateAccessToken(user, "STUDENT", 2024123456L);

        // when
        Long userId = jwtTokenService.extractUserId(token);

        // then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 테스트")
    void extractEmail() {
        // given
        String token = jwtTokenService.generateAccessToken(user, "STUDENT", 2024123456L);

        // when
        String email = jwtTokenService.extractEmail(token);

        // then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰에서 사용자 타입 추출 테스트")
    void extractUserType() {
        // given
        String studentToken = jwtTokenService.generateStudentAccessToken(user, student);
        String professorToken = jwtTokenService.generateProfessorAccessToken(user, professor);

        // when
        String studentType = jwtTokenService.extractUserType(studentToken);
        String professorType = jwtTokenService.extractUserType(professorToken);

        // then
        assertThat(studentType).isEqualTo("STUDENT");
        assertThat(professorType).isEqualTo("PROFESSOR");
    }

    @Test
    @DisplayName("토큰에서 학번/교번 추출 테스트")
    void extractUserNumber() {
        // given
        String studentToken = jwtTokenService.generateStudentAccessToken(user, student);
        String professorToken = jwtTokenService.generateProfessorAccessToken(user, professor);

        // when
        String studentNumber = jwtTokenService.extractUserNumber(studentToken);
        String professorNumber = jwtTokenService.extractUserNumber(professorToken);

        // then
        assertThat(studentNumber).isEqualTo("2024123456");
        assertThat(professorNumber).isEqualTo("1001");
    }

    @Test
    @DisplayName("토큰 유효성 검증 테스트 - 유효한 토큰")
    void validateToken_Valid() {
        // given
        String token = jwtTokenService.generateAccessToken(user, "STUDENT", 2024123456L);

        // when
        Boolean isValid = jwtTokenService.validateToken(token, user);
        Boolean isValidWithoutUser = jwtTokenService.validateToken(token);

        // then
        assertThat(isValid).isTrue();
        assertThat(isValidWithoutUser).isTrue();
    }

    @Test
    @DisplayName("토큰 만료 여부 확인 테스트")
    void isTokenExpired() {
        // given
        String token = jwtTokenService.generateAccessToken(user, "STUDENT", 2024123456L);

        // when
        Boolean isExpired = jwtTokenService.isTokenExpired(token);

        // then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("학생 여부 확인 테스트")
    void isStudent() {
        // given
        String studentToken = jwtTokenService.generateStudentAccessToken(user, student);
        String professorToken = jwtTokenService.generateProfessorAccessToken(user, professor);

        // when
        boolean isStudentFromStudentToken = jwtTokenService.isStudent(studentToken);
        boolean isStudentFromProfessorToken = jwtTokenService.isStudent(professorToken);

        // then
        assertThat(isStudentFromStudentToken).isTrue();
        assertThat(isStudentFromProfessorToken).isFalse();
    }

    @Test
    @DisplayName("교수 여부 확인 테스트")
    void isProfessor() {
        // given
        String studentToken = jwtTokenService.generateStudentAccessToken(user, student);
        String professorToken = jwtTokenService.generateProfessorAccessToken(user, professor);

        // when
        boolean isProfessorFromStudentToken = jwtTokenService.isProfessor(studentToken);
        boolean isProfessorFromProfessorToken = jwtTokenService.isProfessor(professorToken);

        // then
        assertThat(isProfessorFromStudentToken).isFalse();
        assertThat(isProfessorFromProfessorToken).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 테스트")
    void validateToken_Invalid() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        Boolean isValid = jwtTokenService.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }
}
