package com.mzc.backend.lms.domains.user.profile.service;

import com.mzc.backend.lms.domains.user.profile.dto.UserBasicInfoDto;

import java.util.Map;
import java.util.Set;

/**
 * 유저 정보 캐시 서비스
 * Redis Cache-Aside 패턴으로 유저 기본 정보 조회
 */
public interface UserInfoCacheService {
	
	/**
	 * 유저 ID Set을 받아 복호화된 유저 정보 Map 반환
	 *
	 * @param userIds 조회할 유저 ID Set
	 * @return userId를 key로 하는 UserBasicInfoDto Map
	 */
	Map<Long, UserBasicInfoDto> getUserInfoMap(Set<Long> userIds);
}
