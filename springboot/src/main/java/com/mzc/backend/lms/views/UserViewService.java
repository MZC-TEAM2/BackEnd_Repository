package com.mzc.backend.lms.views;

import java.util.List;
import java.util.Map;

/**
 * 사용자 정보 공통 조회 서비스 인터페이스
 *
 * Student, Professor와 관계없이 공통으로 사용되는 사용자 정보 조회 메서드를 제공합니다.
 * 모든 암호화된 필드(이름, 전화번호 등)는 자동으로 복호화되어 반환됩니다.
 */
public interface UserViewService {

    /**
     * User ID로 사용자 타입 확인
     *
     * @param userId User ID
     * @return "STUDENT" 또는 "PROFESSOR", 없으면 null
     */
    String getUserType(Long userId);

    /**
     * User ID로 사용자 이름만 간단히 조회
     * 캐싱 가능한 간단한 조회용
     *
     * @param userId User ID
     * @return 복호화된 이름, 없으면 null
     */
    String getUserName(Long userId);

    /**
     * 여러 User ID로 이름 일괄 조회
     *
     * @param userIds User ID 목록
     * @return User ID를 키로 하는 이름 맵
     */
    Map<Long, String> getUserNames(List<Long> userIds);

    /**
     * User ID로 프로필 이미지 URL 조회
     *
     * @param userId User ID
     * @return 현재 프로필 이미지 URL, 없으면 null
     */
    String getUserProfileImageUrl(Long userId);

    /**
     * User ID 존재 여부 확인
     *
     * @param userId User ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByUserId(Long userId);
}
