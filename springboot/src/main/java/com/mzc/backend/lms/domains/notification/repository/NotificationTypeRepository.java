package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 타입 Repository
 */
@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Integer> {

    /**
     * 타입 코드로 알림 타입 조회
     */
    Optional<NotificationType> findByTypeCode(String typeCode);

    /**
     * 타입 코드로 존재 여부 확인
     */
    boolean existsByTypeCode(String typeCode);

    /**
     * 카테고리로 알림 타입 목록 조회
     */
    List<NotificationType> findByCategory(String category);

    /**
     * 활성화된 알림 타입 목록 조회
     */
    List<NotificationType> findByIsActiveTrue();

    /**
     * 카테고리별 활성화된 알림 타입 목록 조회
     */
    @Query("SELECT nt FROM NotificationType nt WHERE nt.category = :category AND nt.isActive = true")
    List<NotificationType> findActiveByCateogry(@Param("category") String category);
}
