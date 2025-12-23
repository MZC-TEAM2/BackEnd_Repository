package com.mzc.backend.lms.domains.course.subject.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 DTO (Spring Page 표준 구조)
 */
@Getter
@Builder
public class PageResponse<T> {
	private List<T> content;
	private long totalElements;
	private int totalPages;
	private int size;
	private int number;
	private boolean first;
	private boolean last;
	private int numberOfElements;
	private boolean empty;
	
	/**
	 * Spring Page 객체를 PageResponse로 변환
	 */
	public static <T> PageResponse<T> from(Page<T> page) {
		return PageResponse.<T>builder()
				.content(page.getContent())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.size(page.getSize())
				.number(page.getNumber())
				.first(page.isFirst())
				.last(page.isLast())
				.numberOfElements(page.getNumberOfElements())
				.empty(page.isEmpty())
				.build();
	}
}

