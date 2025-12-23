package com.mzc.backend.lms.domains.academy.controller;

import com.mzc.backend.lms.domains.academy.service.StudentAcademicTermService;
import com.mzc.backend.lms.domains.course.course.dto.AcademicTermDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/student/academic-terms")
@RequiredArgsConstructor
public class StudentAcademicTermController {

    private final StudentAcademicTermService studentAcademicTermService;

    /**
     * 학생 본인 수강 학기 목록 조회 (성적 조회용 academicTermId 확인)
     */
    @GetMapping
    public ResponseEntity<?> listMyAcademicTerms(@AuthenticationPrincipal Long studentId) {
        try {
            if (studentId == null) {
                return ResponseEntity.status(401).body(error("인증이 필요합니다."));
            }

            List<AcademicTermDto> data = studentAcademicTermService.listMyAcademicTerms(studentId);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", data);
            res.put("count", data.size());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("학생 학기 목록 조회 실패 studentId={}", studentId, e);
            return ResponseEntity.internalServerError().body(error("학생 학기 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 현재 학기 조회 (활성화된 수강신청 기간 ENROLLMENT 기준)
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentAcademicTerm(@AuthenticationPrincipal Long studentId) {
        try {
            if (studentId == null) {
                return ResponseEntity.status(401).body(error("인증이 필요합니다."));
            }

            AcademicTermDto data = studentAcademicTermService.getCurrentAcademicTerm(LocalDateTime.now());

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("data", data);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("학생 현재 학기 조회 실패 studentId={}", studentId, e);
            return ResponseEntity.internalServerError().body(error("학생 현재 학기 조회에 실패했습니다."));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", message);
        return res;
    }
}


