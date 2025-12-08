package com.mzc.backend.lms.domains.user.profile.repository;

import com.mzc.backend.lms.domains.user.profile.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 프로필 이미지 Repository (1:1 관계)
 */
@Repository
public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {

    /**
     * 사용자 ID로 프로필 이미지 조회
     */
    Optional<UserProfileImage> findByUserId(Long userId);

    /**
     * 여러 사용자의 프로필 이미지 일괄 조회
     */
    @Query("SELECT upi FROM UserProfileImage upi WHERE upi.userId IN :userIds")
    List<UserProfileImage> findByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 프로필 이미지가 있는 사용자 ID 목록 조회
     */
    @Query("SELECT upi.userId FROM UserProfileImage upi WHERE upi.userId IN :userIds")
    List<Long> findUserIdsWithProfileImage(@Param("userIds") List<Long> userIds);
}