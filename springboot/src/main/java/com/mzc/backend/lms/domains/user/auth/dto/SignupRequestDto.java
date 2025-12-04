package com.mzc.backend.lms.domains.user.auth.dto;

import com.mzc.backend.lms.domains.user.auth.validation.ValidGrade;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 회원가입 요청 DTO
 */
@Data
@ValidGrade  // 커스텀 학년 검증
public class SignupRequestDto {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 최소 8자, 영문자, 숫자, 특수문자를 포함해야 합니다")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다")
    private String passwordConfirm;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;

    @NotBlank(message = "사용자 타입은 필수입니다")
    @Pattern(regexp = "^(STUDENT|PROFESSOR)$", message = "사용자 타입은 STUDENT 또는 PROFESSOR여야 합니다")
    private String userType;

    @NotNull(message = "대학 정보는 필수입니다")
    private Long collegeId;

    @NotNull(message = "학과 정보는 필수입니다")
    private Long departmentId;

    // 학생인 경우에만 필수 (커스텀 벨리데이터가 처리)
    // 교수인 경우 무시됨
    private Integer grade;  // 학년 (1~4)

    // 교수인 경우
    private String professorNumber;  // 교번 (교수만 입력)

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phoneNumber;

    /**
     * 비밀번호 일치 여부 확인
     */
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }

    /**
     * 학생 여부 확인
     */
    public boolean isStudent() {
        return "STUDENT".equals(userType);
    }

    /**
     * 교수 여부 확인
     */
    public boolean isProfessor() {
        return "PROFESSOR".equals(userType);
    }
}