package com.mzc.backend.lms.domains.user.auth.usecase.impl;

import com.mzc.backend.lms.domains.user.auth.dto.SignupRequestDto;
import com.mzc.backend.lms.domains.user.auth.email.service.EmailVerificationService;
import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.auth.usecase.SignupUseCase;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.organization.repository.DepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.entity.UserPrimaryContact;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserPrimaryContactRepository;
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
import java.time.Year;
import java.util.Optional;

/**
 * 회원가입 유스케이스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignupUseCaseImpl implements SignupUseCase {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPrimaryContactRepository userPrimaryContactRepository;
    private final StudentDepartmentRepository studentDepartmentRepository;
    private final ProfessorDepartmentRepository professorDepartmentRepository;
    private final DepartmentRepository departmentRepository;
    private final EncryptionService encryptionService;
    private final EmailVerificationService emailVerificationService;
    private final com.mzc.backend.lms.domains.user.student.service.StudentNumberGenerator studentNumberGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String execute(SignupRequestDto dto) {
        try {
            log.info("회원가입 시작: email={}, userType={}, departmentId={}", 
                    dto.getEmail(), dto.getUserType(), dto.getDepartmentId());
            
            validateSignupRequest(dto);

            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("학과를 찾을 수 없음: departmentId={}", dto.getDepartmentId());
                        return new IllegalArgumentException("유효하지 않은 학과입니다.");
                    });

            // 학번/교번 먼저 생성
            Long userNumber;
            if (dto.isStudent()) {
                userNumber = studentNumberGenerator.generateStudentNumber(
                    department.getCollege().getId(),
                    department.getId()
                );
            } else if (dto.isProfessor()) {
                userNumber = studentNumberGenerator.generateProfessorNumber(
                    department.getCollege().getId(),
                    department.getId()
                );
            } else {
                throw new IllegalArgumentException("유효하지 않은 사용자 타입입니다.");
            }

            // User 생성 (ID로 학번/교번 사용)
            User user = createUser(userNumber, dto);
            createUserProfile(user, dto);
            createUserContact(user, dto);

            if (dto.isStudent()) {
                createStudent(user, department, dto.getGrade());
            } else if (dto.isProfessor()) {
                createProfessor(user, department);
            }

            emailVerificationService.clearVerification(dto.getEmail());

            log.info("회원가입 완료: userId={}, email={}, userType={}",
                    user.getId(), dto.getEmail(), dto.getUserType());

            return user.getId().toString();

        } catch (Exception e) {
            log.error("회원가입 실패: email={}, error={}", dto.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        String encryptedEmail = encryptionService.encryptEmail(email);
        return !userRepository.existsByEmail(encryptedEmail);
    }

    private void validateSignupRequest(SignupRequestDto dto) {
        if (!dto.isPasswordMatched()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!emailVerificationService.isEmailVerified(dto.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        if (!isEmailAvailable(dto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
    }

    private User createUser(Long id, SignupRequestDto dto) {
        User user = User.create(
            id,
            encryptionService.encryptEmail(dto.getEmail()),
            encryptionService.encryptPassword(dto.getPassword())
        );
        return userRepository.save(user);
    }

    private void createUserProfile(User user, SignupRequestDto dto) {
        // 이름 암호화하여 저장
        String encryptedName = encryptionService.encryptName(dto.getName());
        UserProfile profile = UserProfile.create(user, encryptedName);
        userProfileRepository.save(profile);
    }

    private void createUserContact(User user, SignupRequestDto dto) {
        UserPrimaryContact contact = UserPrimaryContact.create(
            user,
            encryptionService.encryptPhoneNumber(dto.getPhoneNumber())
        );
        userPrimaryContactRepository.save(contact);
    }

    private void createStudent(User user, Department department, Integer grade) {
        if (grade == null || grade < 1 || grade > 4) {
            grade = 1;
        }

        // User의 ID가 이미 학번임
        Long studentId = user.getId();

        // 학생 엔티티 생성 (새로운 PK 구조)
        Student student = Student.create(studentId, user, Year.now().getValue(), grade);
        studentRepository.save(student);

        // 학생-학과 관계 설정
        StudentDepartment studentDept = StudentDepartment.create(
            student, department, true, LocalDate.now()
        );
        studentDepartmentRepository.save(studentDept);

        log.info("학생 생성: studentId={}, grade={}", studentId, grade);
    }

    private void createProfessor(User user, Department department) {
        // User의 ID가 이미 교번임
        Long professorId = user.getId();

        // 교수 엔티티 생성 (새로운 PK 구조)
        Professor professor = Professor.create(professorId, user, LocalDate.now());
        professorRepository.save(professor);

        // 교수-학과 관계 설정
        ProfessorDepartment professorDept = ProfessorDepartment.create(
            professor, department, true, LocalDate.now()
        );
        professorDepartmentRepository.save(professorDept);

        log.info("교수 생성: professorId={}", professorId);
    }
}
