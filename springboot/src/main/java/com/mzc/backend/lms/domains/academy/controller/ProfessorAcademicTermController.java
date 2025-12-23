package com.mzc.backend.lms.domains.academy.controller;

import com.mzc.backend.lms.domains.academy.service.ProfessorAcademicTermService;
import com.mzc.backend.lms.domains.course.course.dto.AcademicTermDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/professor/academic-terms")
@RequiredArgsConstructor
public class ProfessorAcademicTermController {

    private final ProfessorAcademicTermService professorAcademicTermService;

    /**
     * 교수 본인 담당 학기 목록 조회 (지난 학기 강의/성적 조회용 academicTermId 확인)
     */
    @GetMapping
    public ResponseEntity<?> listMyAcademicTerms(@AuthenticationPrincipal Long professorId) {
        try {
            if (professorId == null) {
                return ResponseEntity.status(401).body(error("인증이 필요합니다."));
            }
            List<AcademicTermDto> data = professorAcademicTermService.listMyAcademicTerms(professorId);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", data);
            res.put("count", data.size());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("교수 학기 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(error("교수 학기 목록 조회에 실패했습니다."));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", message);
        return res;
    }
}


