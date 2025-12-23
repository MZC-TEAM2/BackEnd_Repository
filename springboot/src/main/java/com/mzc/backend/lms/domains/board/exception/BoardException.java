package com.mzc.backend.lms.domains.board.exception;

import lombok.Getter;

/**
 * Board 도메인 예외
 */
@Getter
public class BoardException extends RuntimeException {
	
	private final BoardErrorCode errorCode;
	
	public BoardException(BoardErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
	
	public BoardException(BoardErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public BoardException(BoardErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
}
