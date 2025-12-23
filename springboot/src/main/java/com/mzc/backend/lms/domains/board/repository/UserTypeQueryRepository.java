package com.mzc.backend.lms.domains.board.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 타입 조회 전용 Repository
 * board 도메인에서 사용자 타입을 확인하기 위한 경량 Repository
 * <p>
 * User 도메인과의 결합도를 낮추기 위해 필요한 쿼리만 제공합니다.
 */
@Repository
@RequiredArgsConstructor
public class UserTypeQueryRepository {
	
	private final EntityManager entityManager;
	
	/**
	 * 사용자 ID로 사용자 타입 코드 조회
	 *
	 * @param userId 사용자 ID
	 * @return 사용자 타입 코드 (STUDENT, PROFESSOR 등)
	 */
	public Optional<String> findUserTypeCodeByUserId(Long userId) {
		String jpql = """
				SELECT ut.typeCode
				FROM UserTypeMapping utm
				JOIN utm.userType ut
				WHERE utm.userId = :userId
				""";
		
		try {
			String typeCode = entityManager.createQuery(jpql, String.class)
					.setParameter("userId", userId)
					.getSingleResult();
			return Optional.ofNullable(typeCode);
		} catch (Exception e) {
			return Optional.empty();
		}
	}
	
	/**
	 * 사용자가 학생인지 확인
	 *
	 * @param userId 사용자 ID
	 * @return 학생 여부
	 */
	public boolean isStudent(Long userId) {
		return findUserTypeCodeByUserId(userId)
				.map(typeCode -> "STUDENT".equals(typeCode))
				.orElse(true); // 매핑이 없으면 학생으로 간주
	}
	
	/**
	 * 사용자가 교수인지 확인
	 *
	 * @param userId 사용자 ID
	 * @return 교수 여부
	 */
	public boolean isProfessor(Long userId) {
		return findUserTypeCodeByUserId(userId)
				.map(typeCode -> "PROFESSOR".equals(typeCode))
				.orElse(false);
	}
}
