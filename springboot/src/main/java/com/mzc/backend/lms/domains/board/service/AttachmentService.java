package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.dto.response.AttachmentResponseDto;
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.Comment;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.AttachmentType;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 첨부파일 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {
	
	private final AttachmentRepository attachmentRepository;
	
	@Value("${app.file.upload-dir:uploads}")
	private String uploadDir;
	
	/**
	 * 단일 파일 업로드
	 */
	@Transactional
	public AttachmentResponseDto uploadFile(MultipartFile file, String ignoredType, Post post, Comment comment) {
		log.info("파일 업로드 시작: {}", file.getOriginalFilename());
		
		// 파일 유효성 검사
		if (file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어있습니다.");
		}
		
		if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
			throw new IllegalArgumentException("파일명이 없습니다.");
		}
		
		try {
			// 파일 저장
			String storedFileName = saveFile(file);
			String filePath = getFilePath(storedFileName);
			
			// 파일 확장자로부터 AttachmentType 자동 결정
			String extension = getFileExtension(file.getOriginalFilename());
			AttachmentType attachmentType = AttachmentType.fromExtension(extension);
			
			// Attachment 엔티티 생성
			Attachment attachment = Attachment.builder()
					.post(post)
					.comment(comment)
					.originalName(file.getOriginalFilename())
					.storedName(storedFileName)
					.filePath(filePath)
					.fileSize(file.getSize())
					.attachmentType(attachmentType)
					.build();
			
			// DB 저장
			Attachment savedAttachment = attachmentRepository.save(attachment);
			log.info("파일 업로드 완료: ID={}, 파일명={}, 타입={}", savedAttachment.getId(), file.getOriginalFilename(), attachmentType);
			
			return AttachmentResponseDto.from(savedAttachment);
			
		} catch (IOException e) {
			log.error("파일 저장 실패: {}", file.getOriginalFilename(), e);
			throw new RuntimeException("파일 저장에 실패했습니다.", e);
		}
	}
	
	/**
	 * 다중 파일 업로드
	 */
	@Transactional
	public List<AttachmentResponseDto> uploadMultipleFiles(List<MultipartFile> files, String ignoredType) {
		log.info("다중 파일 업로드 시작: {}개", files.size());
		
		List<AttachmentResponseDto> results = new ArrayList<>();
		
		for (MultipartFile file : files) {
			AttachmentResponseDto result = uploadFile(file, ignoredType, null, null);
			results.add(result);
		}
		
		log.info("다중 파일 업로드 완료: {}개", results.size());
		return results;
	}
	
	/**
	 * 파일 확장자 추출
	 */
	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf(".") + 1);
	}
	
	/**
	 * 파일을 디스크에 저장
	 */
	private String saveFile(MultipartFile file) throws IOException {
		// 날짜별 디렉토리 구조 생성 (YYYY/MM/DD)
		LocalDate now = LocalDate.now();
		String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		
		// 저장 디렉토리 생성
		Path uploadPath = Paths.get(uploadDir, datePath);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		
		// 고유한 파일명 생성 (UUID + 원본 확장자)
		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		String storedFileName = UUID.randomUUID().toString() + extension;
		
		// 파일 저장
		Path filePath = uploadPath.resolve(storedFileName);
		file.transferTo(filePath.toFile());
		
		log.debug("파일 저장 완료: {}", filePath);
		
		// 저장된 파일의 상대 경로 반환
		return datePath + "/" + storedFileName;
	}
	
	/**
	 * 파일 경로 생성
	 */
	private String getFilePath(String storedFileName) {
		return "/" + uploadDir + "/" + storedFileName;
	}
	
	/**
	 * 첨부파일 조회
	 */
	public AttachmentResponseDto getAttachment(Long attachmentId) {
		Attachment attachment = attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));
		return AttachmentResponseDto.from(attachment);
	}
	
	/**
	 * 첨부파일 삭제
	 */
	@Transactional
	public void deleteAttachment(Long attachmentId) {
		Attachment attachment = attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new RuntimeException("첨부파일을 찾을 수 없습니다."));
		
		// 실제 파일 삭제
		try {
			Path filePath = Paths.get(uploadDir, attachment.getStoredName());
			Files.deleteIfExists(filePath);
			log.info("파일 삭제 완료: {}", filePath);
		} catch (IOException e) {
			log.error("파일 삭제 실패: {}", attachment.getStoredName(), e);
		}
		
		// DB에서 삭제
		attachmentRepository.delete(attachment);
		log.info("첨부파일 삭제 완료: ID={}", attachmentId);
	}
	
	/**
	 * 파일 다운로드를 위한 실제 파일 가져오기
	 */
	public File getFile(Long attachmentId) {
		Attachment attachment = attachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new RuntimeException("첨부파일을 찾을 수 없습니다."));
		
		Path filePath = Paths.get(uploadDir, attachment.getStoredName());
		File file = filePath.toFile();
		
		if (!file.exists()) {
			throw new RuntimeException("파일이 존재하지 않습니다.");
		}
		
		// 다운로드 횟수 증가
		attachment.increaseDownloadCount();
		attachmentRepository.save(attachment);
		
		return file;
	}
	
	/**
	 * attachmentIds로 Attachment 엔티티 목록 조회
	 */
	public List<Attachment> getAttachmentsByIds(List<Long> attachmentIds) {
		return attachmentRepository.findAllById(attachmentIds);
	}
}
