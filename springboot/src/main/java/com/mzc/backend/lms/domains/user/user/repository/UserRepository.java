package com.mzc.backend.lms.domains.user.user.repository;

import com.mzc.backend.lms.domains.user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일로 사용자 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 삭제되지 않은 사용자 조회 (Soft Delete 고려)
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findActiveById(@Param("id") Long id);

    /**
     * 이메일로 삭제되지 않은 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findActiveByEmail(@Param("email") String email);

    /**
     * 특정 년도/단과대학/학과 조합의 최대 학번 순번 조회
     * 학번 형식: YYYYCCDDNNN (년도4 + 단과대학2 + 학과2 + 순번3)
     */
    @Query("SELECT MAX(CAST(SUBSTRING(CAST(u.id AS string), 9, 3) AS int)) " +
           "FROM User u " +
           "WHERE CAST(u.id AS string) LIKE CONCAT(:prefix, '%')")
    Optional<Integer> findMaxSequenceByPrefix(@Param("prefix") String prefix);

    /**
     * 탈퇴 후 지정 기간 경과한 사용자 ID 목록 조회 (스케줄러용)
     * Native Query로 @Where 절 우회
     *
     * @param threshold 기준 시간 (이 시간 이전에 삭제된 사용자 조회)
     * @return 하드 딜리트 대상 사용자 ID 목록
     */
    @Query(value = "SELECT id FROM users WHERE deleted_at IS NOT NULL AND deleted_at < :threshold",
           nativeQuery = true)
    List<Long> findUserIdsDeletedBefore(@Param("threshold") LocalDateTime threshold);

    /**
     * 탈퇴 후 지정 기간 경과한 사용자 하드 딜리트 (스케줄러용)
     * Native Query로 @Where 절 우회
     *
     * @param threshold 기준 시간
     * @return 삭제된 사용자 수
     */
    @Modifying
    @Query(value = "DELETE FROM users WHERE deleted_at IS NOT NULL AND deleted_at < :threshold",
           nativeQuery = true)
    int hardDeleteUsersDeletedBefore(@Param("threshold") LocalDateTime threshold);
}
