package com.mzc.backend.lms.domains.user.profile.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 주 연락처 엔티티 (1:1 관계)
 * user_primary_contacts 테이블과 매핑
 */
@Entity
@Table(name = "user_primary_contacts",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mobile_number"})
    },
    indexes = {
        @Index(name = "idx_primary_contact_mobile", columnList = "mobile_number"),
        @Index(name = "idx_primary_contact_verified", columnList = "mobile_verified")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPrimaryContact {

    @Id
    @Column(name = "user_id", length = 20)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "mobile_number", length = 50)
    private String mobileNumber;  // 암호화된 휴대폰 번호

    @Column(name = "home_number", length = 50)
    private String homeNumber;    // 암호화된 집 전화번호

    @Column(name = "office_number", length = 50)
    private String officeNumber;  // 암호화된 사무실 번호

    @Column(name = "mobile_verified")
    private Boolean mobileVerified = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private UserPrimaryContact(User user, String mobileNumber, String homeNumber, String officeNumber) {
        this.user = user;
        this.userId = user.getId();
        this.mobileNumber = mobileNumber;
        this.homeNumber = homeNumber;
        this.officeNumber = officeNumber;
        this.mobileVerified = false;
    }

    /**
     * 주 연락처 생성
     */
    public static UserPrimaryContact create(User user, String mobileNumber) {
        return UserPrimaryContact.builder()
                .user(user)
                .mobileNumber(mobileNumber)
                .build();
    }

    /**
     * 모바일 번호 업데이트
     */
    public void updateMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
        this.mobileVerified = false;
    }

    /**
     * 집 전화번호 업데이트
     */
    public void updateHomeNumber(String homeNumber) {
        this.homeNumber = homeNumber;
    }

    /**
     * 사무실 번호 업데이트
     */
    public void updateOfficeNumber(String officeNumber) {
        this.officeNumber = officeNumber;
    }

    /**
     * 모바일 번호 인증 완료
     */
    public void verifyMobile() {
        this.mobileVerified = true;
    }

    /**
     * 주 연락처 (모바일) 가져오기
     */
    public String getPrimaryContactNumber() {
        return this.mobileNumber;
    }
}