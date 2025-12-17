package com.mzc.backend.lms.domains.course.course.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.AcademicTermRepository;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.course.course.dto.*;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseTypeRepository;
import com.mzc.backend.lms.domains.course.subject.entity.Subject;
import com.mzc.backend.lms.domains.course.subject.repository.SubjectRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorRepository;
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 교수 강의 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProfessorCourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicTermRepository academicTermRepository;
    private final ProfessorRepository professorRepository;
    private final ProfessorDepartmentRepository professorDepartmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentPeriodRepository enrollmentPeriodRepository;
    private final CourseService courseService;
    private final CourseTypeRepository courseTypeRepository;

    /**
     * 강의 개설 (API 명세서 기준)
     * 방법 A: subjectId로 기존 과목 선택
     * 방법 B: subject 객체로 새 과목 생성
     */
    public CreateCourseResponseDto createCourse(CreateCourseRequestDto request, Long professorId) {
        log.info("강의 개설 요청: professorId={}, subjectId={}, section={}",
                professorId, request.getSubjectId(), request.getSection());

        // 0. subjectId와 subject 둘 중 하나만 제공되었는지 검증
        if ((request.getSubjectId() == null && request.getSubject() == null) ||
            (request.getSubjectId() != null && request.getSubject() != null)) {
            throw new IllegalArgumentException("subjectId와 subject 중 하나만 제공해야 합니다.");
        }

        // 1. EnrollmentPeriod 조회 및 AcademicTerm 가져오기
        EnrollmentPeriod enrollmentPeriod = enrollmentPeriodRepository.findById(request.getEnrollmentPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("수강신청 기간을 찾을 수 없습니다."));
        
        AcademicTerm academicTerm = enrollmentPeriod.getAcademicTerm();
        if (academicTerm == null) {
            throw new IllegalArgumentException("수강신청 기간에 연결된 학기 정보가 없습니다.");
        }

        // 2. 교수 확인 및 주 학과 조회
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("교수를 찾을 수 없습니다."));

        ProfessorDepartment professorDepartment = professorDepartmentRepository
                .findByProfessorId(professorId)
                .filter(pd -> pd.getIsPrimary() != null && pd.getIsPrimary())
                .orElseThrow(() -> new IllegalArgumentException("교수의 주 소속 학과를 찾을 수 없습니다."));
        
        Department department = professorDepartment.getDepartment();
        Long departmentId = department.getId();

        // 3. Subject 처리 (방법 A 또는 방법 B)
        Subject subject;
        boolean isNewSubject = false;
        
        if (request.getSubjectId() != null) {
            // 방법 A: 기존 Subject 사용
            subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("과목을 찾을 수 없습니다: " + request.getSubjectId()));
            log.info("기존 과목 사용: subjectId={}, subjectCode={}", subject.getId(), subject.getSubjectCode());
            
        } else {
            // 방법 B: 새 Subject 생성
            CreateCourseRequestDto.SubjectRequestDto subjectReq = request.getSubject();
            
            // 과목 코드 중복 체크 (같은 학과 내)
            Long targetDeptId = subjectReq.getDepartmentId() != null ? subjectReq.getDepartmentId() : departmentId;
            if (subjectRepository.existsByDepartmentIdAndSubjectCode(targetDeptId, subjectReq.getSubjectCode())) {
                throw new IllegalArgumentException("이미 존재하는 과목 코드입니다: " + subjectReq.getSubjectCode());
            }
            
            // CourseType 조회
            int typeCode = convertCourseTypeToTypeCode(subjectReq.getCourseType());
            CourseType courseType = courseTypeRepository.findByTypeCode(typeCode)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 이수구분입니다: " + subjectReq.getCourseType()));
            
            // Department 조회
            Department subjectDept = subjectReq.getDepartmentId() != null ?
                    professorDepartmentRepository.findByProfessorId(professorId)
                            .map(ProfessorDepartment::getDepartment)
                            .filter(d -> d.getId().equals(subjectReq.getDepartmentId()))
                            .orElseThrow(() -> new IllegalArgumentException("학과를 찾을 수 없습니다: " + subjectReq.getDepartmentId()))
                    : department;
            
            // 새 Subject 생성
            subject = Subject.builder()
                    .subjectCode(subjectReq.getSubjectCode())
                    .subjectName(subjectReq.getSubjectName())
                    .subjectDescription(subjectReq.getDescription() != null ? subjectReq.getDescription() : "")
                    .department(subjectDept)
                    .courseType(courseType)
                    .credits(subjectReq.getCredits())
                    .description(subjectReq.getDescription())
                    .build();
            
            subject = subjectRepository.save(subject);
            isNewSubject = true;
            
            // 선수과목 추가
            if (subjectReq.getPrerequisiteSubjectIds() != null && !subjectReq.getPrerequisiteSubjectIds().isEmpty()) {
                for (Long prereqId : subjectReq.getPrerequisiteSubjectIds()) {
                    Subject prereqSubject = subjectRepository.findById(prereqId)
                            .orElseThrow(() -> new IllegalArgumentException("선수과목을 찾을 수 없습니다: " + prereqId));
                    subject.addPrerequisite(prereqSubject, true);  // 기본적으로 필수
                }
                subjectRepository.save(subject);
            }
            
            log.info("새 과목 생성: subjectId={}, subjectCode={}, subjectName={}", 
                    subject.getId(), subject.getSubjectCode(), subject.getSubjectName());
        }

        // 4. 전공과목인 경우 교수의 학과와 Subject의 학과 일치 검증
        CourseType courseType = subject.getCourseType();
        if (courseType.getCategory() == 0) { // 전공과목
            if (!subject.getDepartment().getId().equals(departmentId)) {
                throw new IllegalArgumentException(
                        String.format("전공과목은 해당 학과에서만 개설할 수 있습니다. (과목 학과: %s, 교수 학과: %s)",
                                subject.getDepartment().getDepartmentName(),
                                department.getDepartmentName()));
            }
        }

        // 5. 중복 체크 (동일 Subject + AcademicTerm + Section)
        if (courseRepository.existsBySubjectIdAndAcademicTermIdAndSectionNumber(
                subject.getId(), academicTerm.getId(), request.getSection())) {
            throw new IllegalArgumentException("이미 동일한 과목, 학기, 분반으로 개설된 강의가 있습니다.");
        }

        // 6. 시간표 충돌 체크
        if (request.getSchedule() != null && !request.getSchedule().isEmpty()) {
            for (ScheduleRequestDto scheduleDto : request.getSchedule()) {
                DayOfWeek dayOfWeek = DayOfWeek.of(scheduleDto.getDayOfWeek());
                LocalTime startTime = LocalTime.parse(scheduleDto.getStartTime());
                LocalTime endTime = LocalTime.parse(scheduleDto.getEndTime());

                if (courseRepository.existsByProfessorAndTimeConflict(
                        professorId, academicTerm.getId(), dayOfWeek, startTime, endTime)) {
                    throw new IllegalArgumentException(
                            String.format("시간표 충돌이 발생했습니다. (%s %s-%s)",
                                    getDayName(dayOfWeek), scheduleDto.getStartTime(), scheduleDto.getEndTime()));
                }
            }
        }

        // 7. Course 생성
        Course course = Course.builder()
                .subject(subject)
                .professor(professor)
                .academicTerm(academicTerm)
                .sectionNumber(request.getSection())
                .maxStudents(request.getMaxStudents())
                .currentStudents(0)
                .description(request.getDescription())  // 분반별 강의 설명
                .build();

        // 8. 스케줄 추가
        if (request.getSchedule() != null && !request.getSchedule().isEmpty()) {
            for (ScheduleRequestDto scheduleDto : request.getSchedule()) {
                DayOfWeek dayOfWeek = DayOfWeek.of(scheduleDto.getDayOfWeek());
                LocalTime startTime = LocalTime.parse(scheduleDto.getStartTime());
                LocalTime endTime = LocalTime.parse(scheduleDto.getEndTime());

                CourseSchedule schedule = CourseSchedule.builder()
                        .dayOfWeek(dayOfWeek)
                        .startTime(startTime)
                        .endTime(endTime)
                        .scheduleRoom(scheduleDto.getClassroom())
                        .build();

                course.addSchedule(schedule);
            }
        }

        Course savedCourse = courseRepository.save(course);

        log.info("강의 개설 완료: courseId={}, courseCode={}, courseName={}", 
                savedCourse.getId(), subject.getSubjectCode(), subject.getSubjectName());

        // 9. 응답 DTO 생성
        List<ScheduleDto> schedules = savedCourse.getSchedules().stream()
                .sorted(Comparator.comparing(CourseSchedule::getDayOfWeek)
                        .thenComparing(CourseSchedule::getStartTime))
                .map(this::convertToScheduleDto)
                .collect(Collectors.toList());

        return CreateCourseResponseDto.builder()
                .id(savedCourse.getId())
                .courseCode(subject.getSubjectCode())
                .courseName(subject.getSubjectName())
                .section(savedCourse.getSectionNumber())
                .credits(subject.getCredits())
                .maxStudents(savedCourse.getMaxStudents())
                .description(savedCourse.getDescription())
                .status("DRAFT")  // API 명세서에 맞춰 기본값 설정
                .subjectId(subject.getId())
                .isNewlyCreated(isNewSubject)
                .academicTermId(academicTerm.getId())
                .schedules(schedules)
                .createdAt(savedCourse.getCreatedAt())
                .build();
    }

    /**
     * courseType 문자열을 typeCode로 변환
     * MAJOR_REQ -> 1, MAJOR_ELEC -> 2, GEN_REQ -> 3, GEN_ELEC -> 4
     */
    private int convertCourseTypeToTypeCode(String courseType) {
        return switch (courseType) {
            case "MAJOR_REQ" -> 1;
            case "MAJOR_ELEC" -> 2;
            case "GEN_REQ" -> 3;
            case "GEN_ELEC" -> 4;
            default -> throw new IllegalArgumentException("유효하지 않은 이수구분입니다: " + courseType);
        };
    }

    /**
     * 강의 수정
     */
    public CreateCourseResponseDto updateCourse(Long courseId, UpdateCourseRequestDto request, Long professorId) {
        log.info("강의 수정 요청: courseId={}, professorId={}", courseId, professorId);

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("강의 수정 권한이 없습니다.");
        }

        // 2. 수강생이 있는 경우 정원 감소 불가
        if (request.getMaxStudents() != null && request.getMaxStudents() < course.getCurrentStudents()) {
            throw new IllegalArgumentException(
                    String.format("현재 수강생 수(%d명)보다 적은 정원(%d명)으로 설정할 수 없습니다.",
                            course.getCurrentStudents(), request.getMaxStudents()));
        }

        // 3. 강의 정보 수정
        if (request.getSectionNumber() != null) {
            // 분반 변경 시 중복 체크
            if (!course.getSectionNumber().equals(request.getSectionNumber())) {
                if (courseRepository.existsBySubjectIdAndAcademicTermIdAndSectionNumber(
                        course.getSubject().getId(), course.getAcademicTerm().getId(), request.getSectionNumber())) {
                    throw new IllegalArgumentException("이미 동일한 분반으로 개설된 강의가 있습니다.");
                }
            }
            course.setSectionNumber(request.getSectionNumber());
        }

        if (request.getMaxStudents() != null) {
            course.setMaxStudents(request.getMaxStudents());
        }

        // 4. 스케줄 수정
        if (request.getSchedules() != null) {
            // 기존 스케줄 삭제
            course.getSchedules().clear();

            // 새 스케줄 추가
            for (ScheduleRequestDto scheduleDto : request.getSchedules()) {
                DayOfWeek dayOfWeek = DayOfWeek.of(scheduleDto.getDayOfWeek());
                LocalTime startTime = LocalTime.parse(scheduleDto.getStartTime());
                LocalTime endTime = LocalTime.parse(scheduleDto.getEndTime());

                // 시간표 충돌 체크 (현재 강의 제외)
                // TODO: 현재 강의를 제외한 충돌 체크 로직 추가 필요

                CourseSchedule schedule = CourseSchedule.builder()
                        .dayOfWeek(dayOfWeek)
                        .startTime(startTime)
                        .endTime(endTime)
                        .scheduleRoom(scheduleDto.getClassroom())
                        .build();

                course.addSchedule(schedule);
            }
        }

        Course updatedCourse = courseRepository.save(course);

        log.info("강의 수정 완료: courseId={}", updatedCourse.getId());

        // 5. 응답 DTO 생성
        List<ScheduleDto> schedules = updatedCourse.getSchedules().stream()
                .sorted(Comparator.comparing(CourseSchedule::getDayOfWeek)
                        .thenComparing(CourseSchedule::getStartTime))
                .map(this::convertToScheduleDto)
                .collect(Collectors.toList());

        return CreateCourseResponseDto.builder()
                .id(updatedCourse.getId())
                .courseCode(updatedCourse.getSubject().getSubjectCode())
                .courseName(updatedCourse.getSubject().getSubjectName())
                .section(updatedCourse.getSectionNumber())
                .credits(updatedCourse.getSubject().getCredits())
                .maxStudents(updatedCourse.getMaxStudents())
                .status("DRAFT")
                .academicTermId(updatedCourse.getAcademicTerm().getId())  // 학기 ID 추가
                .schedules(schedules)
                .createdAt(updatedCourse.getCreatedAt())
                .build();
    }

    /**
     * 강의 취소
     */
    public void cancelCourse(Long courseId, Long professorId) {
        log.info("강의 취소 요청: courseId={}, professorId={}", courseId, professorId);

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("강의 취소 권한이 없습니다.");
        }

        // 2. 수강생 확인
        long enrollmentCount = enrollmentRepository.findByCourseId(courseId).size();
        if (enrollmentCount > 0) {
            throw new IllegalArgumentException(
                    String.format("수강생이 %d명 있어 강의를 취소할 수 없습니다.", enrollmentCount));
        }

        // 3. 강의 삭제
        courseRepository.delete(course);

        log.info("강의 취소 완료: courseId={}", courseId);
    }

    /**
     * 내가 개설한 강의 목록 조회
     */
    @Transactional(readOnly = true)
    public MyCoursesResponseDto getMyCourses(Long professorId, Long academicTermId) {
        log.info("내 강의 목록 조회: professorId={}, academicTermId={}", professorId, academicTermId);

        List<Course> courses;
        AcademicTerm academicTerm = null;

        if (academicTermId != null) {
            academicTerm = academicTermRepository.findById(academicTermId)
                    .orElseThrow(() -> new IllegalArgumentException("학기를 찾을 수 없습니다."));
            courses = courseRepository.findByProfessorProfessorIdAndAcademicTermId(professorId, academicTermId);
        } else {
            courses = courseRepository.findByProfessorProfessorId(professorId);
        }

        // DTO 변환
        List<CourseDto> courseDtos = courses.stream()
                .map(courseService::convertToCourseDto)
                .collect(Collectors.toList());

        AcademicTermDto termDto = null;
        if (academicTerm != null) {
            termDto = AcademicTermDto.builder()
                    .id(academicTerm.getId())
                    .year(academicTerm.getYear())
                    .termType(academicTerm.getTermType())
                    .startDate(academicTerm.getStartDate())
                    .endDate(academicTerm.getEndDate())
                    .build();
        }

        return MyCoursesResponseDto.builder()
                .term(termDto)
                .totalCourses(courses.size())
                .courses(courseDtos)
                .build();
    }

    /**
     * 교수 강의 상세 조회
     */
    @Transactional(readOnly = true)
    public ProfessorCourseDetailDto getCourseDetail(Long courseId, Long professorId) {
        log.info("교수 강의 상세 조회: courseId={}, professorId={}", courseId, professorId);

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("강의 조회 권한이 없습니다.");
        }

        // 2. CourseDto 변환
        CourseDto courseDto = courseService.convertToCourseDto(course);

        // 3. ProfessorCourseDetailDto 생성
        return ProfessorCourseDetailDto.builder()
                .id(course.getId())
                .courseCode(courseDto.getCourseCode())
                .courseName(courseDto.getCourseName())
                .section(courseDto.getSection())
                .department(courseDto.getDepartment())
                .credits(courseDto.getCredits())
                .courseType(courseDto.getCourseType())
                .maxStudents(course.getMaxStudents())
                .currentStudents(course.getCurrentStudents())
                .description(course.getSubject().getDescription())
                .schedule(courseDto.getSchedule())
                .createdAt(course.getCreatedAt())
                .build();
    }

    /**
     * ScheduleDto 변환
     */
    private ScheduleDto convertToScheduleDto(CourseSchedule schedule) {
        DayOfWeek dayOfWeek = schedule.getDayOfWeek();
        return ScheduleDto.builder()
                .dayOfWeek(dayOfWeek.getValue())
                .dayName(getDayName(dayOfWeek))
                .startTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .endTime(schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .classroom(schedule.getScheduleRoom())
                .build();
    }

    /**
     * 강의 등록 기간 활성화 여부 확인
     */
    private boolean isCourseRegistrationPeriodActive() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return enrollmentPeriodRepository.existsActiveCourseRegistrationPeriod(now);
    }

    /**
     * 요일 이름 반환
     */
    private String getDayName(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}

