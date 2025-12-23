package com.mzc.backend.lms.domains.user.user.repository;

import com.mzc.backend.lms.domains.user.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {
	
	@Autowired
	private UserRepository userRepository;
	
	private User testUser;
	
	@BeforeEach
	void setUp() {
		// Given: 테스트용 사용자 생성
		testUser = User.create(1001L, "test@example.com", "password123");
	}
	
	@Test
	@DisplayName("사용자 저장 및 조회")
	void saveAndFindUser() {
		// Given: 사용자 저장
		User savedUser = userRepository.save(testUser);
		
		// When: ID로 조회
		Optional<User> foundUser = userRepository.findById(savedUser.getId());
		
		// Then: 저장된 사용자 검증
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
		assertThat(foundUser.get().getPassword()).isEqualTo("password123");
	}
	
	@Test
	@DisplayName("이메일로 사용자 조회")
	void findByEmail() {
		// Given: 사용자 저장
		userRepository.save(testUser);
		
		// When: 이메일로 조회
		Optional<User> foundUser = userRepository.findByEmail("test@example.com");
		
		// Then: 조회된 사용자 검증
		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
	}
	
	@Test
	@DisplayName("이메일 중복 확인")
	void existsByEmail() {
		// Given: 사용자 저장
		userRepository.save(testUser);
		
		// When & Then: 이메일 존재 여부 확인
		assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
		assertThat(userRepository.existsByEmail("notexist@example.com")).isFalse();
	}
	
	@Test
	@DisplayName("소프트 삭제된 사용자는 활성 조회에서 제외")
	void findActiveUserExcludesDeleted() {
		// Given: 사용자 저장 및 삭제 처리
		User savedUser = userRepository.save(testUser);
		savedUser.delete();
		userRepository.save(savedUser);
		
		// When: 활성 사용자 조회
		Optional<User> activeUser = userRepository.findActiveById(savedUser.getId());
		
		// Then: 삭제된 사용자는 조회되지 않음
		assertThat(activeUser).isEmpty();
	}
	
	@Test
	@DisplayName("비밀번호 변경")
	void changePassword() {
		// Given: 사용자 저장
		User savedUser = userRepository.save(testUser);
		
		// When: 비밀번호 변경
		savedUser.changePassword("newPassword456");
		User updatedUser = userRepository.save(savedUser);
		
		// Then: 변경된 비밀번호 확인
		assertThat(updatedUser.getPassword()).isEqualTo("newPassword456");
	}
	
	@Test
	@DisplayName("사용자 복구")
	void restoreUser() {
		// Given: 사용자 저장 및 삭제
		User savedUser = userRepository.save(testUser);
		savedUser.delete();
		userRepository.save(savedUser);
		
		// When: 사용자 복구
		savedUser.restore();
		User restoredUser = userRepository.save(savedUser);
		
		// Then: 복구된 사용자는 활성 조회에 포함
		Optional<User> activeUser = userRepository.findActiveById(restoredUser.getId());
		assertThat(activeUser).isPresent();
		assertThat(activeUser.get().isDeleted()).isFalse();
	}
}
