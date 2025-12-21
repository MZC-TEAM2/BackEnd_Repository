package com.mzc.backend.lms.domains.user.search.dto;

import com.mzc.backend.lms.domains.user.auth.encryption.annotation.Encrypted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 유저 탐색 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class UserSearchResponseDto {

    private Long userId;

    @Encrypted
    private String name;

    private String email;

    private String departmentName;

    private String collegeName;

    private String userType;

    private String thumbnailUrl;

    public static UserSearchResponseDto of(
            Long userId,
            String name,
            String email,
            String departmentName,
            String collegeName,
            String userType,
            String thumbnailUrl
    ) {
        return UserSearchResponseDto.builder()
                .userId(userId)
                .name(name)
                .email(email)
                .departmentName(departmentName)
                .collegeName(collegeName)
                .userType(userType)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
