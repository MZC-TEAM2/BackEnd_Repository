package com.mzc.backend.lms.domains.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 댓글 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDto {
	
	@NotNull(message = "게시글 ID는 필수입니다")
	private Long postId;
	
	// 인증된 사용자로부터 설정됨 (Controller에서 주입)
	private Long authorId;
	
	private Long parentCommentId; // null이면 일반 댓글, 값이 있으면 대댓글
	
	@NotBlank(message = "내용은 필수입니다")
	private String content;
	
	private List<Long> attachmentIds; // 첨부파일 ID 목록
	
	/**
	 * 인증된 사용자 ID 설정 (Controller에서 호출)
	 */
	public void setAuthorIdFromAuth(Long authorId) {
		this.authorId = authorId;
	}
}
