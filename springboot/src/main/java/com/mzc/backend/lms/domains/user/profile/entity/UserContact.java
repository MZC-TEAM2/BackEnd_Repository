package com.mzc.backend.lms.domains.user.profile.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 연락처 엔티티
 * user_contacts 테이블과 매핑
 */
@Entity
@Table(name = "user_contacts",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "contact_type", "contact_value"})
       },
       indexes = {
           @Index(name = "idx_user_contacts_user_id", columnList = "user_id")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "contact_type", length = 20, nullable = false)
    private String contactType;  // MOBILE, HOME, OFFICE

    @Column(name = "contact_value", length = 50, nullable = false)
    private String contactValue;  // 실제 연락처 번호

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "verified")
    private Boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private UserContact(User user, String contactType, String contactValue, Boolean isPrimary) {
        this.user = user;
        this.contactType = contactType;
        this.contactValue = contactValue;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.verified = false;
    }

    /**
     * 연락처 생성
     */
    public static UserContact create(User user, ContactType type, String contactValue, Boolean isPrimary) {
        return UserContact.builder()
                .user(user)
                .contactType(type.name())
                .contactValue(contactValue)
                .isPrimary(isPrimary)
                .build();
    }

    /**
     * 연락처 유형 열거형
     */
    public enum ContactType {
        MOBILE("휴대폰"),
        HOME("자택"),
        OFFICE("사무실");

        private final String description;

        ContactType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 주 연락처로 설정
     */
    public void setPrimary() {
        this.isPrimary = true;
    }

    /**
     * 주 연락처 해제
     */
    public void unsetPrimary() {
        this.isPrimary = false;
    }

    /**
     * 인증 완료 처리
     */
    public void verify() {
        this.verified = true;
    }
}
