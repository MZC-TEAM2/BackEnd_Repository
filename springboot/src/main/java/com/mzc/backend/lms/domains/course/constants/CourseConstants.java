package com.mzc.backend.lms.domains.course.constants;

import java.time.DayOfWeek;
import java.util.Map;

/**
 * 강의 관련 상수 클래스
 */
public final class CourseConstants {
	
	/**
	 * 강의 유형 코드 맵 (typeCode -> 코드)
	 * 1: MAJOR_REQ, 2: MAJOR_ELEC, 3: GEN_REQ, 4: GEN_ELEC
	 * 데이터베이스 스키마와 일치 (V3__init_course_dummy_data.sql 참고)
	 */
	public static final Map<Integer, String> COURSE_TYPE_CODE_MAP = Map.of(
			1, "MAJOR_REQ",
			2, "MAJOR_ELEC",
			3, "GEN_REQ",
			4, "GEN_ELEC"
	);
	/**
	 * 강의 유형 이름 맵 (typeCode -> 이름)
	 * 1: 전공필수, 2: 전공선택, 3: 교양필수, 4: 교양선택
	 */
	public static final Map<Integer, String> COURSE_TYPE_NAME_MAP = Map.of(
			1, "전공필수",
			2, "전공선택",
			3, "교양필수",
			4, "교양선택"
	);
	/**
	 * 요일 이름 맵 (DayOfWeek -> 한글 이름)
	 */
	public static final Map<DayOfWeek, String> DAY_NAME_MAP = Map.of(
			DayOfWeek.MONDAY, "월",
			DayOfWeek.TUESDAY, "화",
			DayOfWeek.WEDNESDAY, "수",
			DayOfWeek.THURSDAY, "목",
			DayOfWeek.FRIDAY, "금"
	);
	/**
	 * 강의 유형 색상 맵 (코드 -> 색상)
	 */
	public static final Map<String, String> COURSE_TYPE_COLOR_MAP = Map.of(
			"MAJOR_REQ", "#FFB4C8",
			"MAJOR_ELEC", "#FFD4E5",
			"GEN_REQ", "#B4E5FF",
			"GEN_ELEC", "#D4F0FF"
	);
	
	private CourseConstants() {
		// 인스턴스화 방지
	}
	
	/**
	 * 강의 유형 코드로 색상 조회
	 */
	public static String getCourseTypeColor(String code) {
		return COURSE_TYPE_COLOR_MAP.getOrDefault(code, "#CCCCCC");
	}
}
