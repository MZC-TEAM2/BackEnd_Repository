package com.mzc.backend.lms.domains.user.auth.service;

import com.mzc.backend.lms.domains.user.auth.dto.*;
import com.mzc.backend.lms.domains.user.auth.email.service.EmailVerificationService;
import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.auth.jwt.service.JwtTokenService;
import com.mzc.backend.lms.domains.user.auth.token.entity.RefreshToken;
import com.mzc.backend.lms.domains.user.auth.token.repository.RefreshTokenRepository;
import com.mzc.backend.lms.domains.user.organization.entity.College;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.organization.repository.CollegeRepository;
import com.mzc.backend.lms.domains.user.organization.repository.DepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.profile.entity.UserContact;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.repository.UserContactRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.entity.StudentDepartment;
import com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;

/**
 * 인증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserContactRepository userContactRepository;
    private final StudentDepartmentRepository studentDepartmentRepository;
    private final ProfessorDepartmentRepository professorDepartmentRepository;
    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final EncryptionService encryptionService;
    private final EmailVerificationService emailVerificationService;
    private final JwtTokenService jwtTokenService;

    /**
     * 회원가입
     */
    public Long signup(SignupRequestDto dto) {
        // 1. 비밀번호 일치 확인
        if (!dto.isPasswordMatched()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 이메일 인증 확인
        if (!emailVerificationService.isEmailVerified(dto.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        // 3. 이메일 중복 확인
        if (userRepository.existsByEmail(encryptionService.encryptEmail(dto.getEmail()))) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 4. 대학/학과 조회
        College college = collegeRepository.findById(dto.getCollegeId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 대학입니다."));
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 학과입니다."));

        // 5. User 엔티티 생성
        User user = User.create(
            encryptionService.encryptEmail(dto.getEmail()),
            encryptionService.encryptPassword(dto.getPassword())
        );
        user = userRepository.save(user);

        // 6. UserProfile 생성
        UserProfile profile = UserProfile.create(
            user,
            dto.getName()
        );
        userProfileRepository.save(profile);

        // 7. UserContact 생성 (전화번호)
        UserContact contact = UserContact.create(
            user,
            UserContact.ContactType.MOBILE,
            encryptionService.encryptPhoneNumber(dto.getPhoneNumber()),
            true  // 주 연락처
        );
        userContactRepository.save(contact);

        // 8. 사용자 타입별 처리
        if (dto.isStudent()) {
            createStudent(user, department, dto.getGrade());
        } else if (dto.isProfessor()) {
            createProfessor(user, department, dto.getProfessorNumber());
        }

        // 9. 이메일 인증 정보 삭제
        emailVerificationService.clearVerification(dto.getEmail());

        log.info("회원가입 완료: userId={}, email={}, userType={}",
                user.getId(), dto.getEmail(), dto.getUserType());

        return user.getId();
    }

    /**
     * 학생 생성
     */
    private void createStudent(User user, Department department, Integer grade) {
        // 학번 생성 (년도 + 시퀀스)
        String studentNumber = generateStudentNumber();

        // Student 엔티티 생성
        Student student = Student.create(user, studentNumber, Year.now().getValue());
        studentRepository.save(student);

        // StudentDepartment 생성
        StudentDepartment studentDept = StudentDepartment.create(
            student,
            department,
            true,  // isPrimary (주전공)
            LocalDate.now()
        );
        studentDepartmentRepository.save(studentDept);

        log.info("학생 생성: studentNumber={}, grade={}", studentNumber, grade);
    }

    /**
     * 교수 생성
     */
    private void createProfessor(User user, Department department, String professorNumber) {
        // 교번이 없으면 자동 생성
        if (professorNumber == null || professorNumber.trim().isEmpty()) {
            professorNumber = generateProfessorNumber();
        }

        // Professor 엔티티 생성
        Professor professor = Professor.create(user, professorNumber, LocalDate.now());
        professorRepository.save(professor);

        // ProfessorDepartment 생성
        ProfessorDepartment professorDept = ProfessorDepartment.create(
            professor,
            department,
            true,  // isPrimary
            LocalDate.now()
        );
        professorDepartmentRepository.save(professorDept);

        log.info("교수 생성: professorNumber={}", professorNumber);
    }

    /**
     * 학번 자동 생성 (년도 + 6자리 시퀀스)
     */
    private String generateStudentNumber() {
        int year = Year.now().getValue();
        String prefix = String.valueOf(year);

        // 해당 년도의 마지막 학번 조회
        Optional<Student> lastStudent = studentRepository.findTopByStudentNumberStartingWithOrderByStudentNumberDesc(prefix);

        int sequence = 1;
        if (lastStudent.isPresent()) {
            String lastNumber = lastStudent.get().getStudentNumber();
            sequence = Integer.parseInt(lastNumber.substring(4)) + 1;
        }

        return String.format("%s%06d", prefix, sequence);
    }

    /**
     * 교번 자동 생성 (P + 년도 + 3자리 시퀀스)
     */
    private String generateProfessorNumber() {
        int year = Year.now().getValue();
        String prefix = "P" + year;

        // 해당 년도의 마지막 교번 조회
        Optional<Professor> lastProfessor = professorRepository.findTopByProfessorNumberStartingWithOrderByProfessorNumberDesc(prefix);

        int sequence = 1;
        if (lastProfessor.isPresent()) {
            String lastNumber = lastProfessor.get().getProfessorNumber();
            sequence = Integer.parseInt(lastNumber.substring(5)) + 1;
        }

        return String.format("%s%03d", prefix, sequence);
    }

    /**
     * 로그인
     */
    public LoginResponseDto login(LoginRequestDto dto, String ipAddress) {
        // 1. 사용자 조회 (이메일 또는 학번/교번)
        User user = findUserByUsername(dto.getUsername());

        if (user == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 2. 비밀번호 확인
        if (!encryptionService.matchesPassword(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. 사용자 타입 및 정보 조회
        String userType = null;
        String userNumber = null;
        String name = null;

        // 학생인지 확인
        Optional<Student> student = studentRepository.findByUserId(user.getId());
        if (student.isPresent()) {
            userType = "STUDENT";
            userNumber = student.get().getStudentNumber();
        } else {
            // 교수인지 확인
            Optional<Professor> professor = professorRepository.findByUserId(user.getId());
            if (professor.isPresent()) {
                userType = "PROFESSOR";
                userNumber = professor.get().getProfessorNumber();
            }
        }

        // 프로필에서 이름 조회
        Optional<UserProfile> profile = userProfileRepository.findByUserId(user.getId());
        if (profile.isPresent()) {
            name = profile.get().getName();
        }

        // 4. JWT 토큰 생성
        String accessToken = jwtTokenService.generateAccessToken(user, userType, userNumber);
        String refreshToken = jwtTokenService.generateRefreshToken(user);

        // 5. Refresh Token DB 저장
        RefreshToken refreshTokenEntity = RefreshToken.create(
            user,
            refreshToken,
            dto.getDeviceInfo(),
            ipAddress != null ? ipAddress : dto.getIpAddress(),
            LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(refreshTokenEntity);

        // 6. 응답 생성
        String decryptedEmail = encryptionService.decryptEmail(user.getEmail());

        log.info("로그인 성공: userId={}, userType={}", user.getId(), userType);

        return LoginResponseDto.of(
            accessToken,
            refreshToken,
            userType,
            userNumber,
            name,
            decryptedEmail,
            user.getId()
        );
    }

    /**
     * 사용자명으로 사용자 조회 (이메일 또는 학번/교번)
     */
    private User findUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        // 이메일 형식인 경우
        if (username.contains("@")) {
            String encryptedEmail = encryptionService.encryptEmail(username);
            return userRepository.findByEmail(encryptedEmail).orElse(null);
        }

        // 학번으로 조회
        Optional<Student> student = studentRepository.findByStudentNumber(username);
        if (student.isPresent()) {
            return student.get().getUser();
        }

        // 교번으로 조회
        Optional<Professor> professor = professorRepository.findByProfessorNumber(username);
        if (professor.isPresent()) {
            return professor.get().getUser();
        }

        return null;
    }

    /**
     * 토큰 갱신 (Token Rotation)
     */
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto dto) {
        // 1. Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        // 2. 토큰 유효성 검증
        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("만료되었거나 폐기된 토큰입니다.");
        }

        // 3. JWT 토큰 유효성 검증
        if (!jwtTokenService.validateToken(dto.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
        }

        User user = refreshToken.getUser();

        // 4. 사용자 타입 및 정보 조회
        String userType = null;
        String userNumber = null;

        Optional<Student> student = studentRepository.findByUserId(user.getId());
        if (student.isPresent()) {
            userType = "STUDENT";
            userNumber = student.get().getStudentNumber();
        } else {
            Optional<Professor> professor = professorRepository.findByUserId(user.getId());
            if (professor.isPresent()) {
                userType = "PROFESSOR";
                userNumber = professor.get().getProfessorNumber();
            }
        }

        // 5. 새로운 토큰 생성
        String newAccessToken = jwtTokenService.generateAccessToken(user, userType, userNumber);
        String newRefreshToken = jwtTokenService.generateRefreshToken(user);

        // 6. 기존 Refresh Token 폐기
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // 7. 새로운 Refresh Token 저장
        RefreshToken newRefreshTokenEntity = RefreshToken.create(
            user,
            newRefreshToken,
            refreshToken.getDeviceInfo(),
            refreshToken.getIpAddress(),
            LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(newRefreshTokenEntity);

        log.info("토큰 갱신: userId={}", user.getId());

        return RefreshTokenResponseDto.of(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃
     */
    public void logout(String refreshToken) {
        // Refresh Token 조회 및 폐기
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                    log.info("로그아웃: userId={}", token.getUser().getId());
                });
    }

    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        String encryptedEmail = encryptionService.encryptEmail(email);
        return !userRepository.existsByEmail(encryptedEmail);
    }
}