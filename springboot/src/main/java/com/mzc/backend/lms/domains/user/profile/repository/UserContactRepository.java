package com.mzc.backend.lms.domains.user.profile.repository;

import com.mzc.backend.lms.domains.user.profile.entity.UserContact;
import com.mzc.backend.lms.domains.user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 연락처 Repository
 */
@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Long> {

    /**
     * 사용자로 연락처 목록 조회
     */
    List<UserContact> findByUser(User user);

    /**
     * 사용자 ID로 연락처 목록 조회
     */
    @Query("SELECT uc FROM UserContact uc WHERE uc.user.id = :userId")
    List<UserContact> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 주 연락처 조회
     */
    @Query("SELECT uc FROM UserContact uc WHERE uc.user.id = :userId AND uc.isPrimary = true")
    Optional<UserContact> findPrimaryContactByUserId(@Param("userId") Long userId);

    /**
     * 연락처 타입별 조회
     */
    List<UserContact> findByUserAndContactType(User user, String contactType);
}