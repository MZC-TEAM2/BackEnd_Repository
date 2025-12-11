package com.mzc.backend.lms.domains.board.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Board 도메인 에러 코드
 */
@Getter
public enum BoardErrorCode {
    
    // Board Category 관련 (BOARD_CATEGORY_0XX)
    BOARD_CATEGORY_NOT_FOUND("BOARD_CATEGORY_001", "게시판 카테고리를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    BOARD_CATEGORY_ALREADY_EXISTS("BOARD_CATEGORY_002", "이미 존재하는 게시판 카테고리입니다", HttpStatus.CONFLICT),
    
    // Post 관련 (POST_0XX)
    POST_NOT_FOUND("POST_001", "게시글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    POST_ALREADY_DELETED("POST_002", "이미 삭제된 게시글입니다", HttpStatus.BAD_REQUEST),
    POST_ATTACHMENT_NOT_ALLOWED("POST_003", "해당 게시판은 첨부파일을 허용하지 않습니다", HttpStatus.BAD_REQUEST),
    POST_ANONYMOUS_NOT_ALLOWED("POST_004", "해당 게시판은 익명을 허용하지 않습니다", HttpStatus.BAD_REQUEST),
    
    // Comment 관련 (COMMENT_0XX)
    COMMENT_NOT_FOUND("COMMENT_001", "댓글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    COMMENT_ALREADY_DELETED("COMMENT_002", "이미 삭제된 댓글입니다", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_ALLOWED("COMMENT_003", "해당 게시판은 댓글을 허용하지 않습니다", HttpStatus.BAD_REQUEST),
    COMMENT_DEPTH_EXCEEDED("COMMENT_004", "댓글 깊이 제한을 초과했습니다", HttpStatus.BAD_REQUEST),
    PARENT_COMMENT_NOT_FOUND("COMMENT_005", "부모 댓글을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    
    // Attachment 관련 (ATTACHMENT_0XX)
    ATTACHMENT_NOT_FOUND("ATTACHMENT_001", "첨부파일을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ATTACHMENT_UPLOAD_FAILED("ATTACHMENT_002", "첨부파일 업로드에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ATTACHMENT_SIZE_EXCEEDED("ATTACHMENT_003", "첨부파일 크기가 제한을 초과했습니다", HttpStatus.BAD_REQUEST),
    ATTACHMENT_TYPE_NOT_ALLOWED("ATTACHMENT_004", "허용되지 않는 파일 형식입니다", HttpStatus.BAD_REQUEST),
    
    // File 관련 (FILE_0XX)
    FILE_UPLOAD_FAILED("FILE_001", "파일 업로드에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("FILE_002", "파일 크기가 제한을 초과했습니다", HttpStatus.BAD_REQUEST),
    INVALID_FILE_NAME("FILE_003", "유효하지 않은 파일명입니다", HttpStatus.BAD_REQUEST),
    INVALID_FILE_EXTENSION("FILE_004", "유효하지 않은 파일 확장자입니다", HttpStatus.BAD_REQUEST),
    ;
    
    private final String code;
    private final String message;
    private final HttpStatus status;
    
    BoardErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
