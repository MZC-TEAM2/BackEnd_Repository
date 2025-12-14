package com.mzc.backend.lms.domains.board.controller;

import com.mzc.backend.lms.domains.board.dto.response.AttachmentResponseDto;
import com.mzc.backend.lms.domains.board.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AttachmentController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = {"USER"})
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttachmentService attachmentService;

    private AttachmentResponseDto mockAttachment;

    @BeforeEach
    void setUp() {
        mockAttachment = AttachmentResponseDto.builder()
                .id(1L)
                .originalName("test-file.txt")
                .storedName("uuid-test-file.txt")
                .filePath("/uploads/2025/01/13/uuid-test-file.txt")
                .fileSize(1024L)
                .attachmentType(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT)
                .downloadCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("단일 파일 업로드 성공")
    void uploadFile_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        when(attachmentService.uploadFile(any(), anyString(), any(), any()))
                .thenReturn(mockAttachment);

        // when & then
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("attachmentType", "POST_CONTENT")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("파일이 업로드되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.originalName").value("test-file.txt"))
                .andExpect(jsonPath("$.data.fileSize").value(1024));

        verify(attachmentService).uploadFile(any(), eq("POST_CONTENT"), eq(null), eq(null));
    }

    @Test
    @DisplayName("다중 파일 업로드 성공")
    void uploadMultipleFiles_Success() throws Exception {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "test1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content 2".getBytes()
        );

        AttachmentResponseDto attachment2 = AttachmentResponseDto.builder()
                .id(2L)
                .originalName("test-file2.txt")
                .storedName("uuid-test-file2.txt")
                .filePath("/uploads/2025/01/13/uuid-test-file2.txt")
                .fileSize(2048L)
                .attachmentType(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT)
                .downloadCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        List<AttachmentResponseDto> mockResults = Arrays.asList(mockAttachment, attachment2);

        when(attachmentService.uploadMultipleFiles(anyList(), anyString()))
                .thenReturn(mockResults);

        // when & then
        mockMvc.perform(multipart("/api/v1/attachments/upload/multiple")
                        .file(file1)
                        .file(file2)
                        .param("attachmentType", "POST_CONTENT")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("2개 파일이 업로드되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));

        verify(attachmentService).uploadMultipleFiles(anyList(), eq("POST_CONTENT"));
    }

    @Test
    @DisplayName("파일 다운로드 성공")
    void downloadFile_Success() throws Exception {
        // given
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        when(attachmentService.getAttachment(1L)).thenReturn(mockAttachment);
        when(attachmentService.getFile(1L)).thenReturn(tempFile);

        // when & then
        mockMvc.perform(get("/api/v1/attachments/1/download")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=\"test-file.txt\""));

        verify(attachmentService).getAttachment(1L);
        verify(attachmentService).getFile(1L);
    }

    @Test
    @DisplayName("첨부파일 정보 조회 성공")
    void getAttachmentInfo_Success() throws Exception {
        // given
        when(attachmentService.getAttachment(1L)).thenReturn(mockAttachment);

        // when & then
        mockMvc.perform(get("/api/v1/attachments/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.originalName").value("test-file.txt"))
                .andExpect(jsonPath("$.data.fileSize").value(1024));

        verify(attachmentService).getAttachment(1L);
    }

    @Test
    @DisplayName("첨부파일 삭제 성공")
    void deleteAttachment_Success() throws Exception {
        // given
        doNothing().when(attachmentService).deleteAttachment(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/attachments/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("파일이 삭제되었습니다."));

        verify(attachmentService).deleteAttachment(1L);
    }

    @Test
    @DisplayName("존재하지 않는 파일 조회 시 404 반환")
    void getAttachmentInfo_NotFound() throws Exception {
        // given
        when(attachmentService.getAttachment(999L))
                .thenThrow(new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/v1/attachments/999")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("첨부파일을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("파일 업로드 시 예외 발생")
    void uploadFile_Exception() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        when(attachmentService.uploadFile(any(), anyString(), any(), any()))
                .thenThrow(new RuntimeException("파일 저장 실패"));

        // when & then
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                        .file(file)
                        .param("attachmentType", "POST_CONTENT")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("파일 저장 실패"));
    }
}
