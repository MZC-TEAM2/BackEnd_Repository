package com.mzc.backend.lms.domains.enrollment.dto;

import com.mzc.backend.lms.domains.user.auth.encryption.annotation.Encrypted;
import lombok.Builder;
import lombok.Getter;

/**
 * 교수 정보 DTO
 */
@Getter
@Builder
public class ProfessorDto {
    private Long id;

    @Encrypted
    private String name;
}
