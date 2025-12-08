package com.mzc.backend.lms.domains.board.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 사용자 정보 추적 Base Entity
 * 생성자/수정자 정보를 포함하는 엔터티용
 */
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {
    
    /**
     * 생성자 ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
    /**
     * 수정자 ID
     */
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Constructors
    protected AuditableEntity() {}
    
    protected AuditableEntity(Long createdBy) {
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
    }
    
    // Getters
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    // Business Methods
    
    /**
     * 수정자 정보 업데이트
     */
    public void updateModifier(Long modifierId) {
        this.updatedBy = modifierId;
    }
    
    /**
     * 생성자와 수정자가 동일한지 확인
     */
    public boolean isModifiedByCreator() {
        return createdBy != null && createdBy.equals(updatedBy);
    }
    
    /**
     * 특정 사용자가 생성한 엔터티인지 확인
     */
    public boolean isCreatedBy(Long userId) {
        return createdBy != null && createdBy.equals(userId);
    }
    
    /**
     * 특정 사용자가 수정한 엔터티인지 확인
     */
    public boolean isUpdatedBy(Long userId) {
        return updatedBy != null && updatedBy.equals(userId);
    }
    
    // 테스트용 setter (protected)
    protected void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    protected void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}