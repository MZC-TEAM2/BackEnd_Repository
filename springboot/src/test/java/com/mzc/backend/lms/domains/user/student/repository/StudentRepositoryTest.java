package com.mzc.backend.lms.domains.user.student.repository;

import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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

    @Autowired
    private EntityManager entityManager;

    private User createAndPersistUser(Long id, String email) {
        User user = User.create(id, email, "password123");
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private Student createAndPersistStudent(User user, Integer admissionYear, Integer grade) {
        Student student = Student.create(user.getId(), user, admissionYear, grade);
        entityManager.persist(student);
        entityManager.flush();
        return student;
    }

    @Test
    @DisplayName("학생 정보 저장 및 조회")
    void saveAndFindStudent() {
        // Given
        User user = createAndPersistUser(2024123456L, "student@example.com");
        Student student = createAndPersistStudent(user, 2024, 1);
        entityManager.clear();

        // When
        Optional<Student> foundStudent = studentRepository.findById(student.getStudentId());

        // Then
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getStudentNumber()).isEqualTo(2024123456L);
        assertThat(foundStudent.get().getAdmissionYear()).isEqualTo(2024);
    }

    @Test
    @DisplayName("학번으로 학생 조회")
    void findByStudentId() {
        // Given
        User user = createAndPersistUser(2024123456L, "student@example.com");
        createAndPersistStudent(user, 2024, 1);
        entityManager.clear();

        // When
        Optional<Student> foundStudent = studentRepository.findById(2024123456L);

        // Then
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getStudentNumber()).isEqualTo(2024123456L);
    }

    @Test
    @DisplayName("학번 중복 확인")
    void existsByStudentId() {
        // Given
        User user = createAndPersistUser(2024123456L, "student@example.com");
        createAndPersistStudent(user, 2024, 1);
        entityManager.clear();

        // When & Then
        assertThat(studentRepository.existsById(2024123456L)).isTrue();
        assertThat(studentRepository.existsById(2024999999L)).isFalse();
    }

    @Test
    @DisplayName("입학년도별 학생 목록 조회")
    void findByAdmissionYear() {
        // Given
        User user1 = createAndPersistUser(2024123456L, "student1@example.com");
        createAndPersistStudent(user1, 2024, 1);

        User user2 = createAndPersistUser(2024123457L, "student2@example.com");
        createAndPersistStudent(user2, 2024, 1);

        User user3 = createAndPersistUser(2023123456L, "student3@example.com");
        createAndPersistStudent(user3, 2023, 2);
        entityManager.clear();

        // When
        List<Student> students2024 = studentRepository.findByAdmissionYear(2024);

        // Then
        assertThat(students2024).hasSize(2);
        assertThat(students2024).extracting(Student::getAdmissionYear)
                .containsOnly(2024);
    }

    @Test
    @DisplayName("학번 패턴으로 학생 검색")
    void findByStudentIdPattern() {
        // Given
        User user1 = createAndPersistUser(2024123456L, "student1@example.com");
        createAndPersistStudent(user1, 2024, 1);

        User user2 = createAndPersistUser(2024123457L, "student2@example.com");
        createAndPersistStudent(user2, 2024, 1);
        entityManager.clear();

        // When
        List<Student> students = studentRepository.findByStudentIdPattern("2024%");

        // Then
        assertThat(students).hasSize(2);
        assertThat(students).extracting(Student::getStudentNumber)
                .allMatch(num -> String.valueOf(num).startsWith("2024"));
    }
}
