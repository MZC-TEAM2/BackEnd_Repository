package com.mzc.backend.lms.domains.user.auth.usecase.impl;

import com.mzc.backend.lms.domains.dashboard.student.event.LoginSuccessEvent;
import com.mzc.backend.lms.domains.user.auth.dto.LoginRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.LoginResponseDto;
import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.auth.jwt.service.JwtTokenService;
import com.mzc.backend.lms.domains.user.auth.token.entity.RefreshToken;
import com.mzc.backend.lms.domains.user.auth.token.repository.RefreshTokenRepository;
import com.mzc.backend.lms.domains.user.auth.usecase.LoginUseCase;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 로그인 유스케이스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EncryptionService encryptionService;
    private final JwtTokenService jwtTokenService;
    private final com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository studentDepartmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public LoginResponseDto execute(LoginRequestDto dto, String ipAddress) {
        User user = findUserByUsername(dto.getUsername());

        if (user == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!encryptionService.matchesPassword(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        UserInfo userInfo = getUserInfo(user, dto.getUsername());

        String accessToken = jwtTokenService.generateAccessToken(
            user, userInfo.userType, userInfo.userNumber
        );
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken, dto, ipAddress);

        String decryptedEmail = encryptionService.decryptEmail(user.getEmail());

        // 프로필 썸네일 URL 조회
        String thumbnailUrl = userProfileImageRepository.findByUserId(user.getId())
                .map(UserProfileImage::getThumbnailUrl)
                .orElse(null);

        // 학과 정보 조회 (학생인 경우)
        Long departmentId = null;
        String departmentName = null;
        if ("STUDENT".equals(userInfo.userType)) {
            try {
                var studentDepartment = studentDepartmentRepository.findByStudentId(user.getId());
                if (studentDepartment.isPresent()) {
                    departmentId = studentDepartment.get().getDepartment().getId();
                    departmentName = studentDepartment.get().getDepartment().getDepartmentName();
                }
            } catch (Exception e) {
                log.warn("학과 정보 조회 실패: userId={}", user.getId(), e);
            }
        }

        log.info("로그인 성공: userId={}, userType={}, departmentId={}, departmentName={}",
                user.getId(), userInfo.userType, departmentId, departmentName);

        // 로그인 성공 이벤트 발행
        eventPublisher.publishEvent(new LoginSuccessEvent(user.getId(), userInfo.userType));

        return LoginResponseDto.of(
            accessToken,
            refreshToken,
            userInfo.userType,
            userInfo.userNumber != null ? userInfo.userNumber.toString() : null,
            userInfo.name,
            decryptedEmail,
            user.getId().toString(),
            thumbnailUrl,
            departmentId,
            departmentName
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAuthenticatable(String username) {
        return findUserByUsername(username) != null;
    }

    private User findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        // 이메일 형식인 경우
        if (username.contains("@")) {
            String encryptedEmail = encryptionService.encryptEmail(username);
            return userRepository.findByEmail(encryptedEmail).orElse(null);
        }

        // 학번/교번인 경우 (숫자로만 구성)
        try {
            Long userNumber = Long.parseLong(username);

            // 학번으로 조회
            Optional<Student> student = studentRepository.findById(userNumber);
            if (student.isPresent()) {
                return student.get().getUser();
            }

            // 교번으로 조회
            Optional<Professor> professor = professorRepository.findById(userNumber);
            if (professor.isPresent()) {
                return professor.get().getUser();
            }
        } catch (NumberFormatException e) {
            // 숫자가 아닌 경우 null 반환
            log.debug("Invalid user number format: {}", username);
        }

        return null;
    }

    private UserInfo getUserInfo(User user, String username) {
        String userType = "USER";
        Long userNumber = null;
        String name = null;

        // User.id가 곧 학번/교번이므로 이를 기준으로 조회
        Long userId = user.getId();

        // 학생으로 조회 시도
        Optional<Student> student = studentRepository.findById(userId);
        if (student.isPresent()) {
            userType = "STUDENT";
            userNumber = userId;
        } else {
            // 교수로 조회 시도
            Optional<Professor> professor = professorRepository.findById(userId);
            if (professor.isPresent()) {
                userType = "PROFESSOR";
                userNumber = userId;
            }
        }

        // 프로필에서 이름 가져오기
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isPresent()) {
            // 이름 복호화
            name = encryptionService.decryptName(profile.get().getName());
        }

        return new UserInfo(userType, userNumber, name);
    }

    private void saveRefreshToken(User user, String refreshToken,
                                  LoginRequestDto dto, String ipAddress) {
        RefreshToken refreshTokenEntity = RefreshToken.create(
            user,
            refreshToken,
            dto.getDeviceInfo(),
            ipAddress != null ? ipAddress : dto.getIpAddress(),
            LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private static class UserInfo {
        final String userType;
        final Long userNumber;
        final String name;

        UserInfo(String userType, Long userNumber, String name) {
            this.userType = userType;
            this.userNumber = userNumber;
            this.name = name;
        }
    }
}
