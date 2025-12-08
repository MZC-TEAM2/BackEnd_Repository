package com.mzc.backend.lms.domains.user.auth.usecase.impl;

import com.mzc.backend.lms.domains.user.auth.dto.RefreshTokenRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.RefreshTokenResponseDto;
import com.mzc.backend.lms.domains.user.auth.jwt.service.JwtTokenService;
import com.mzc.backend.lms.domains.user.auth.token.entity.RefreshToken;
import com.mzc.backend.lms.domains.user.auth.token.repository.RefreshTokenRepository;
import com.mzc.backend.lms.domains.user.auth.usecase.RefreshTokenUseCase;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 토큰 갱신 유스케이스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional
    public RefreshTokenResponseDto execute(RefreshTokenRequestDto dto) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("만료되었거나 폐기된 토큰입니다.");
        }

        if (!jwtTokenService.validateToken(dto.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
        }

        User user = refreshToken.getUser();
        UserTypeInfo userTypeInfo = getUserTypeInfo(user);

        String newAccessToken = jwtTokenService.generateAccessToken(
            user, userTypeInfo.userType, userTypeInfo.userNumber
        );
        String newRefreshToken = jwtTokenService.generateRefreshToken(user);

        // Token Rotation: 기존 토큰 폐기 및 새 토큰 저장
        performTokenRotation(refreshToken, user, newRefreshToken);

        log.info("토큰 갱신: userId={}", user.getId());

        return RefreshTokenResponseDto.of(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String refreshToken) {
        Optional<RefreshToken> token = refreshTokenRepository.findByToken(refreshToken);
        if (token.isEmpty()) {
            return false;
        }

        return token.get().isValid() && jwtTokenService.validateToken(refreshToken);
    }

    @Override
    @Transactional
    public int revokeAllUserTokens(String userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
        int count = 0;

        for (RefreshToken token : tokens) {
            if (token.isValid()) {
                token.revoke();
                refreshTokenRepository.save(token);
                count++;
            }
        }

        log.info("사용자 모든 토큰 폐기: userId={}, count={}", userId, count);
        return count;
    }

    private UserTypeInfo getUserTypeInfo(User user) {
        String userType = null;
        Long userNumber = user.getId();  // User의 ID가 곧 학번/교번

        // Student 테이블에서 찾기
        var student = studentRepository.findById(userNumber);
        if (student.isPresent()) {
            userType = "STUDENT";
        } else {
            // Professor 테이블에서 찾기
            var professor = professorRepository.findById(userNumber);
            if (professor.isPresent()) {
                userType = "PROFESSOR";
            }
        }

        return new UserTypeInfo(userType, userNumber);
    }

    private void performTokenRotation(RefreshToken oldToken, User user, String newTokenValue) {
        // 기존 토큰 폐기
        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        // 새 토큰 저장
        RefreshToken newToken = RefreshToken.create(
            user,
            newTokenValue,
            oldToken.getDeviceInfo(),
            oldToken.getIpAddress(),
            LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(newToken);
    }

    private static class UserTypeInfo {
        final String userType;
        final Long userNumber;

        UserTypeInfo(String userType, Long userNumber) {
            this.userType = userType;
            this.userNumber = userNumber;
        }
    }
}