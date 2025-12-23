package com.mzc.backend.lms.domains.board.enums;

/**
 * 사용자 역할 Enum
 * LMS 시스템의 사용자 권한 체계
 */
public enum UserRole {
	STUDENT("학생", 1),
	TEACHING_ASSISTANT("조교", 2),
	PROFESSOR("교수", 3),
	ADMIN("관리자", 4);
	
	private final String description;
	private final int level;
	
	UserRole(String description, int level) {
		this.description = description;
		this.level = level;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getLevel() {
		return level;
	}
	
	/**
	 * 교육자 역할 여부 확인 (교수, 조교)
	 */
	public boolean isEducator() {
		return this == PROFESSOR || this == TEACHING_ASSISTANT;
	}
	
	/**
	 * 관리 권한 여부 확인 (관리자, 교수)
	 */
	public boolean hasManagementRole() {
		return this == ADMIN || this == PROFESSOR;
	}
	
	/**
	 * 다른 역할보다 높은 권한 레벨인지 확인
	 */
	public boolean hasHigherLevelThan(UserRole otherRole) {
		return this.level > otherRole.level;
	}
}
