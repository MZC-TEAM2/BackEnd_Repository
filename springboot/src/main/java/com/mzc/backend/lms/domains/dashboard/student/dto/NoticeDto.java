package com.mzc.backend.lms.domains.dashboard.student.dto;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공지사항 응답 DTO
 * 대시보드용 최신 공지사항 정보
 */
@Getter
public class NoticeDto {

    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;
    private final Integer viewCount;

    public NoticeDto(Long id, String title, LocalDateTime createdAt, Integer viewCount) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
    }
}
