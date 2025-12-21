package com.mzc.backend.lms.domains.course.course.controller;

import com.mzc.backend.lms.domains.course.course.dto.UpdateWeekContentRequestDto;
import com.mzc.backend.lms.domains.course.course.dto.WeekContentDto;
import com.mzc.backend.lms.domains.course.course.service.CourseWeekContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 콘텐츠 단건(교수용) 관리 컨트롤러
 * 문서 스펙: /api/v1/professor/contents/{contentId}
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/professor/contents")
@RequiredArgsConstructor
public class ProfessorWeekContentController {

    private final CourseWeekContentService courseWeekContentService;

    /**
     * 콘텐츠 수정 (단일 contentId)
     */
    @PutMapping("/{contentId}")
    public ResponseEntity<?> updateContent(
            @PathVariable Long contentId,
            @RequestBody UpdateWeekContentRequestDto request,
            Authentication authentication
    ) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            Long professorId = Long.parseLong(authentication.getName());
            WeekContentDto response = courseWeekContentService.updateContentByContentId(contentId, request, professorId);
            return ResponseEntity.ok(createSuccessResponse(response, "콘텐츠가 수정되었습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("콘텐츠 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("콘텐츠 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 콘텐츠 삭제 (단일 contentId)
     */
    @DeleteMapping("/{contentId}")
    public ResponseEntity<?> deleteContent(
            @PathVariable Long contentId,
            Authentication authentication
    ) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            Long professorId = Long.parseLong(authentication.getName());
            courseWeekContentService.deleteContentByContentId(contentId, professorId);
            return ResponseEntity.ok(createSuccessResponse(null, "콘텐츠가 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("콘텐츠 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("콘텐츠 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}


