package com.mzc.backend.lms.domains.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 댓글 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequestDto {
	
	@NotBlank(message = "내용은 필수입니다")
	private String content;
	
	// 새로 추가할 첨부파일 ID 목록
	private List<Long> attachmentIds;
	
	// 삭제할 첨부파일 ID 목록
	private List<Long> removedAttachmentIds;
}
