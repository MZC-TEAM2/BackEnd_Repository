package com.mzc.backend.lms.domains.user.student.repository;

import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudentRepository 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StudentRepository 테스트")
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        // Given: 테스트용 사용자 및 학생 생성
        testUser = User.create("student@example.com", "password123");
        testUser = userRepository.save(testUser);

        testStudent = Student.create(testUser, "2024123456", 2024);
    }

    @Test
    @DisplayName("학생 정보 저장 및 조회")
    void saveAndFindStudent() {
        // Given: 학생 정보 저장
        Student savedStudent = studentRepository.save(testStudent);

        // When: ID로 조회
        Optional<Student> foundStudent = studentRepository.findById(savedStudent.getUserId());

        // Then: 저장된 학생 정보 검증
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getStudentNumber()).isEqualTo("2024123456");
        assertThat(foundStudent.get().getAdmissionYear()).isEqualTo(2024);
    }

    @Test
    @DisplayName("학번으로 학생 조회")
    void findByStudentNumber() {
        // Given: 학생 정보 저장
        studentRepository.save(testStudent);

        // When: 학번으로 조회
        Optional<Student> foundStudent = studentRepository.findByStudentNumber("2024123456");

        // Then: 조회된 학생 정보 검증
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getStudentNumber()).isEqualTo("2024123456");
    }

    @Test
    @DisplayName("학번 중복 확인")
    void existsByStudentNumber() {
        // Given: 학생 정보 저장
        studentRepository.save(testStudent);

        // When & Then: 학번 존재 여부 확인
        assertThat(studentRepository.existsByStudentNumber("2024123456")).isTrue();
        assertThat(studentRepository.existsByStudentNumber("2024999999")).isFalse();
    }

    @Test
    @DisplayName("입학년도별 학생 목록 조회")
    void findByAdmissionYear() {
        // Given: 여러 학생 저장
        studentRepository.save(testStudent);

        User anotherUser = userRepository.save(User.create("another@example.com", "password"));
        Student anotherStudent = Student.create(anotherUser, "2024123457", 2024);
        studentRepository.save(anotherStudent);

        User oldUser = userRepository.save(User.create("old@example.com", "password"));
        Student oldStudent = Student.create(oldUser, "2023123456", 2023);
        studentRepository.save(oldStudent);

        // When: 2024년도 입학생 조회
        List<Student> students2024 = studentRepository.findByAdmissionYear(2024);

        // Then: 2024년도 입학생만 조회됨
        assertThat(students2024).hasSize(2);
        assertThat(students2024).extracting(Student::getAdmissionYear)
                .containsOnly(2024);
    }

    @Test
    @DisplayName("사용자 ID로 학생 정보 조회")
    void findByUserId() {
        // Given: 학생 정보 저장
        studentRepository.save(testStudent);

        // When: 사용자 ID로 조회
        Optional<Student> foundStudent = studentRepository.findByUserId(testUser.getId());

        // Then: 조회된 학생 정보 검증
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getStudentNumber()).isEqualTo("2024123456");
    }

    @Test
    @DisplayName("학번 패턴으로 학생 검색")
    void findByStudentNumberPattern() {
        // Given: 여러 학생 저장
        studentRepository.save(testStudent);

        User anotherUser = userRepository.save(User.create("another@example.com", "password"));
        Student anotherStudent = Student.create(anotherUser, "2024123457", 2024);
        studentRepository.save(anotherStudent);

        // When: 2024로 시작하는 학번 검색
        List<Student> students = studentRepository.findByStudentNumberPattern("2024%");

        // Then: 2024로 시작하는 학번을 가진 학생들만 조회
        assertThat(students).hasSize(2);
        assertThat(students).extracting(Student::getStudentNumber)
                .allMatch(num -> num.startsWith("2024"));
    }
}