package com.mzc.backend.lms.domains.course.course.service;

import com.mzc.backend.lms.domains.course.course.dto.*;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.course.course.entity.CourseWeek;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository;
import com.mzc.backend.lms.domains.course.subject.entity.Subject;
import com.mzc.backend.lms.domains.course.subject.entity.SubjectPrerequisites;
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository;
import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.views.UserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserViewService userViewService;
    private final CourseWeekRepository courseWeekRepository;
    private final SubjectPrerequisitesRepository subjectPrerequisitesRepository;

    private static final Map<Integer, String> COURSE_TYPE_CODE_MAP = Map.of(
            1, "MAJOR_REQ",  // 0 -> 1로 변경
            2, "MAJOR_ELEC", // 1 -> 2로 변경
            3, "GEN_REQ",    // 2 -> 3으로 변경
            4, "GEN_ELEC"    // 3 -> 4로 변경
    );

    private static final Map<Integer, String> COURSE_TYPE_NAME_MAP = Map.of(
            1, "전공필수",  // 0 -> 1로 변경
            2, "전공선택",  // 1 -> 2로 변경
            3, "교양필수",  // 2 -> 3으로 변경
            4, "교양선택"   // 3 -> 4로 변경
    );

    // DAY_NAME_MAP을 DayOfWeek를 키로 사용하도록 변경
    private static final Map<DayOfWeek, String> DAY_NAME_MAP = Map.of(
            DayOfWeek.MONDAY, "월",
            DayOfWeek.TUESDAY, "화",
            DayOfWeek.WEDNESDAY, "수",
            DayOfWeek.THURSDAY, "목",
            DayOfWeek.FRIDAY, "금"
    );

    public CourseResponseDto searchCourses(CourseSearchRequestDto request) {
        // 페이징 설정
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        
        // 정렬 설정
        Sort sort = parseSort(request.getSort());

        // 필터링된 강의 조회
        List<Course> courses = filterCourses(request);
        
        // 정렬 적용
        courses = sortCourses(courses, sort);
        
        // 페이징 적용
        int start = page * size;
        int end = Math.min(start + size, courses.size());
        List<Course> pagedCourses = start < courses.size() 
                ? courses.subList(start, end) 
                : new ArrayList<>();

        // DTO 변환
        List<CourseDto> content = pagedCourses.stream()
                .map(this::convertToCourseDto)
                .collect(Collectors.toList());

        return CourseResponseDto.builder()
                .content(content)
                .totalElements(courses.size())
                .totalPages((int) Math.ceil((double) courses.size() / size))
                .currentPage(page)
                .size(size)
                .build();
    }

    private List<Course> filterCourses(CourseSearchRequestDto request) {
        // termId 필수 체크
        if (request.getTermId() == null) {
            throw new IllegalArgumentException("termId는 필수입니다.");
        }

        if(courseRepository.findByAcademicTermId(request.getTermId()).isEmpty()) {
            throw new IllegalArgumentException("해당 학기의 강의가 없습니다.");
        }

        // 학기로 필터링
        List<Course> courses = courseRepository.findByAcademicTermId(request.getTermId());
        log.info("termId={}로 조회된 강의 수: {}", request.getTermId(), courses.size());

        if(request.getDepartmentId() != null && courseRepository.findBySubjectDepartmentId(request.getDepartmentId()).isEmpty()) {
            throw new IllegalArgumentException("해당 학과는 존재하지 않습니다.");
        }

        // 학과 필터
        if (request.getDepartmentId() != null) {
            int beforeSize = courses.size();
            courses = courses.stream()
                    .filter(c -> c.getSubject().getDepartment().getId().equals(request.getDepartmentId()))
                    .collect(Collectors.toList());
            log.info("departmentId={} 필터 후: {} -> {}", request.getDepartmentId(), beforeSize, courses.size());
        }

        // 이수구분 필터
        if (request.getCourseType() != null) {
            int beforeSize = courses.size();
            int typeCode = request.getCourseType();
            courses = courses.stream()
                    .filter(c -> c.getSubject().getCourseType().getTypeCode() == typeCode)
                    .collect(Collectors.toList());
            log.info("courseType={} 필터 후: {} -> {}", typeCode, beforeSize, courses.size());
        }

        // 학점 필터
        if (request.getCredits() != null) {
            int beforeSize = courses.size();
            courses = courses.stream()
                    .filter(c -> c.getSubject().getCredits().equals(request.getCredits()))
                    .collect(Collectors.toList());
            log.info("credits={} 필터 후: {} -> {}", request.getCredits(), beforeSize, courses.size());
        }

        // 키워드 검색 (과목명, 과목코드, 교수명)
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            int beforeSize = courses.size();
            String keyword = request.getKeyword().trim();
            log.info("키워드 검색 시작: keyword='{}', 필터 전 강의 수: {}", keyword, beforeSize);
            
            courses = courses.stream()
                    .filter(c -> {
                        String subjectName = c.getSubject().getSubjectName();
                        String subjectCode = c.getSubject().getSubjectCode();
                        
                        boolean matchesSubjectName = subjectName != null && 
                                subjectName.toLowerCase().contains(keyword.toLowerCase());
                        boolean matchesSubjectCode = subjectCode != null && 
                                subjectCode.toLowerCase().contains(keyword.toLowerCase());
                        
                        String professorName = userViewService.getUserName(
                                c.getProfessor().getProfessorId().toString()
                        );
                        boolean matchesProfessor = professorName != null && 
                                professorName.toLowerCase().contains(keyword.toLowerCase());
                        
                        boolean matches = matchesSubjectName || matchesSubjectCode || matchesProfessor;
                        if (matches) {
                            log.info("매칭된 강의: subjectName={}, subjectCode={}, professorName={}", 
                                    subjectName, subjectCode, professorName);
                        }
                        return matches;
                    })
                    .collect(Collectors.toList());
            
            log.info("키워드 검색 후: {} -> {}", beforeSize, courses.size());
        }

        log.info("최종 필터링된 강의 수: {}", courses.size());
        return courses;
    }

    private CourseDto convertToCourseDto(Course course) {
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

        // 수강 정원 정보 (수강신청 상태 제외)
        EnrollmentInfoDto enrollmentInfo = EnrollmentInfoDto.builder()
                .current(course.getCurrentStudents())
                .max(course.getMaxStudents())
                .isFull(course.getCurrentStudents() >= course.getMaxStudents())
                .availableSeats(course.getMaxStudents() - course.getCurrentStudents())
                .build();

        return CourseDto.builder()
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
                .enrollment(enrollmentInfo)
                .build();
    }

    private ScheduleDto convertToScheduleDto(CourseSchedule schedule) {
        DayOfWeek dayOfWeek = schedule.getDayOfWeek();
        return ScheduleDto.builder()
                .dayOfWeek(dayOfWeek.getValue()) // DayOfWeek를 int로 변환 (MONDAY=1, TUESDAY=2, ...)
                .dayName(DAY_NAME_MAP.get(dayOfWeek))
                .startTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .endTime(schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
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
        String code = COURSE_TYPE_CODE_MAP.get(courseType.getTypeCode());
        String name = COURSE_TYPE_NAME_MAP.get(courseType.getTypeCode());
        String color = getCourseTypeColor(code);

        return CourseTypeDto.builder()
                .code(code)
                .name(name)
                .color(color)
                .build();
    }

    private String getCourseTypeColor(String code) {
        return switch (code) {
            case "MAJOR_REQ" -> "#FFB4C8";
            case "MAJOR_ELEC" -> "#FFD4E5";
            case "GEN_REQ" -> "#B4E5FF";
            case "GEN_ELEC" -> "#D4F0FF";
            default -> "#CCCCCC";
        };
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

        String mappedProperty = switch (property) {
            case "courseCode" -> "subject.subjectCode";
            case "courseName" -> "subject.subjectName";
            case "credits" -> "subject.credits";
            default -> "subject.subjectCode";
        };

        return Sort.by(direction, mappedProperty);
    }

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
     * 강의 상세 정보 조회
     */
    public CourseDetailDto getCourseDetailById(Long courseId) {
        if(courseId == null) {
            throw new IllegalArgumentException("강의 ID는 필수입니다.");
        }
        // 강의 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. courseId: " + courseId));
        
        log.info("강의 상세 조회: courseId={}, courseName={}", 
                courseId, course.getSubject().getSubjectName());
        
        // 기본 CourseDto 정보 생성
        CourseDto baseDto = convertToCourseDto(course);
        Subject subject = course.getSubject();
        AcademicTerm academicTerm = course.getAcademicTerm();
        
        // 주차별 강의 계획 조회
        List<CourseWeek> courseWeeks = courseWeekRepository.findByCourseId(courseId);
        List<WeekDto> weeks = courseWeeks.stream()
                .sorted(Comparator.comparing(CourseWeek::getWeekNumber))
                .map(w -> WeekDto.builder()
                        .id(w.getId())
                        .weekNumber(w.getWeekNumber())
                        .weekTitle(w.getWeekTitle())
                        .build())
                .collect(Collectors.toList());
        
        // 선수과목 정보 조회
        List<SubjectPrerequisites> prerequisites = subjectPrerequisitesRepository.findBySubjectId(subject.getId());
        List<PrerequisiteDto> prerequisiteDtos = prerequisites.stream()
                .map(p -> PrerequisiteDto.builder()
                        .subjectCode(p.getPrerequisite().getSubjectCode())
                        .subjectName(p.getPrerequisite().getSubjectName())
                        .isMandatory(p.getIsMandatory())
                        .build())
                .collect(Collectors.toList());
        
        // 학기 정보 DTO 생성
        AcademicTermDto academicTermDto = AcademicTermDto.builder()
                .id(academicTerm.getId())
                .year(academicTerm.getYear())
                .termType(academicTerm.getTermType())
                .startDate(academicTerm.getStartDate())
                .endDate(academicTerm.getEndDate())
                .build();
        
        // CourseDetailDto 생성
        return CourseDetailDto.builder()
                // 기본 강의 정보
                .id(baseDto.getId())
                .courseCode(baseDto.getCourseCode())
                .courseName(baseDto.getCourseName())
                .section(baseDto.getSection())
                .professor(baseDto.getProfessor())
                .department(baseDto.getDepartment())
                .credits(baseDto.getCredits())
                .courseType(baseDto.getCourseType())
                .schedule(baseDto.getSchedule())
                .scheduleText(baseDto.getScheduleText())
                .enrollment(baseDto.getEnrollment())
                // Subject 상세 정보
                .subjectDescription(subject.getSubjectDescription())
                .description(subject.getDescription())
                .theoryHours(subject.getTheoryHours())
                .practiceHours(subject.getPracticeHours())
                // 주차별 강의 계획
                .weeks(weeks)
                // 선수과목 정보
                .prerequisites(prerequisiteDtos)
                // 학기 정보
                .academicTerm(academicTermDto)
                .build();
    }
}
