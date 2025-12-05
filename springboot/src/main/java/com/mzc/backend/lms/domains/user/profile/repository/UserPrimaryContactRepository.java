package com.mzc.backend.lms.domains.user.profile.repository;

import com.mzc.backend.lms.domains.user.profile.entity.UserPrimaryContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 주 연락처 Repository (1:1 관계)
 */
@Repository
public interface UserPrimaryContactRepository extends JpaRepository<UserPrimaryContact, Long> {

    /**
     * 사용자 ID로 주 연락처 조회
     */
    Optional<UserPrimaryContact> findByUserId(Long userId);

    /**
     * 여러 사용자의 주 연락처 일괄 조회
     */
    @Query("SELECT upc FROM UserPrimaryContact upc WHERE upc.userId IN :userIds")
    List<UserPrimaryContact> findByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 모바일 번호 존재 여부 확인 (암호화된 값으로 체크)
     */
    boolean existsByMobileNumber(String encryptedMobileNumber);
}