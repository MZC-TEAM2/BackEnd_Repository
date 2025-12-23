package com.mzc.backend.lms.domains.board.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게시글/댓글 좋아요 통합 엔티티 (V5 스키마)
 * 사용자의 게시글/댓글 좋아요 이력 관리
 * <p>
 * Note: Soft Delete는 UNIQUE 제약조건과 충돌하므로 Hard Delete 사용
 */
@Entity
@Table(name = "post_likes",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_user_post_like", columnNames = {"user_id", "post_id"}),
				@UniqueConstraint(name = "uk_user_comment_like", columnNames = {"user_id", "comment_id"})
		},
		indexes = {
				@Index(name = "idx_post_likes_user_id", columnList = "user_id"),
				@Index(name = "idx_post_likes_post_id", columnList = "post_id"),
				@Index(name = "idx_post_likes_comment_id", columnList = "comment_id"),
				@Index(name = "idx_post_likes_type", columnList = "like_type")
		})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "like_type", nullable = false, length = 20)
	private LikeType likeType;
	
	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	// deleted_at은 DB 스키마에는 있지만, Hard Delete 사용으로 애플리케이션에서는 미사용
	
	@Builder
	public PostLike(User user, Post post, Comment comment, LikeType likeType) {
		this.user = user;
		this.post = post;
		this.comment = comment;
		this.likeType = likeType;
	}
	
	/**
	 * 게시글 좋아요 생성
	 */
	public static PostLike create(User user, Post post) {
		return PostLike.builder()
				.user(user)
				.post(post)
				.likeType(LikeType.POST)
				.build();
	}
	
	/**
	 * 댓글 좋아요 생성
	 */
	public static PostLike createForComment(User user, Comment comment) {
		return PostLike.builder()
				.user(user)
				.comment(comment)
				.likeType(LikeType.COMMENT)
				.build();
	}
	
	/**
	 * 좋아요 타입 Enum
	 */
	public enum LikeType {
		POST,    // 게시글 좋아요
		COMMENT  // 댓글 좋아요
	}
}
