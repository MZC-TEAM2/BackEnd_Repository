package com.mzc.backend.lms.domains.user.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 학년 검증 어노테이션
 * 학생인 경우 학년 필수 및 1~4 범위 검증
 * 교수인 경우 학년 무시
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GradeValidator.class)
@Documented
public @interface ValidGrade {
	String message() default "유효하지 않은 학년 정보입니다";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
