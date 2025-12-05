package com.mzc.backend.lms.views.impl;

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService;
import com.mzc.backend.lms.domains.user.organization.repository.DepartmentRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserContactRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileImageRepository;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.domains.user.user.exceptions.UserException;
import com.mzc.backend.lms.views.StudentViewService;
import com.mzc.backend.lms.views.user.StudentView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 학생 정보 통합 조회 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentViewServiceImpl implements StudentViewService {

    private final StudentRepository studentRepository;
    private final StudentDepartmentRepository studentDepartmentRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserContactRepository userContactRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final DepartmentRepository departmentRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional(readOnly = true)
    public StudentView getStudent(String studentNumber) {
        return findStudent(studentNumber)
            .orElseThrow(() -> UserException.studentNotFound(studentNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentView> findStudent(String studentNumber) {
        log.debug("Finding student by studentNumber: {}", studentNumber);

        // Native Query로 전체 정보 조회
        Object[] result = studentRepository.findStudentFullInfoByStudentNumber(studentNumber);

        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(mapToStudentView(result));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentView getStudentByUserId(Long userId) {
        return findStudentByUserId(userId)
            .orElseThrow(() -> UserException.studentNotFoundByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudentView> findStudentByUserId(Long userId) {
        log.debug("Finding student by userId: {}", userId);

        // 먼저 Student 엔티티 조회
        Optional<Student> studentOpt = studentRepository.findByUserIdWithUser(userId);

        if (studentOpt.isEmpty()) {
            return Optional.empty();
        }

        Student student = studentOpt.get();

        // Native Query로 전체 정보 조회
        Object[] result = studentRepository.findStudentFullInfoByStudentNumber(student.getStudentNumber());

        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(mapToStudentView(result));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, StudentView> getStudentsByNumbers(List<String> studentNumbers) {
        if (studentNumbers == null || studentNumbers.isEmpty()) {
            return Collections.emptyMap();
        }

        log.debug("Finding {} students by numbers", studentNumbers.size());

        // Native Query로 여러 학생 정보 조회
        List<Object[]> results = studentRepository.findStudentsFullInfoByStudentNumbers(studentNumbers);

        return results.stream()
            .map(this::mapToStudentView)
            .collect(Collectors.toMap(
                StudentView::getStudentNumber,
                student -> student
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, StudentView> getStudentsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        log.debug("Finding {} students by userIds", userIds.size());

        // 먼저 Student 엔티티들 조회
        List<Student> students = studentRepository.findByUserIdsWithUser(userIds);

        if (students.isEmpty()) {
            return Collections.emptyMap();
        }

        // 학번 목록 추출
        List<String> studentNumbers = students.stream()
            .map(Student::getStudentNumber)
            .collect(Collectors.toList());

        // Native Query로 전체 정보 조회
        List<Object[]> results = studentRepository.findStudentsFullInfoByStudentNumbers(studentNumbers);

        return results.stream()
            .map(this::mapToStudentView)
            .collect(Collectors.toMap(
                StudentView::getUserId,
                student -> student
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudent(Long userId) {
        return studentRepository.findByUserId(userId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByStudentNumber(String studentNumber) {
        return studentRepository.existsByStudentNumber(studentNumber);
    }

    /**
     * Native Query 결과를 StudentView로 매핑
     *
     * 컬럼 순서:
     * 0: user_id, 1: student_number, 2: grade, 3: admission_year,
     * 4: email, 5: profile_name, 6: phone_number,
     * 7: department_id, 8: department_name,
     * 9: college_id, 10: college_name,
     * 11: enrollment_date, 12: dept_active,
     * 13: profile_image_url
     */
    private StudentView mapToStudentView(Object[] result) {
        return StudentView.builder()
            .userId(((Number) result[0]).longValue())
            .studentNumber((String) result[1])
            .grade(result[2] != null ? ((Number) result[2]).intValue() : null)
            .admissionYear(result[3] != null ? ((Number) result[3]).intValue() : null)
            .email(result[4] != null ? decryptEmail((String) result[4]) : null)
            .name(result[5] != null ? decryptName((String) result[5]) : null)
            .phoneNumber(result[6] != null ? decryptPhoneNumber((String) result[6]) : null)
            .departmentId(result[7] != null ? ((Number) result[7]).longValue() : null)
            .departmentName((String) result[8])
            .collegeId(result[9] != null ? ((Number) result[9]).longValue() : null)
            .collegeName((String) result[10])
            .enrollmentDate(result[11] != null ? ((java.sql.Date) result[11]).toLocalDate() : null)
            .isDepartmentActive(result[12] != null ? (Boolean) result[12] : false)
            .profileImageUrl((String) result[13])
            .createdAt(null) // 필요시 추가
            .build();
    }

    /**
     * 이메일 복호화
     */
    private String decryptEmail(String encryptedEmail) {
        try {
            return encryptionService.decryptEmail(encryptedEmail);
        } catch (Exception e) {
            log.error("Failed to decrypt email", e);
            return encryptedEmail;
        }
    }

    /**
     * 이름 복호화
     */
    private String decryptName(String encryptedName) {
        try {
            return encryptionService.decryptName(encryptedName);
        } catch (Exception e) {
            log.error("Failed to decrypt name", e);
            return encryptedName;
        }
    }

    /**
     * 전화번호 복호화
     */
    private String decryptPhoneNumber(String encryptedPhoneNumber) {
        try {
            return encryptionService.decryptPhoneNumber(encryptedPhoneNumber);
        } catch (Exception e) {
            log.error("Failed to decrypt phone number", e);
            return encryptedPhoneNumber;
        }
    }
}