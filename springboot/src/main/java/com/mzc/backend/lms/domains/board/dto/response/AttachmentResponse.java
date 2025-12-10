package com.mzc.backend.lms.domains.board.dto.response;

import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.enums.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 첨부파일 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AttachmentResponse {

    private Long id;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private AttachmentType attachmentType;
    private int downloadCount;
    private LocalDateTime createdAt;

    public static AttachmentResponse from(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .originalName(attachment.getOriginalName())
                .storedName(attachment.getStoredName())
                .filePath(attachment.getFilePath())
                .fileSize(attachment.getFileSize())
                .attachmentType(attachment.getAttachmentType())
                .downloadCount(attachment.getDownloadCount())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
