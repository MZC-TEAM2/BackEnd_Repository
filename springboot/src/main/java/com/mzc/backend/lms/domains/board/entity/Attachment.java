package com.mzc.backend.lms.domains.board.entity;

import com.mzc.backend.lms.domains.board.enums.AttachmentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 첨부파일 엔터티
 * 게시글에 포함된 파일 정보 관리
 */
@Entity
@Table(name = "attachments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;
	
	@Column(name = "original_name", nullable = false)
	private String originalName;
	
	@Column(name = "stored_name", nullable = false)
	private String storedName;
	
	@Column(name = "file_path", nullable = false)
	private String filePath;
	
	@Column(name = "file_size", nullable = false)
	private Long fileSize;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "attachment_type", nullable = false, length = 20)
	private AttachmentType attachmentType;
	
	@Column(name = "download_count", nullable = false)
	private int downloadCount = 0;
	
	@Builder
	public Attachment(Post post, Comment comment, String originalName, String storedName, String filePath, Long fileSize, AttachmentType attachmentType) {
		this.post = post;
		this.comment = comment;
		this.originalName = originalName;
		this.storedName = storedName;
		this.filePath = filePath;
		this.fileSize = fileSize;
		this.attachmentType = attachmentType;
	}
	
	// --- 비즈니스 로직 ---
	
	public void increaseDownloadCount() {
		this.downloadCount++;
	}
	
	/**
	 * 댓글에 첨부파일 연결
	 */
	public void attachToComment(Comment comment) {
		this.comment = comment;
	}
	
	/**
	 * 게시글에 첨부파일 연결
	 */
	public void attachToPost(Post post) {
		this.post = post;
	}
}
