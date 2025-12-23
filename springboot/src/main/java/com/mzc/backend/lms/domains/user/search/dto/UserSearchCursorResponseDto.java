package com.mzc.backend.lms.domains.user.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 유저 탐색 커서 기반 응답 DTO
 */
@Getter
@Builder
public class UserSearchCursorResponseDto {
	
	private List<UserSearchResponseDto> content;
	
	private Long nextCursorId;
	
	private String nextCursorName;
	
	private boolean hasNext;
	
	public static UserSearchCursorResponseDto of(
			List<UserSearchResponseDto> content,
			int requestedSize,
			UserSearchRequestDto.SortBy sortBy
	) {
		boolean hasNext = content.size() > requestedSize;
		
		List<UserSearchResponseDto> resultContent = hasNext
				? content.subList(0, requestedSize)
				: content;
		
		Long nextCursorId = null;
		String nextCursorName = null;
		
		if (!resultContent.isEmpty()) {
			UserSearchResponseDto last = resultContent.get(resultContent.size() - 1);
			nextCursorId = last.getUserId();
			if (sortBy == UserSearchRequestDto.SortBy.NAME) {
				nextCursorName = last.getName();
			}
		}
		
		return UserSearchCursorResponseDto.builder()
				.content(resultContent)
				.nextCursorId(nextCursorId)
				.nextCursorName(nextCursorName)
				.hasNext(hasNext)
				.build();
	}
}
