package com.mzc.backend.lms.domains.user.profile.repository;

import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}