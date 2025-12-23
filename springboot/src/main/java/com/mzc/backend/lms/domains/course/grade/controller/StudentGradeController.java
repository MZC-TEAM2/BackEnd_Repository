package com.mzc.backend.lms.domains.course.grade.controller;

import com.mzc.backend.lms.domains.course.grade.dto.StudentGradeResponseDto;
import com.mzc.backend.lms.domains.course.grade.service.StudentGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/student/grades")
@RequiredArgsConstructor
public class StudentGradeController {

    private final StudentGradeService studentGradeService;

    /**
     * 학생 성적 조회 (지난 학기 포함)
     * - 본인(studentId) 기준
     * - 공개된(PUBLISHED) 성적만 반환
     * - academicTermId로 학기 필터 가능
     */
    @GetMapping
    public ResponseEntity<?> listMyGrades(
            @AuthenticationPrincipal Long studentId,
            @RequestParam(required = false) Long academicTermId
    ) {
        try {
            if (studentId == null) {
                return ResponseEntity.status(401).body(error("인증이 필요합니다."));
            }

            List<StudentGradeResponseDto> data = studentGradeService.listPublishedGrades(studentId, academicTermId);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", data);
            res.put("count", data.size());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("학생 성적 조회 실패 studentId={}, academicTermId={}", studentId, academicTermId, e);
            return ResponseEntity.internalServerError().body(error("학생 성적 조회에 실패했습니다."));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", message);
        return res;
    }
}


