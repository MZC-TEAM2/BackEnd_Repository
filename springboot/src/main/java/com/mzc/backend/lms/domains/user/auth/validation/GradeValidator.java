package com.mzc.backend.lms.domains.user.auth.validation;

import com.mzc.backend.lms.domains.user.auth.dto.SignupRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 학년 검증 Validator
 * 학생인 경우만 학년 정보를 검증
 */
public class GradeValidator implements ConstraintValidator<ValidGrade, SignupRequestDto> {

    @Override
    public void initialize(ValidGrade constraintAnnotation) {
        // 초기화 로직 (필요시)
    }

    @Override
    public boolean isValid(SignupRequestDto dto, ConstraintValidatorContext context) {
        // null인 경우 다른 @NotNull 검증에 위임
        if (dto == null || dto.getUserType() == null) {
            return true;
        }

        // 학생인 경우
        if ("STUDENT".equals(dto.getUserType())) {
            // 학년이 null이면 실패
            if (dto.getGrade() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("학생은 학년 정보가 필요합니다")
                    .addConstraintViolation();
                return false;
            }

            // 학년이 1~4 사이가 아니면 실패
            if (dto.getGrade() < 1 || dto.getGrade() > 4) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("학년은 1~4 사이여야 합니다")
                    .addConstraintViolation();
                return false;
            }
        }

        // 교수인 경우 grade는 무시 (null이든 값이 있든 통과)
        // PROFESSOR인 경우 grade 값은 서비스 레이어에서 무시됨

        return true;
    }
}