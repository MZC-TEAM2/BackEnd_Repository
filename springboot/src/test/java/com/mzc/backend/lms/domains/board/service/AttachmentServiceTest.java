package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.response.AttachmentResponseDto;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AttachmentService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    @TempDir
    Path tempDir;

    private Attachment mockAttachment;

    @BeforeEach
    void setUp() {
        // 임시 디렉토리를 업로드 경로로 설정
        ReflectionTestUtils.setField(attachmentService, "uploadDir", tempDir.toString());

        // mockAttachment 생성 - builder에 id가 없으므로 리플렉션 사용
        mockAttachment = Attachment.builder()
                .originalName("test.txt")
                .storedName("uuid-test.txt")
                .filePath(tempDir.toString() + "/2025/01/13/uuid-test.txt")
                .fileSize(1024L)
                .attachmentType(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT)
                .build();
        
        // id는 리플렉션으로 설정 (테스트용)
        ReflectionTestUtils.setField(mockAttachment, "id", 1L);
    }

    @Test
    @DisplayName("단일 파일 업로드 성공")
    void uploadFile_Success() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        when(attachmentRepository.save(any(Attachment.class)))
                .thenReturn(mockAttachment);

        // when
        AttachmentResponseDto result = attachmentService.uploadFile(file, "POST_CONTENT", null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalName()).isEqualTo("test.txt");
        assertThat(result.getAttachmentType()).isEqualTo(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT);
        
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    @DisplayName("다중 파일 업로드 성공")
    void uploadMultipleFiles_Success() throws IOException {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "test1.txt",
                "text/plain",
                "test content 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test2.txt",
                "text/plain",
                "test content 2".getBytes()
        );

        List<MultipartFile> files = Arrays.asList(file1, file2);

        Attachment attachment2 = Attachment.builder()
                .originalName("test2.txt")
                .storedName("uuid-test2.txt")
                .filePath(tempDir.toString() + "/2025/01/13/uuid-test2.txt")
                .fileSize(2048L)
                .attachmentType(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT)
                .build();
        ReflectionTestUtils.setField(attachment2, "id", 2L);

        when(attachmentRepository.save(any(Attachment.class)))
                .thenReturn(mockAttachment)
                .thenReturn(attachment2);

        // when
        List<AttachmentResponseDto> results = attachmentService.uploadMultipleFiles(files, "POST_CONTENT");

        // then
        assertThat(results).hasSize(2);
        // mockAttachment가 "test.txt"이므로 첫 번째 결과도 "test.txt"
        assertThat(results.get(0).getOriginalName()).isEqualTo("test.txt");
        assertThat(results.get(1).getOriginalName()).isEqualTo("test2.txt");
        
        verify(attachmentRepository, times(2)).save(any(Attachment.class));
    }

    @Test
    @DisplayName("첨부파일 조회 성공")
    void getAttachment_Success() {
        // given
        when(attachmentRepository.findById(1L))
                .thenReturn(Optional.of(mockAttachment));

        // when
        AttachmentResponseDto result = attachmentService.getAttachment(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOriginalName()).isEqualTo("test.txt");
        
        verify(attachmentRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 첨부파일 조회 시 예외 발생")
    void getAttachment_NotFound() {
        // given
        when(attachmentRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attachmentService.getAttachment(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("첨부파일을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("파일 다운로드 - 다운로드 카운트 증가")
    void getFile_IncrementDownloadCount() throws IOException {
        // given
        Path testFilePath = tempDir.resolve("test-download.txt");
        Files.write(testFilePath, "test content".getBytes());

        Attachment attachment = Attachment.builder()
                .originalName("test-download.txt")
                .storedName("test-download.txt")
                .filePath(testFilePath.toString())
                .fileSize(100L)
                .attachmentType(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT)
                .build();
        ReflectionTestUtils.setField(attachment, "id", 1L);
        ReflectionTestUtils.setField(attachment, "downloadCount", 5);

        when(attachmentRepository.findById(1L))
                .thenReturn(Optional.of(attachment));

        // when
        File result = attachmentService.getFile(1L);

        // then
        assertThat(result).exists();
        assertThat(attachment.getDownloadCount()).isEqualTo(6);
        
        verify(attachmentRepository).findById(1L);
    }

    @Test
    @DisplayName("첨부파일 삭제 성공")
    void deleteAttachment_Success() throws IOException {
        // given
        Path testFilePath = tempDir.resolve("test-delete.txt");
        Files.write(testFilePath, "test content".getBytes());

        Attachment attachment = Attachment.builder()
                .originalName("test-delete.txt")
                .storedName("test-delete.txt")
                .filePath(testFilePath.toString())
                .fileSize(100L)
                .attachmentType(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT)
                .build();
        ReflectionTestUtils.setField(attachment, "id", 1L);

        when(attachmentRepository.findById(1L))
                .thenReturn(Optional.of(attachment));

        // when
        attachmentService.deleteAttachment(1L);

        // then
        assertThat(testFilePath).doesNotExist();
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    @DisplayName("여러 ID로 첨부파일 목록 조회")
    void getAttachmentsByIds_Success() {
        // given
        Attachment attachment2 = Attachment.builder()
                .originalName("test2.txt")
                .storedName("uuid-test2.txt")
                .filePath(tempDir.toString() + "/2025/01/13/uuid-test2.txt")
                .fileSize(2048L)
                .attachmentType(com.mzc.backend.lms.domains.board.enums.AttachmentType.DOCUMENT)
                .build();
        ReflectionTestUtils.setField(attachment2, "id", 2L);

        List<Long> ids = Arrays.asList(1L, 2L);
        List<Attachment> attachments = Arrays.asList(mockAttachment, attachment2);

        when(attachmentRepository.findAllById(ids))
                .thenReturn(attachments);

        // when
        List<Attachment> results = attachmentService.getAttachmentsByIds(ids);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(1).getId()).isEqualTo(2L);
        
        verify(attachmentRepository).findAllById(ids);
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생")
    void uploadFile_EmptyFile() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> attachmentService.uploadFile(emptyFile, "POST_CONTENT", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일이 비어있습니다.");
    }

    @Test
    @DisplayName("파일명이 없는 파일 업로드 시 예외 발생")
    void uploadFile_NoFileName() {
        // given
        MockMultipartFile fileWithoutName = new MockMultipartFile(
                "file",
                "",
                "text/plain",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> attachmentService.uploadFile(fileWithoutName, "POST_CONTENT", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일명이 없습니다.");
    }
}
