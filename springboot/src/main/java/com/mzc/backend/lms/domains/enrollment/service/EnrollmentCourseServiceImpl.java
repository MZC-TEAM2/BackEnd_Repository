package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.course.constants.CourseConstants;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.subject.entity.SubjectPrerequisites;
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository;
import com.mzc.backend.lms.domains.enrollment.dto.*;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import com.mzc.backend.lms.domains.enrollment.repository.CourseCartRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.views.UserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentCourseServiceImpl implements EnrollmentCourseService {
	
	private final CourseRepository courseRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final CourseCartRepository courseCartRepository;
	private final EnrollmentPeriodRepository enrollmentPeriodRepository;
	private final SubjectPrerequisitesRepository subjectPrerequisitesRepository;
	private final UserViewService userViewService;
	
	@Override
	public CourseListResponseDto searchCourses(CourseSearchRequestDto request, String studentId) {
		// 페이징 설정
		int page = request.getPage() != null ? request.getPage() : 0;
		int size = request.getSize() != null ? request.getSize() : 20;
		
		// 정렬 설정
		Sort sort = parseSort(request.getSort());
		
		// 필터링된 강의 조회
		List<Course> courses = filterCourses(request);
		
		// 정렬 적용 (추가!)
		courses = sortCourses(courses, sort);
		
		// 페이징 적용
		int start = page * size;
		int end = Math.min(start + size, courses.size());
		List<Course> pagedCourses = start < courses.size()
				? courses.subList(start, end)
				: new ArrayList<>();
		
		// DTO 변환
		List<CourseItemDto> content = pagedCourses.stream()
				.map(course -> convertToCourseItemDto(course, studentId))
				.collect(Collectors.toList());
		
		return CourseListResponseDto.builder()
				.content(content)
				.totalElements(courses.size())
				.totalPages((int) Math.ceil((double) courses.size() / size))
				.currentPage(page)
				.size(size)
				.build();
	}
	
	private List<Course> filterCourses(CourseSearchRequestDto request) {
		// enrollmentPeriodId 필수 체크
		if (request.getEnrollmentPeriodId() == null) {
			throw new IllegalArgumentException("enrollmentPeriodId는 필수입니다.");
		}
		
		// EnrollmentPeriod 조회
		EnrollmentPeriod enrollmentPeriod = enrollmentPeriodRepository.findById(request.getEnrollmentPeriodId())
				.orElseThrow(() -> new IllegalArgumentException("수강신청 기간을 찾을 수 없습니다: " + request.getEnrollmentPeriodId()));
		
		// EnrollmentPeriod의 AcademicTerm으로 강의 조회
		Long academicTermId = enrollmentPeriod.getAcademicTerm().getId();
		List<Course> courses = courseRepository.findByAcademicTermId(academicTermId);
		
		// 디버깅: 초기 강의 수 확인
		log.debug("enrollmentPeriodId={}, academicTermId={}로 조회된 강의 수: {}",
				request.getEnrollmentPeriodId(), academicTermId, courses.size());
		
		// 학과 필터
		if (request.getDepartmentId() != null) {
			int beforeSize = courses.size();
			courses = courses.stream()
					.filter(c -> c.getSubject().getDepartment().getId().equals(request.getDepartmentId()))
					.collect(Collectors.toList());
			log.debug("departmentId={} 필터 후: {} -> {}", request.getDepartmentId(), beforeSize, courses.size());
		}
		
		// 이수구분 필터
		if (request.getCourseType() != null) {
			int beforeSize = courses.size();
			int typeCode = request.getCourseType();
			courses = courses.stream()
					.filter(c -> c.getSubject().getCourseType().getTypeCode() == typeCode)
					.collect(Collectors.toList());
			log.debug("courseType={} 필터 후: {} -> {}", typeCode, beforeSize, courses.size());
		}
		
		// 학점 필터
		if (request.getCredits() != null) {
			int beforeSize = courses.size();
			courses = courses.stream()
					.filter(c -> c.getSubject().getCredits().equals(request.getCredits()))
					.collect(Collectors.toList());
			log.debug("credits={} 필터 후: {} -> {}", request.getCredits(), beforeSize, courses.size());
		}
		
		// 키워드 검색 (과목명, 과목코드, 교수명)
		if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
			int beforeSize = courses.size();
			String keyword = request.getKeyword().trim();
			log.debug("키워드 검색 시작: keyword='{}', 필터 전 강의 수: {}", keyword, beforeSize);
			
			courses = courses.stream()
					.filter(c -> {
						// 과목명, 과목코드 매칭 (한글은 toLowerCase 불필요하지만 일관성을 위해 유지)
						String subjectName = c.getSubject().getSubjectName();
						String subjectCode = c.getSubject().getSubjectCode();
						
						boolean matchesSubjectName = subjectName != null &&
								subjectName.toLowerCase().contains(keyword.toLowerCase());
						boolean matchesSubjectCode = subjectCode != null &&
								subjectCode.toLowerCase().contains(keyword.toLowerCase());
						
						// 교수명 매칭
						String professorName = userViewService.getUserName(
								c.getProfessor().getProfessorId().toString()
						);
						boolean matchesProfessor = professorName != null &&
								professorName.toLowerCase().contains(keyword.toLowerCase());
						
						boolean matches = matchesSubjectName || matchesSubjectCode || matchesProfessor;
						if (matches) {
							log.debug("매칭된 강의: subjectName={}, subjectCode={}, professorName={}",
									subjectName, subjectCode, professorName);
						}
						return matches;
					})
					.collect(Collectors.toList());
			
			log.debug("키워드 검색 후: {} -> {}", beforeSize, courses.size());
		}
		
		log.debug("최종 필터링된 강의 수: {}", courses.size());
		return courses;
	}
	
	private CourseItemDto convertToCourseItemDto(Course course, String studentId) {
		// 교수 이름 조회
		String professorName = userViewService.getUserName(
				course.getProfessor().getProfessorId().toString()
		);
		
		// 스케줄 변환
		List<ScheduleDto> schedules = course.getSchedules().stream()
				.map(this::convertToScheduleDto)
				.sorted(Comparator.comparing(ScheduleDto::getDayOfWeek)
						.thenComparing(ScheduleDto::getStartTime))
				.collect(Collectors.toList());
		
		// 스케줄 텍스트 생성
		String scheduleText = generateScheduleText(schedules);
		
		// CourseType 변환
		CourseTypeDto courseTypeDto = convertToCourseTypeDto(course.getSubject().getCourseType());
		
		// 수강신청 정보
		EnrollmentDto enrollmentDto = EnrollmentDto.builder()
				.current(course.getCurrentStudents())
				.max(course.getMaxStudents())
				.isFull(course.getCurrentStudents() >= course.getMaxStudents())
				.build();
		
		// 장바구니/수강신청 여부 확인
		boolean isInCart = false;
		boolean isEnrolled = false;
		boolean hasPrerequisites = true; // 선수과목 이수 여부
		
		if (studentId != null) {
			Long studentIdLong = Long.parseLong(studentId);
			isInCart = courseCartRepository.existsByStudentIdAndCourseId(
					studentIdLong, course.getId());
			isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(
					studentIdLong, course.getId());
			
			// 선수과목 이수 여부 확인
			hasPrerequisites = checkPrerequisites(course, studentIdLong);
		}
		
		return CourseItemDto.builder()
				.id(course.getId())
				.courseCode(course.getSubject().getSubjectCode())
				.courseName(course.getSubject().getSubjectName())
				.section(course.getSectionNumber())
				.professor(ProfessorDto.builder()
						.id(course.getProfessor().getProfessorId())
						.name(professorName != null ? professorName : "교수")
						.build())
				.department(DepartmentDto.builder()
						.id(course.getSubject().getDepartment().getId())
						.name(course.getSubject().getDepartment().getDepartmentName())
						.build())
				.credits(course.getSubject().getCredits())
				.courseType(courseTypeDto)
				.schedule(schedules)
				.scheduleText(scheduleText)
				.enrollment(enrollmentDto)
				.isInCart(isInCart)
				.isEnrolled(isEnrolled)
				.canEnroll(!isEnrolled && !enrollmentDto.getIsFull() && hasPrerequisites)
				.warnings(new ArrayList<>())
				.build();
	}
	
	private ScheduleDto convertToScheduleDto(CourseSchedule schedule) {
		DayOfWeek dayOfWeek = schedule.getDayOfWeek();
		LocalTime startTime = schedule.getStartTime();
		LocalTime endTime = schedule.getEndTime();
		return ScheduleDto.builder()
				.dayOfWeek(dayOfWeek.getValue()) // DayOfWeek를 int로 변환
				.dayName(CourseConstants.DAY_NAME_MAP.get(dayOfWeek))
				.startTime(startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
				.endTime(endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
				.classroom(schedule.getScheduleRoom())
				.build();
	}
	
	private String generateScheduleText(List<ScheduleDto> schedules) {
		if (schedules.isEmpty()) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		Map<String, List<ScheduleDto>> byRoom = schedules.stream()
				.collect(Collectors.groupingBy(ScheduleDto::getClassroom));
		
		List<String> parts = new ArrayList<>();
		for (Map.Entry<String, List<ScheduleDto>> entry : byRoom.entrySet()) {
			String room = entry.getKey();
			List<String> timeParts = entry.getValue().stream()
					.map(s -> String.format("%s %s-%s", s.getDayName(),
							s.getStartTime(), s.getEndTime()))
					.collect(Collectors.toList());
			parts.add(String.join(", ", timeParts) + "\n" + room);
		}
		
		return String.join("\n", parts);
	}
	
	private CourseTypeDto convertToCourseTypeDto(CourseType courseType) {
		String code = CourseConstants.COURSE_TYPE_CODE_MAP.get(courseType.getTypeCode());
		String name = CourseConstants.COURSE_TYPE_NAME_MAP.get(courseType.getTypeCode());
		String color = CourseConstants.getCourseTypeColor(code);
		
		return CourseTypeDto.builder()
				.code(code)
				.name(name)
				.color(color)
				.build();
	}
	
	private Sort parseSort(String sort) {
		if (sort == null || sort.isEmpty()) {
			return Sort.by(Sort.Direction.ASC, "subject.subjectCode");
		}
		
		String[] parts = sort.split(",");
		String property = parts[0].trim();
		Sort.Direction direction = parts.length > 1 &&
				parts[1].trim().equalsIgnoreCase("desc")
				? Sort.Direction.DESC
				: Sort.Direction.ASC;
		
		// property 매핑
		String mappedProperty = switch (property) {
			case "courseCode" -> "subject.subjectCode";
			case "courseName" -> "subject.subjectName";
			case "credits" -> "subject.credits";
			default -> "subject.subjectCode";
		};
		
		return Sort.by(direction, mappedProperty);
	}
	
	/**
	 * 강의 리스트 정렬
	 */
	private List<Course> sortCourses(List<Course> courses, Sort sort) {
		if (sort == null || sort.isEmpty()) {
			return courses;
		}
		
		Comparator<Course> comparator = null;
		
		for (Sort.Order order : sort) {
			Comparator<Course> orderComparator = getComparator(order);
			if (comparator == null) {
				comparator = orderComparator;
			} else {
				comparator = comparator.thenComparing(orderComparator);
			}
		}
		
		if (comparator != null) {
			return courses.stream()
					.sorted(comparator)
					.collect(Collectors.toList());
		}
		
		return courses;
	}
	
	/**
	 * Sort.Order에 따른 Comparator 생성
	 */
	private Comparator<Course> getComparator(Sort.Order order) {
		Comparator<Course> comparator = switch (order.getProperty()) {
			case "subject.subjectCode" -> Comparator.comparing(
					c -> c.getSubject().getSubjectCode());
			case "subject.subjectName" -> Comparator.comparing(
					c -> c.getSubject().getSubjectName());
			case "subject.credits" -> Comparator.comparing(
					c -> c.getSubject().getCredits());
			default -> Comparator.comparing(
					c -> c.getSubject().getSubjectCode());
		};
		
		return order.getDirection() == Sort.Direction.DESC
				? comparator.reversed()
				: comparator;
	}
	
	/**
	 * 선수과목 이수 여부 확인
	 */
	private boolean checkPrerequisites(Course course, Long studentId) {
		Long subjectId = course.getSubject().getId();
		List<SubjectPrerequisites> prerequisites = subjectPrerequisitesRepository.findBySubjectId(subjectId);
		
		log.debug("과목 ID: {}, 선수과목 개수: {}", subjectId, prerequisites.size());
		
		if (prerequisites.isEmpty()) {
			log.debug("선수과목이 없으므로 true 반환");
			return true; // 선수과목이 없으면 true
		}
		
		// 학생이 수강신청한 강의 목록 조회
		List<Enrollment> studentEnrollments = enrollmentRepository.findByStudentId(studentId);
		Set<Long> enrolledSubjectIds = studentEnrollments.stream()
				.map(enrollment -> enrollment.getCourse().getSubject().getId())
				.collect(Collectors.toSet());
		
		log.debug("학생 ID: {}, 수강신청한 과목 수: {}, 과목 IDs: {}", studentId, enrolledSubjectIds.size(), enrolledSubjectIds);
		
		// 필수 선수과목이 모두 이수되었는지 확인
		for (SubjectPrerequisites prerequisite : prerequisites) {
			if (prerequisite.getIsMandatory()) {
				Long prerequisiteSubjectId = prerequisite.getPrerequisite().getId();
				log.debug("필수 선수과목 ID: {}, 이수 여부: {}", prerequisiteSubjectId, enrolledSubjectIds.contains(prerequisiteSubjectId));
				if (!enrolledSubjectIds.contains(prerequisiteSubjectId)) {
					log.debug("필수 선수과목을 이수하지 않아 false 반환");
					return false; // 필수 선수과목을 이수하지 않음
				}
			}
		}
		
		log.debug("모든 필수 선수과목을 이수하여 true 반환");
		return true; // 모든 필수 선수과목을 이수함
	}
}
