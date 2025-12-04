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
    private final UserContactRepository userContactRepository;
    private final StudentDepartmentRepository studentDepartmentRepository;
    private final ProfessorDepartmentRepository professorDepartmentRepository;
    private final DepartmentRepository departmentRepository;
    private final EncryptionService encryptionService;
    private final EmailVerificationService emailVerificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long execute(SignupRequestDto dto) {
        try {
            validateSignupRequest(dto);

            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 학과입니다."));

            User user = createUser(dto);
            createUserProfile(user, dto);
            createUserContact(user, dto);

            if (dto.isStudent()) {
                createStudent(user, department, dto.getGrade());
            } else if (dto.isProfessor()) {
                createProfessor(user, department, dto.getProfessorNumber());
            }

            emailVerificationService.clearVerification(dto.getEmail());

            log.info("회원가입 완료: userId={}, email={}, userType={}",
                    user.getId(), dto.getEmail(), dto.getUserType());

            return user.getId();

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

    private User createUser(SignupRequestDto dto) {
        User user = User.create(
            encryptionService.encryptEmail(dto.getEmail()),
            encryptionService.encryptPassword(dto.getPassword())
        );
        return userRepository.save(user);
    }

    private void createUserProfile(User user, SignupRequestDto dto) {
        UserProfile profile = UserProfile.create(user, dto.getName());
        userProfileRepository.save(profile);
    }

    private void createUserContact(User user, SignupRequestDto dto) {
        UserContact contact = UserContact.create(
            user,
            UserContact.ContactType.MOBILE,
            encryptionService.encryptPhoneNumber(dto.getPhoneNumber()),
            true
        );
        userContactRepository.save(contact);
    }

    private void createStudent(User user, Department department, Integer grade) {
        if (grade == null || grade < 1 || grade > 4) {
            grade = 1;
        }

        String studentNumber = generateStudentNumber();
        Student student = Student.create(user, studentNumber, Year.now().getValue(), grade);
        studentRepository.save(student);

        StudentDepartment studentDept = StudentDepartment.create(
            student, department, true, LocalDate.now()
        );
        studentDepartmentRepository.save(studentDept);

        log.info("학생 생성: studentNumber={}, grade={}", studentNumber, grade);
    }

    private void createProfessor(User user, Department department, String professorNumber) {
        if (professorNumber == null || professorNumber.trim().isEmpty()) {
            professorNumber = generateProfessorNumber();
        }

        Professor professor = Professor.create(user, professorNumber, LocalDate.now());
        professorRepository.save(professor);

        ProfessorDepartment professorDept = ProfessorDepartment.create(
            professor, department, true, LocalDate.now()
        );
        professorDepartmentRepository.save(professorDept);

        log.info("교수 생성: professorNumber={}", professorNumber);
    }

    private String generateStudentNumber() {
        int year = Year.now().getValue();
        String prefix = String.valueOf(year);

        Optional<Student> lastStudent = studentRepository
                .findTopByStudentNumberStartingWithOrderByStudentNumberDesc(prefix);

        int sequence = 1;
        if (lastStudent.isPresent()) {
            String lastNumber = lastStudent.get().getStudentNumber();
            sequence = Integer.parseInt(lastNumber.substring(4)) + 1;
        }

        return String.format("%s%06d", prefix, sequence);
    }

    private String generateProfessorNumber() {
        int year = Year.now().getValue();
        String prefix = "P" + year;

        Optional<Professor> lastProfessor = professorRepository
                .findTopByProfessorNumberStartingWithOrderByProfessorNumberDesc(prefix);

        int sequence = 1;
        if (lastProfessor.isPresent()) {
            String lastNumber = lastProfessor.get().getProfessorNumber();
            sequence = Integer.parseInt(lastNumber.substring(5)) + 1;
        }

        return String.format("%s%03d", prefix, sequence);
    }
}