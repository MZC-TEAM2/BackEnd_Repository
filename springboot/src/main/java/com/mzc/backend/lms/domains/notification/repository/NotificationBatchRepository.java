package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.NotificationBatch;
import com.mzc.backend.lms.domains.notification.entity.NotificationBatch.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 대량 알림 배치 Repository
 */
@Repository
public interface NotificationBatchRepository extends JpaRepository<NotificationBatch, Long> {
	
	/**
	 * 상태로 배치 목록 조회
	 */
	List<NotificationBatch> findByStatus(BatchStatus status);
	
	/**
	 * 처리 대기 중인 배치 목록 조회
	 */
	@Query("SELECT nb FROM NotificationBatch nb WHERE nb.status = 'PENDING' ORDER BY nb.createdAt ASC")
	List<NotificationBatch> findPendingBatches();
	
	/**
	 * 강의 ID로 배치 목록 조회
	 */
	Page<NotificationBatch> findByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);
	
	/**
	 * 발신자 ID로 배치 목록 조회
	 */
	Page<NotificationBatch> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);
	
	/**
	 * 특정 상태의 배치 개수 조회
	 */
	long countByStatus(BatchStatus status);
	
	/**
	 * 처리 중인 배치 목록 조회
	 */
	@Query("SELECT nb FROM NotificationBatch nb WHERE nb.status = 'PROCESSING'")
	List<NotificationBatch> findProcessingBatches();
}
