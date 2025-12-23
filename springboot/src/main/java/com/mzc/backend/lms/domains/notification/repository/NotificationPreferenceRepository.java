package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 수신 설정 Repository
 */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
	
	/**
	 * 사용자 ID로 알림 수신 설정 목록 조회
	 */
	List<NotificationPreference> findByUserId(Long userId);
	
	/**
	 * 사용자 ID와 알림 타입 ID로 수신 설정 조회
	 */
	Optional<NotificationPreference> findByUserIdAndNotificationTypeId(Long userId, Integer notificationTypeId);
	
	/**
	 * 사용자의 활성화된 알림 수신 설정 목록 조회
	 */
	@Query("SELECT np FROM NotificationPreference np WHERE np.user.id = :userId AND np.isEnabled = true")
	List<NotificationPreference> findEnabledByUserId(@Param("userId") Long userId);
	
	/**
	 * 사용자의 이메일 알림이 활성화된 설정 목록 조회
	 */
	@Query("SELECT np FROM NotificationPreference np WHERE np.user.id = :userId AND np.emailEnabled = true")
	List<NotificationPreference> findEmailEnabledByUserId(@Param("userId") Long userId);
	
	/**
	 * 특정 알림 타입에 대해 알림 수신이 활성화된 사용자 ID 목록 조회
	 */
	@Query("SELECT np.user.id FROM NotificationPreference np WHERE np.notificationType.id = :typeId AND np.isEnabled = true")
	List<Long> findUserIdsWithEnabledNotification(@Param("typeId") Integer typeId);
	
	/**
	 * 사용자 ID와 알림 타입 ID로 존재 여부 확인
	 */
	boolean existsByUserIdAndNotificationTypeId(Long userId, Integer notificationTypeId);
}
