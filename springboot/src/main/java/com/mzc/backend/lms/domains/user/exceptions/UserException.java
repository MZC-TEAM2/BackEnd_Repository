package com.mzc.backend.lms.domains.user.exceptions;

import com.mzc.backend.lms.common.exceptions.CommonException;
import com.mzc.backend.lms.common.exceptions.CommonErrorCode;
import lombok.Getter;

/**
 * User 도메인 예외 클래스
 */
@Getter
public class UserException extends CommonException {

    private final UserErrorCode userErrorCode;

    public UserException(UserErrorCode userErrorCode) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR, userErrorCode.getMessage());
        this.userErrorCode = userErrorCode;
    }

    public UserException(UserErrorCode userErrorCode, String detailMessage) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR,
              String.format("%s - %s", userErrorCode.getMessage(), detailMessage));
        this.userErrorCode = userErrorCode;
    }

    public UserException(UserErrorCode userErrorCode, String detailMessage, Throwable cause) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR,
              String.format("%s - %s", userErrorCode.getMessage(), detailMessage), cause);
        this.userErrorCode = userErrorCode;
    }

    @Override
    public String getExceptionType() {
        return "USER_DOMAIN";
    }

    /**
     * 학생을 찾을 수 없을 때 발생하는 예외 생성
     */
    public static UserException studentNotFound(String studentNumber) {
        return new UserException(UserErrorCode.STUDENT_NOT_FOUND,
            String.format("학번: %s", studentNumber));
    }

    /**
     * 교수를 찾을 수 없을 때 발생하는 예외 생성
     */
    public static UserException professorNotFound(String professorNumber) {
        return new UserException(UserErrorCode.PROFESSOR_NOT_FOUND,
            String.format("교번: %s", professorNumber));
    }

    /**
     * User ID로 학생을 찾을 수 없을 때 발생하는 예외 생성
     */
    public static UserException studentNotFoundByUserId(Long userId) {
        return new UserException(UserErrorCode.STUDENT_NOT_FOUND,
            String.format("User ID: %d", userId));
    }

    /**
     * User ID로 교수를 찾을 수 없을 때 발생하는 예외 생성
     */
    public static UserException professorNotFoundByUserId(Long userId) {
        return new UserException(UserErrorCode.PROFESSOR_NOT_FOUND,
            String.format("User ID: %d", userId));
    }

    /**
     * 사용자 프로필을 찾을 수 없을 때 발생하는 예외 생성
     */
    public static UserException userProfileNotFound(Long userId) {
        return new UserException(UserErrorCode.USER_PROFILE_NOT_FOUND,
            String.format("User ID: %d", userId));
    }

    /**
     * 암호화 실패 예외 생성
     */
    public static UserException encryptionFailed(String fieldName, Throwable cause) {
        return new UserException(UserErrorCode.ENCRYPTION_FAILED,
            String.format("Field: %s", fieldName), cause);
    }

    /**
     * 복호화 실패 예외 생성
     */
    public static UserException decryptionFailed(String fieldName, Throwable cause) {
        return new UserException(UserErrorCode.DECRYPTION_FAILED,
            String.format("Field: %s", fieldName), cause);
    }
}