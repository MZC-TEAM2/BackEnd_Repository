package com.mzc.backend.lms.domains.user.search.controller;

import com.mzc.backend.lms.domains.user.search.dto.CollegeListResponseDto;
import com.mzc.backend.lms.domains.user.search.dto.DepartmentListResponseDto;
import com.mzc.backend.lms.domains.user.search.dto.UserSearchCursorResponseDto;
import com.mzc.backend.lms.domains.user.search.dto.UserSearchRequestDto;
import com.mzc.backend.lms.domains.user.search.service.UserSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 유저 탐색 컨트롤러 (커서 기반 무한스크롤)
 */
@Tag(name = "User Search", description = "유저 탐색 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    @Operation(summary = "유저 탐색", description = "단과대, 학과, 이름, 사용자 타입으로 유저를 탐색합니다. (커서 기반 무한스크롤)")
    @GetMapping("/search")
    public ResponseEntity<UserSearchCursorResponseDto> searchUsers(
            @Parameter(description = "검색 조건") @ModelAttribute UserSearchRequestDto request
    ) {
        UserSearchCursorResponseDto result = userSearchService.searchUsers(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "단과대 목록 조회", description = "유저 탐색 필터용 단과대 목록을 조회합니다.")
    @GetMapping("/colleges")
    public ResponseEntity<List<CollegeListResponseDto>> getColleges() {
        List<CollegeListResponseDto> result = userSearchService.getAllColleges();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "학과 목록 조회 (단과대별)", description = "특정 단과대의 학과 목록을 조회합니다.")
    @GetMapping("/colleges/{collegeId}/departments")
    public ResponseEntity<List<DepartmentListResponseDto>> getDepartmentsByCollege(
            @Parameter(description = "단과대 ID") @PathVariable Long collegeId
    ) {
        List<DepartmentListResponseDto> result = userSearchService.getDepartmentsByCollegeId(collegeId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "전체 학과 목록 조회", description = "모든 학과 목록을 조회합니다.")
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentListResponseDto>> getAllDepartments() {
        List<DepartmentListResponseDto> result = userSearchService.getAllDepartments();
        return ResponseEntity.ok(result);
    }
}
