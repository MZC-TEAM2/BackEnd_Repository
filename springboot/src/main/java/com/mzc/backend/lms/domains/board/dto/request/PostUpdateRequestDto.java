package com.mzc.backend.lms.domains.board.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequestDto {
    private Long categoryId;
    private String title;
    private String content;
    private String postType;
    private Boolean isAnonymous;
    private List<Long> attachmentIds; // 새로 추가할 첨부파일 ID 목록
    private List<Long> deleteAttachmentIds; // 삭제할 첨부파일 ID 목록

}
