package com.mzc.backend.lms.domains.course.grade.controller;

import com.mzc.backend.lms.domains.course.grade.dto.ProfessorCourseGradesResponseDto;
import com.mzc.backend.lms.domains.course.grade.service.ProfessorGradeQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/professor/courses")
@RequiredArgsConstructor
public class ProfessorGradeQueryController {

    private final ProfessorGradeQueryService professorGradeQueryService;

    /**
     * 담당 강의 수강생 성적 전체 조회
     * - 담당 교수만 조회 가능
     * - status=ALL|PUBLISHED (기본 ALL)
     */
    @GetMapping("/{courseId}/grades")
    public ResponseEntity<?> listCourseGrades(
            @PathVariable Long courseId,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @AuthenticationPrincipal Long professorId
    ) {
        try {
            if (professorId == null) {
                return ResponseEntity.status(401).body(error("인증이 필요합니다."));
            }

            ProfessorGradeQueryService.GradeQueryStatus st;
            try {
                st = ProfessorGradeQueryService.GradeQueryStatus.valueOf(status);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(error("status는 ALL 또는 PUBLISHED 여야 합니다."));
            }

            List<ProfessorCourseGradesResponseDto> data = professorGradeQueryService.listCourseGrades(courseId, professorId, st);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", data);
            res.put("count", data.size());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("교수 강의 성적 조회 실패 courseId={}, status={}", courseId, status, e);
            return ResponseEntity.internalServerError().body(error("교수 강의 성적 조회에 실패했습니다."));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", message);
        return res;
    }
}


