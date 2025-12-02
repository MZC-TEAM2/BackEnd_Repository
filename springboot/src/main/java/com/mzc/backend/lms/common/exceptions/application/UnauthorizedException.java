package com.mzc.backend.lms.common.exceptions.application;


import com.mzc.backend.lms.common.exceptions.CommonErrorCode;
import com.mzc.backend.lms.common.exceptions.CommonException;

/**
 * 인증되지 않은 요청일 때 발생하는 예외
 * HTTP 401 Unauthorized
 */
public class UnauthorizedException extends CommonException {

	public UnauthorizedException() {
		super(CommonErrorCode.UNAUTHORIZED);
	}

	public UnauthorizedException(String message) {
		super(CommonErrorCode.UNAUTHORIZED, message);
	}

	@Override
	public String getExceptionType() {
		return "APPLICATION";
	}

	public static UnauthorizedException tokenExpired() {
		return new UnauthorizedException("인증 토큰이 만료되었습니다.");
	}

	public static UnauthorizedException tokenInvalid() {
		return new UnauthorizedException("유효하지 않은 인증 토큰입니다.");
	}

	public static UnauthorizedException tokenMissing() {
		return new UnauthorizedException("인증 토큰이 필요합니다.");
	}
}
