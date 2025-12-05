package com.mzc.backend.lms.domains.user.profile.repository;

import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 프로필 이미지 Repository
 */
@Repository
public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {

    /**
     * 사용자의 현재 프로필 이미지 조회
     */
    Optional<UserProfileImage> findTopByUserIdAndIsCurrentTrueOrderByUploadedAtDesc(Long userId);

    /**
     * 사용자의 모든 프로필 이미지 조회
     */
    List<UserProfileImage> findByUserIdOrderByUploadedAtDesc(Long userId);

    /**
     * 사용자의 프로필 이미지 존재 여부 확인
     */
    boolean existsByUserIdAndIsCurrentTrue(Long userId);
}