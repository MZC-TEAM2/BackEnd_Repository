package com.mzc.backend.lms.common.exceptions.application;


import com.mzc.backend.lms.common.exceptions.CommonErrorCode;
import com.mzc.backend.lms.common.exceptions.CommonException;

/**
 * 요청 데이터가 유효하지 않을 때 발생하는 예외
 * HTTP 400 Bad Request
 */
public class InvalidRequestException extends CommonException {
	
	public InvalidRequestException() {
		super(CommonErrorCode.INVALID_INPUT);
	}
	
	public InvalidRequestException(CommonErrorCode commonErrorCode) {
		super(commonErrorCode);
	}
	
	public InvalidRequestException(CommonErrorCode commonErrorCode, String message) {
		super(commonErrorCode, message);
	}
	
	public static InvalidRequestException invalidFormat(String fieldName) {
		return new InvalidRequestException(
				CommonErrorCode.INVALID_FORMAT,
				"잘못된 형식입니다: " + fieldName
		);
	}
	
	public static InvalidRequestException requiredFieldMissing(String fieldName) {
		return new InvalidRequestException(
				CommonErrorCode.REQUIRED_FIELD_MISSING,
				"필수 필드가 누락되었습니다: " + fieldName
		);
	}
	
	public static InvalidRequestException valueOutOfRange(String fieldName, String range) {
		return new InvalidRequestException(
				CommonErrorCode.VALUE_OUT_OF_RANGE,
				fieldName + "의 값이 허용 범위를 벗어났습니다: " + range
		);
	}
	
	@Override
	public String getExceptionType() {
		return "APPLICATION";
	}
}
