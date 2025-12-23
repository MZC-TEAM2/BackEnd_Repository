package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장바구니 일괄 삭제 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartBulkDeleteRequestDto {
	private List<Long> cartIds;
}
