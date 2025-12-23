package com.mzc.backend.lms.domains.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

/**
 * 해시태그 엔티티
 * 게시글에 태그를 달아 분류 및 검색을 용이하게 함
 */
@Entity
@Table(name = "hashtags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag extends AuditableEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	/**
	 * 태그 이름 (소문자 저장, 검색용)
	 * 예: "ai윤리토론"
	 */
	@Column(name = "name", nullable = false, unique = true, length = 50)
	private String name;
	
	/**
	 * 화면 표시용 태그명
	 * 예: "AI윤리토론"
	 */
	@Column(name = "display_name", nullable = false, length = 50)
	private String displayName;
	
	/**
	 * 태그 설명
	 */
	@Column(name = "description")
	private String description;
	
	/**
	 * 태그 색상 (HEX 코드)
	 * 예: "#007bff"
	 */
	@Column(name = "color", length = 7)
	@ColumnDefault("'#007bff'")
	private String color = "#007bff";
	
	/**
	 * 태그 카테고리
	 * 예: "DISCUSSION", "FREE", "QUESTION" 등
	 */
	@Column(name = "tag_category", length = 30)
	private String tagCategory;
	
	/**
	 * 활성화 상태
	 */
	@Column(name = "is_active", nullable = false)
	@ColumnDefault("1")
	private boolean isActive = true;
	
	@Builder
	public Hashtag(String name, String displayName, String description,
	               String color, String tagCategory, Long createdBy) {
		super(createdBy);
		this.name = name != null ? name.toLowerCase().trim() : null;
		this.displayName = displayName;
		this.description = description;
		this.color = color != null ? color : "#007bff";
		this.tagCategory = tagCategory;
		this.isActive = true;
	}
	
	// --- 비즈니스 로직 ---
	
	/**
	 * 해시태그 활성화
	 */
	public void activate() {
		this.isActive = true;
	}
	
	/**
	 * 해시태그 비활성화
	 */
	public void deactivate() {
		this.isActive = false;
	}
	
	/**
	 * 해시태그 정보 업데이트
	 */
	public void update(String displayName, String description, String color) {
		if (displayName != null) {
			this.displayName = displayName;
		}
		if (description != null) {
			this.description = description;
		}
		if (color != null) {
			this.color = color;
		}
	}
}
