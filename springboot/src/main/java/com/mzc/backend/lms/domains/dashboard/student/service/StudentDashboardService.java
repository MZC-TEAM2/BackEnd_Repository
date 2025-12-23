package com.mzc.backend.lms.domains.dashboard.student.service;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.dashboard.student.dto.EnrollmentSummaryDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.NoticeDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.PendingAssignmentDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.TodayCourseDto;
import com.mzc.backend.lms.domains.dashboard.student.repository.DashboardQueryRepository;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.profile.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 학생 대시보드 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentDashboardService {
	
	private static final int DEFAULT_PENDING_DAYS = 7;
	private static final int DEFAULT_NOTICE_LIMIT = 5;
	
	private final DashboardQueryRepository dashboardQueryRepository;
	
	/**
	 * 미제출 과제 목록 조회 (기본 7일 이내)
	 *
	 * @param studentId 학생 ID
	 * @return 미제출 과제 목록
	 */
	public List<PendingAssignmentDto> getPendingAssignments(Long studentId) {
		return getPendingAssignments(studentId, DEFAULT_PENDING_DAYS);
	}
	
	/**
	 * 미제출 과제 목록 조회 (기간 지정)
	 *
	 * @param studentId  학생 ID
	 * @param withinDays 마감일 기준 일수
	 * @return 미제출 과제 목록
	 */
	public List<PendingAssignmentDto> getPendingAssignments(Long studentId, int withinDays) {
		return dashboardQueryRepository.findPendingAssignments(studentId, withinDays);
	}
	
	/**
	 * 최신 공지사항 목록 조회 (기본 5개)
	 *
	 * @return 최신 공지사항 목록
	 */
	public List<NoticeDto> getLatestNotices() {
		return getLatestNotices(DEFAULT_NOTICE_LIMIT);
	}
	
	/**
	 * 최신 공지사항 목록 조회 (개수 지정)
	 *
	 * @param limit 조회할 개수
	 * @return 최신 공지사항 목록
	 */
	public List<NoticeDto> getLatestNotices(int limit) {
		return dashboardQueryRepository.findLatestNotices(limit);
	}
	
	/**
	 * 수강 현황 요약 조회
	 *
	 * @param studentId 학생 ID
	 * @return 수강 현황 요약 (과목 수, 총 학점)
	 */
	public EnrollmentSummaryDto getEnrollmentSummary(Long studentId) {
		return dashboardQueryRepository.findEnrollmentSummary(studentId);
	}
	
	/**
	 * 오늘의 강의 목록 조회
	 *
	 * @param studentId 학생 ID
	 * @return 오늘의 강의 목록
	 */
	public List<TodayCourseDto> getTodayCourses(Long studentId) {
		List<Enrollment> enrollments = dashboardQueryRepository.findTodayEnrollments(studentId);
		DayOfWeek today = LocalDate.now().getDayOfWeek();
		
		return enrollments.stream()
				.map(e -> toTodayCourseDto(e, today))
				.sorted(Comparator.comparing(dto ->
						dto.getSchedule().isEmpty() ? "" : dto.getSchedule().get(0).getStartTime()))
				.toList();
	}
	
	private TodayCourseDto toTodayCourseDto(Enrollment enrollment, DayOfWeek today) {
		Course course = enrollment.getCourse();
		Professor professor = course.getProfessor();
		UserProfile professorProfile = professor.getUser().getUserProfile();
		
		// 오늘 요일에 해당하는 스케줄만 필터링
		List<TodayCourseDto.ScheduleDto> todaySchedules = course.getSchedules().stream()
				.filter(s -> s.getDayOfWeek() == today)
				.map(this::toScheduleDto)
				.sorted(Comparator.comparing(TodayCourseDto.ScheduleDto::getStartTime))
				.toList();
		
		return TodayCourseDto.builder()
				.enrollmentId(enrollment.getId())
				.course(TodayCourseDto.CourseInfoDto.builder()
						.id(course.getId())
						.courseCode(course.getSubject().getSubjectCode())
						.courseName(course.getSubject().getSubjectName())
						.section(course.getSectionNumber())
						.credits(course.getSubject().getCredits())
						.courseType(toCourseTypeDto(course))
						.currentStudents(course.getCurrentStudents())
						.maxStudents(course.getMaxStudents())
						.build())
				.professor(TodayCourseDto.ProfessorDto.builder()
						.id(professor.getProfessorId())
						.name(professorProfile != null ? professorProfile.getName() : null)
						.build())
				.schedule(todaySchedules)
				.build();
	}
	
	private TodayCourseDto.ScheduleDto toScheduleDto(CourseSchedule schedule) {
		return TodayCourseDto.ScheduleDto.builder()
				.dayOfWeek(schedule.getDayOfWeek().getValue())
				.dayName(schedule.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN))
				.startTime(schedule.getStartTime().toString())
				.endTime(schedule.getEndTime().toString())
				.classroom(schedule.getScheduleRoom())
				.build();
	}
	
	private TodayCourseDto.CourseTypeDto toCourseTypeDto(Course course) {
		var courseType = course.getSubject().getCourseType();
		if (courseType == null) {
			return null;
		}
		return TodayCourseDto.CourseTypeDto.builder()
				.code(courseType.getTypeCodeString())
				.name(courseType.getTypeName())
				.color(courseType.getColor())
				.build();
	}
}
