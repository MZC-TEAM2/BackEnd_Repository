package com.mzc.backend.lms.domains.academy.controller;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.repository.AcademicTermRepository;
import com.mzc.backend.lms.domains.course.course.dto.AcademicTermDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 현재 학기 조회 (공통)
 * - academic_terms의 start_date/end_date 범위로 현재 날짜에 해당하는 학기 반환
 * - 학생/교수 모두 사용 가능 (인증만 필요)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/academic-terms")
@RequiredArgsConstructor
public class AcademicTermCurrentController {
	
	private final AcademicTermRepository academicTermRepository;
	
	@GetMapping("/current")
	public ResponseEntity<?> getCurrentAcademicTerm(Authentication authentication) {
		try {
			if (authentication == null) {
				return ResponseEntity.status(401).body(error("인증이 필요합니다."));
			}
			
			AcademicTerm t = academicTermRepository.findCurrentTerms(LocalDate.now()).stream()
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("현재 날짜에 해당하는 학기가 없습니다."));
			
			AcademicTermDto data = AcademicTermDto.builder()
					.id(t.getId())
					.year(t.getYear())
					.termType(t.getTermType())
					.startDate(t.getStartDate())
					.endDate(t.getEndDate())
					.build();
			
			Map<String, Object> res = new HashMap<>();
			res.put("success", true);
			res.put("data", data);
			return ResponseEntity.ok(res);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(error(e.getMessage()));
		} catch (Exception e) {
			log.error("현재 학기 조회 실패", e);
			return ResponseEntity.internalServerError().body(error("현재 학기 조회에 실패했습니다."));
		}
	}
	
	private Map<String, Object> error(String message) {
		Map<String, Object> res = new HashMap<>();
		res.put("success", false);
		res.put("message", message);
		return res;
	}
}


