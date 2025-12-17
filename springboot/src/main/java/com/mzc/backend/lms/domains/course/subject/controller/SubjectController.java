package com.mzc.backend.lms.domains.course.subject.controller;

import com.mzc.backend.lms.domains.course.subject.dto.SubjectDetailResponse;
import com.mzc.backend.lms.domains.course.subject.dto.SubjectResponse;
import com.mzc.backend.lms.domains.course.subject.dto.SubjectSearchResponse;
import com.mzc.backend.lms.domains.course.subject.dto.PageResponse;
import com.mzc.backend.lms.domains.course.subject.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 과목 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * 10.1 과목 목록 조회
     * GET /api/v1/subjects
     */
    @GetMapping
    public ResponseEntity<?> getSubjects(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false, defaultValue = "false") Boolean showAllDepartments,
            @RequestParam(required = false) String courseType,
            @RequestParam(required = false) Integer credits,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            Authentication authentication
    ) {
        // 인증 정보에서 userId와 userType 추출
        Long userId = Long.parseLong(authentication.getName());
        String userType = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.equals("PROFESSOR") || auth.equals("STUDENT"))
                .findFirst()
                .orElse("STUDENT");

        // 페이지 요청 생성 (과목코드 기준 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by("subjectCode").ascending());

        Page<SubjectResponse> subjects = subjectService.getSubjects(
                userId,
                userType,
                keyword,
                departmentId,
                showAllDepartments,
                courseType,
                credits,
                isActive,
                pageable
        );

        return ResponseEntity.ok(createSuccessResponse(PageResponse.from(subjects)));
    }

    /**
     * 10.2 과목 상세 조회
     * GET /api/v1/subjects/{subjectId}
     */
    @GetMapping("/{subjectId}")
    public ResponseEntity<?> getSubjectDetail(
            @PathVariable Long subjectId
    ) {
        SubjectDetailResponse subject = subjectService.getSubjectDetail(subjectId);
        return ResponseEntity.ok(createSuccessResponse(subject));
    }

    /**
     * 10.3 과목 검색 (페이징 지원)
     * GET /api/v1/subjects/search
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchSubjects(
            @RequestParam String q,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        // 페이지 파라미터 파싱 (프론트엔드 오류 방어)
        int pageNumber = 0;
        int pageSize = 20;
        
        try {
            if (page != null && !page.isEmpty() && !page.contains("object")) {
                pageNumber = Integer.parseInt(page);
            }
        } catch (NumberFormatException e) {
            pageNumber = 0;
        }
        
        try {
            if (size != null && !size.isEmpty() && !size.contains("object")) {
                pageSize = Integer.parseInt(size);
            }
        } catch (NumberFormatException e) {
            pageSize = 20;
        }
        
        // size 최대값 제한
        int validSize = Math.min(pageSize, 50);
        
        // 페이지 요청 생성 (과목코드 기준 정렬)
        Pageable pageable = PageRequest.of(pageNumber, validSize, Sort.by("subjectCode").ascending());
        
        Page<SubjectSearchResponse> subjects = subjectService.searchSubjects(q, pageable);
        return ResponseEntity.ok(createSuccessResponse(PageResponse.from(subjects)));
    }

    /**
     * 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }
}

