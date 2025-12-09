package com.mzc.backend.lms.domains.user.profile.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 프로필 수정 요청 DTO
 */
@Getter
@Builder
public class ProfileUpdateRequestDto {

    private final String name;
    private final String mobileNumber;
    private final String homeNumber;
    private final String officeNumber;
}