package com.mzc.backend.lms.domains.board.controller;

import com.mzc.backend.lms.domains.board.dto.response.AttachmentResponseDto;
import com.mzc.backend.lms.domains.board.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 첨부파일 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 단일 파일 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("attachmentType") String attachmentType) {
        
        log.info("파일 업로드 요청: {} ({})", file.getOriginalFilename(), attachmentType);
        
        try {
            AttachmentResponseDto result = attachmentService.uploadFile(file, attachmentType, null, null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "파일이 업로드되었습니다.");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("파일 업로드 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 다중 파일 업로드
     */
    @PostMapping("/upload/multiple")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("attachmentType") String attachmentType) {
        
        log.info("다중 파일 업로드 요청: {}개 파일 ({})", files.size(), attachmentType);
        
        try {
            List<AttachmentResponseDto> results = attachmentService.uploadMultipleFiles(files, attachmentType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", files.size() + "개 파일이 업로드되었습니다.");
            response.put("data", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("다중 파일 업로드 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 파일 다운로드
     */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId) {
        log.info("파일 다운로드 요청: {}", attachmentId);
        
        try {
            AttachmentResponseDto attachment = attachmentService.getAttachment(attachmentId);
            File file = attachmentService.getFile(attachmentId);
            
            Resource resource = new FileSystemResource(file);
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + attachment.getOriginalName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", attachmentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 첨부파일 정보 조회
     */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<?> getAttachmentInfo(@PathVariable Long attachmentId) {
        log.info("첨부파일 정보 조회: {}", attachmentId);
        
        try {
            AttachmentResponseDto attachment = attachmentService.getAttachment(attachmentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("data", attachment);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("첨부파일 조회 실패: {}", attachmentId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }

    /**
     * 첨부파일 삭제
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        log.info("첨부파일 삭제 요청: {}", attachmentId);
        
        try {
            attachmentService.deleteAttachment(attachmentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "파일이 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("첨부파일 삭제 실패: {}", attachmentId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
