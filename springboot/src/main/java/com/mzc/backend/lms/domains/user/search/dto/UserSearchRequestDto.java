package com.mzc.backend.lms.domains.user.search.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 유저 탐색 요청 DTO (커서 기반 페이징)
 */
@Getter
@Setter
public class UserSearchRequestDto {

    private Long collegeId;

    private Long departmentId;

    private String name;

    private UserType userType;

    private Long cursorId;

    private String cursorName;

    private Integer size = 20;

    private SortBy sortBy = SortBy.ID;

    public enum UserType {
        STUDENT,
        PROFESSOR
    }

    public enum SortBy {
        ID,
        NAME
    }
}
