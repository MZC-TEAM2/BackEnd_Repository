package com.mzc.backend.lms.domains.board.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통 Base Entity
 * 모든 엔터티가 상속받는 기본 필드 정의
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
	
	/**
	 * 생성 일시
	 */
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	/**
	 * 수정 일시
	 */
	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	/**
	 * 삭제 일시 (Soft Delete)
	 */
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
	/**
	 * 삭제 여부 (성능 최적화용)
	 */
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;
	
	// Constructors
	protected BaseEntity() {
	}
	
	// Getters
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	// 테스트용 setter (protected)
	protected void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	
	protected void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	// Business Methods
	
	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}
	
	public Boolean getIsDeleted() {
		return isDeleted;
	}
	
	/**
	 * Soft Delete 실행
	 */
	public void delete() {
		this.isDeleted = true;
		this.deletedAt = LocalDateTime.now();
	}
	
	/**
	 * Soft Delete 복구
	 */
	public void restore() {
		this.isDeleted = false;
		this.deletedAt = null;
	}
	
	/**
	 * 삭제된 엔터티 여부 확인
	 */
	public boolean isDeleted() {
		return Boolean.TRUE.equals(this.isDeleted);
	}
	
	/**
	 * 활성 엔터티 여부 확인
	 */
	public boolean isActive() {
		return !isDeleted();
	}
}
