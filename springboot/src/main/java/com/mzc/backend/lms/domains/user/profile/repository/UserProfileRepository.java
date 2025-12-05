package com.mzc.backend.lms.domains.user.profile.repository;

import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 프로필 Repository
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 사용자 ID로 프로필 조회
     */
    Optional<UserProfile> findByUserId(Long userId);

    // ==================== View Service용 배치 조회 ====================

    /**
     * 여러 User ID로 프로필 일괄 조회
     */
    @Query("SELECT p FROM UserProfile p WHERE p.userId IN :userIds")
    List<UserProfile> findByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * User ID 존재 여부 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 여러 User ID의 이름만 조회 (프로젝션)
     */
    @Query("SELECT p.userId, p.name FROM UserProfile p WHERE p.userId IN :userIds")
    List<Object[]> findNamesByUserIds(@Param("userIds") List<Long> userIds);
}