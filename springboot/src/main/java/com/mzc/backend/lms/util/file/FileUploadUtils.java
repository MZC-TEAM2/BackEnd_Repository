package com.mzc.backend.lms.util.file;

import com.github.f4b6a3.ulid.UlidCreator;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.AttachmentType;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FileUploadUtils {

    private final Path uploadPath;
    private final AttachmentRepository attachmentRepository;
    
    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize; // 10MB

    public FileUploadUtils(
            @Value("${file.upload.dir:uploads}") String uploadDir,
            AttachmentRepository attachmentRepository) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.attachmentRepository = attachmentRepository;
        
        try {
            Files.createDirectories(this.uploadPath);
            log.info("파일 업로드 디렉토리 초기화: {}", this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 디렉토리 생성 실패", e);
        }
    }

    /**
     * 파일 업로드 및 DB 저장
     * @param post 게시글 엔티티
     * @param files 업로드할 파일 목록
     * @return 저장된 Attachment 엔티티 목록
     */
    @Transactional
    public List<Attachment> uploadFiles(Post post, List<MultipartFile> files) {
        List<Attachment> attachments = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return attachments;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            // 1. 파일 검증
            validateFile(file);
            
            // 2. 파일 저장
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String storedName = generateStoredFileName(extension);
            String relativePath = generateRelativePath(storedName);
            
            try {
                Path targetPath = uploadPath.resolve(relativePath);
                Files.createDirectories(targetPath.getParent());
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // 3. DB 저장
                AttachmentType attachmentType = AttachmentType.fromExtension(extension);
                Attachment attachment = Attachment.builder()
                        .post(post)
                        .originalName(originalFilename)
                        .storedName(storedName)
                        .filePath(relativePath)
                        .fileSize(file.getSize())
                        .attachmentType(attachmentType)
                        .build();
                
                attachments.add(attachmentRepository.save(attachment));
                
                log.info("파일 업로드 완료: original={}, stored={}, size={} bytes", 
                        originalFilename, storedName, file.getSize());
                
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", originalFilename, e);
                throw new BoardException(BoardErrorCode.FILE_UPLOAD_FAILED);
            }
        }
        
        return attachments;
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        // 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            throw new BoardException(BoardErrorCode.FILE_SIZE_EXCEEDED);
        }
        
        // 파일명 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BoardException(BoardErrorCode.INVALID_FILE_NAME);
        }
        
        // 확장자 검증
        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty()) {
            throw new BoardException(BoardErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    /**
     * ULID 기반 저장 파일명 생성
     */
    private String generateStoredFileName(String extension) {
        String ulid = UlidCreator.getUlid().toString();
        return extension.isEmpty() ? ulid : ulid + "." + extension;
    }

    /**
     * 일자별 상대 경로 생성 (yyyy/MM/dd/filename)
     */
    private String generateRelativePath(String storedName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + "/" + storedName;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 파일 삭제 (물리적 삭제)
     */
    public void deleteFile(String filePath) {
        try {
            Path targetPath = uploadPath.resolve(filePath);
            Files.deleteIfExists(targetPath);
            log.info("파일 삭제 완료: {}", filePath);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filePath, e);
        }
    }

    /**
     * 게시글의 모든 첨부파일 삭제
     */
    @Transactional
    public void deletePostFiles(Post post) {
        List<Attachment> attachments = attachmentRepository.findByPost(post);
        
        for (Attachment attachment : attachments) {
            deleteFile(attachment.getFilePath());
        }
        
        attachmentRepository.deleteAll(attachments);
        log.info("게시글 첨부파일 삭제 완료: postId={}, count={}", post.getId(), attachments.size());
    }

    /**
     * 특정 첨부파일들 삭제 (ID 목록)
     * @param attachmentIds 삭제할 첨부파일 ID 목록
     */
    @Transactional
    public void deleteAttachmentsByIds(List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }

        for (Long attachmentId : attachmentIds) {
            attachmentRepository.findById(attachmentId).ifPresent(attachment -> {
                deleteFile(attachment.getFilePath());
                attachmentRepository.delete(attachment);
                log.info("첨부파일 삭제 완료: attachmentId={}, fileName={}", 
                        attachmentId, attachment.getOriginalName());
            });
        }
    }
}
