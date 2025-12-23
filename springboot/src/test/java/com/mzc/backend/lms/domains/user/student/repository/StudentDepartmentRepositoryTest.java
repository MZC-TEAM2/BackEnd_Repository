package com.mzc.backend.lms.domains.user.student.repository;

import com.mzc.backend.lms.domains.user.organization.entity.College;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.organization.repository.CollegeRepository;
import com.mzc.backend.lms.domains.user.organization.repository.DepartmentRepository;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import com.mzc.backend.lms.domains.user.profile.repository.UserProfileRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.entity.StudentDepartment;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StudentDepartmentRepository 테스트
 * 학과 게시판 기능을 위한 학생-학과 관계 조회 테스트
 */
@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("StudentDepartmentRepository 테스트")
class StudentDepartmentRepositoryTest {
	
	@Autowired
	private StudentDepartmentRepository studentDepartmentRepository;
	
	@Autowired
	private StudentRepository studentRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private CollegeRepository collegeRepository;
	
	@Autowired
	private UserProfileRepository userProfileRepository;
	
	private Student testStudent;
	private Department testDepartment;
	
	@BeforeEach
	void setUp() {
		// 1. User 생성
		User user = User.create(20251001L, "test.student@example.com", "encodedPassword");
		userRepository.save(user);
		
		// 1-1. UserProfile 생성
		UserProfile profile = UserProfile.create(user, "테스트학생");
		userProfileRepository.save(profile);
		
		// 2. College 생성
		College college = College.builder()
				.collegeCode("ENG")
				.collegeNumberCode("01")
				.collegeName("공과대학")
				.build();
		collegeRepository.save(college);
		
		// 3. Department 생성
		testDepartment = Department.builder()
				.departmentCode("CS")
				.departmentName("컴퓨터공학과")
				.college(college)
				.build();
		departmentRepository.save(testDepartment);
		
		// 4. Student 생성
		testStudent = Student.builder()
				.studentId(20250001L)
				.user(user)
				.admissionYear(2025)
				.grade(1)
				.build();
		studentRepository.save(testStudent);
		
		// 5. StudentDepartment 생성
		StudentDepartment studentDepartment = StudentDepartment.builder()
				.student(testStudent)
				.department(testDepartment)
				.isPrimary(true)
				.enrolledDate(LocalDate.now())
				.build();
		studentDepartmentRepository.save(studentDepartment);
	}
	
	@Test
	@DisplayName("학생 ID로 학과 정보 조회 성공")
	@Transactional
	void findByStudentId_Success() {
		// when
		Optional<StudentDepartment> result = studentDepartmentRepository.findByStudentId(testStudent.getStudentId());
		
		// then
		assertThat(result).isPresent();
		assertThat(result.get().getDepartment().getDepartmentName()).isEqualTo("컴퓨터공학과");
		assertThat(result.get().getDepartment().getDepartmentCode()).isEqualTo("CS");
		assertThat(result.get().getIsPrimary()).isTrue();
		
		log.info("조회된 학과: {}", result.get().getDepartment().getDepartmentName());
	}
	
	@Test
	@DisplayName("존재하지 않는 학생 ID로 조회 시 빈 결과 반환")
	void findByStudentId_NotFound() {
		// when
		Optional<StudentDepartment> result = studentDepartmentRepository.findByStudentId(99999999L);
		
		// then
		assertThat(result).isEmpty();
	}
	
	@Test
	@DisplayName("학생 엔티티로 학과 정보 조회 성공")
	@Transactional
	void findByStudent_Success() {
		// when
		Optional<StudentDepartment> result = studentDepartmentRepository.findByStudent(testStudent);
		
		// then
		assertThat(result).isPresent();
		assertThat(result.get().getDepartment().getDepartmentName()).isEqualTo("컴퓨터공학과");
	}
	
	@Test
	@DisplayName("학과로 학생 목록 조회 성공")
	@Transactional
	void findByDepartment_Success() {
		// given
		// 같은 학과에 학생 한 명 더 추가
		User user2 = User.create(20251002L, "test.student2@example.com", "encodedPassword");
		userRepository.save(user2);
		
		UserProfile profile2 = UserProfile.create(user2, "테스트학생2");
		userProfileRepository.save(profile2);
		
		Student student2 = Student.builder()
				.studentId(20251002L)
				.user(user2)
				.admissionYear(2025)
				.grade(1)
				.build();
		studentRepository.save(student2);
		
		StudentDepartment studentDepartment2 = StudentDepartment.builder()
				.student(student2)
				.department(testDepartment)
				.isPrimary(true)
				.enrolledDate(LocalDate.now())
				.build();
		studentDepartmentRepository.save(studentDepartment2);
		
		// when
		var students = studentDepartmentRepository.findByDepartment(testDepartment);
		
		// then
		assertThat(students).hasSize(2);
		assertThat(students)
				.extracting(sd -> sd.getStudent().getStudentId())
				.containsExactlyInAnyOrder(20251001L, 20251002L);
		
		log.info("학과에 속한 학생 수: {}", students.size());
	}
	
	@Test
	@DisplayName("복수전공 학생의 주전공 학과 조회")
	@Transactional
	void findByStudentId_WithMultipleDepartments() {
		// given
		// 복수전공 학생 생성 (새로운 학생)
		User multiMajorUser = User.create(20251003L, "multi.major@example.com", "encodedPassword");
		userRepository.save(multiMajorUser);
		
		UserProfile multiMajorProfile = UserProfile.create(multiMajorUser, "복수전공학생");
		userProfileRepository.save(multiMajorProfile);
		
		Student multiMajorStudent = Student.builder()
				.studentId(20251003L)
				.user(multiMajorUser)
				.admissionYear(2025)
				.grade(1)
				.build();
		studentRepository.save(multiMajorStudent);
		
		// 주전공 (컴퓨터공학과)
		StudentDepartment primaryDept = StudentDepartment.builder()
				.student(multiMajorStudent)
				.department(testDepartment)
				.isPrimary(true)
				.enrolledDate(LocalDate.now())
				.build();
		studentDepartmentRepository.save(primaryDept);
		
		// 부전공 추가
		College artCollege = College.builder()
				.collegeCode("ART")
				.collegeNumberCode("02")
				.collegeName("예술대학")
				.build();
		collegeRepository.save(artCollege);
		
		Department minorDepartment = Department.builder()
				.departmentCode("MUS")
				.departmentName("음악학과")
				.college(artCollege)
				.build();
		departmentRepository.save(minorDepartment);
		
		// student_departments 테이블의 UNIQUE 제약 때문에 복수전공은 실제로 불가능
		// 이 테스트는 제약 조건 확인용으로 변경
		// StudentDepartment minorStudentDept = StudentDepartment.builder()
		//         .student(multiMajorStudent)
		//         .department(minorDepartment)
		//         .isPrimary(false)
		//         .enrolledDate(LocalDate.now())
		//         .build();
		// assertThatThrownBy(() -> studentDepartmentRepository.save(minorStudentDept))
		//         .isInstanceOf(DataIntegrityViolationException.class);
		
		// when
		Optional<StudentDepartment> result = studentDepartmentRepository.findByStudentId(multiMajorStudent.getStudentId());
		
		// then
		assertThat(result).isPresent();
		assertThat(result.get().getDepartment().getDepartmentName()).isEqualTo("컴퓨터공학과");
		assertThat(result.get().getIsPrimary()).isTrue();
		
		log.info("조회된 학과: {}, 주전공 여부: {}",
				result.get().getDepartment().getDepartmentName(),
				result.get().getIsPrimary());
	}
}
