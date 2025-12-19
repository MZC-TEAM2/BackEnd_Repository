package com.mzc.backend.lms.domains.course.course.service;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.AcademicTermRepository;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.course.course.dto.*;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.course.course.entity.CourseWeek;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseTypeRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository;
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
    private final CourseWeekRepository courseWeekRepository;

    /**
     * 강의 개설 (API 명세서 기준)
     * 방법 A: subjectId로 기존 과목 선택
     * 방법 B: subject 객체로 새 과목 생성
     */
    public CreateCourseResponseDto createCourse(CreateCourseRequestDto request, Long professorId) {
        // 1. 요청 검증
        validateCreateCourseRequest(request, professorId);

        log.info("강의 개설 요청: professorId={}, subjectId={}, section={}",
                professorId, request.getSubjectId(), request.getSection());

        // 2. EnrollmentPeriod 및 AcademicTerm 조회
        EnrollmentPeriod enrollmentPeriod = findEnrollmentPeriod(request.getEnrollmentPeriodId());
        AcademicTerm academicTerm = enrollmentPeriod.getAcademicTerm();
        if (academicTerm == null) {
            throw new IllegalArgumentException("수강신청 기간에 연결된 학기 정보가 없습니다.");
        }

        // 3. 교수 및 학과 조회
        Professor professor = findProfessor(professorId);
        Department department = findProfessorPrimaryDepartment(professorId);
        Long departmentId = department.getId();

        // 4. Subject 처리 (기존 또는 신규)
        SubjectResult subjectResult = findOrCreateSubject(request, professorId, department, departmentId);
        Subject subject = subjectResult.subject();
        boolean isNewSubject = subjectResult.isNew();

        // 5. 전공과목 학과 검증
        validateMajorSubjectDepartment(subject, departmentId, department);

        // 6. 중복 체크
        validateCourseDuplication(subject.getId(), academicTerm.getId(), request.getSection());

        // 7. 시간표 충돌 체크
        validateScheduleConflicts(request.getSchedule(), professorId, academicTerm.getId(), null);

        // 8. Course 생성 및 저장
        Course course = buildCourse(request, subject, professor, academicTerm);
        addSchedulesToCourse(course, request.getSchedule());
        Course savedCourse = courseRepository.save(course);

        // 9. 16주 주차 자동 생성
        createDefaultWeeks(savedCourse);

        log.info("강의 개설 완료: courseId={}, courseCode={}, courseName={}, 16주 주차 자동 생성",
                savedCourse.getId(), subject.getSubjectCode(), subject.getSubjectName());

        // 10. 응답 DTO 생성
        return buildCreateCourseResponse(savedCourse, subject, isNewSubject, academicTerm);
    }

    /**
     * Subject 처리 결과를 담는 레코드
     */
    private record SubjectResult(Subject subject, boolean isNew) {}

    /**
     * 강의 생성 요청 검증
     */
    private void validateCreateCourseRequest(CreateCourseRequestDto request, Long professorId) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보는 필수입니다.");
        }
        if (professorId == null) {
            throw new IllegalArgumentException("교수 ID는 필수입니다.");
        }
        if ((request.getSubjectId() == null && request.getSubject() == null) ||
            (request.getSubjectId() != null && request.getSubject() != null)) {
            throw new IllegalArgumentException("subjectId와 subject 중 하나만 제공해야 합니다.");
        }
    }

    /**
     * EnrollmentPeriod 조회
     */
    private EnrollmentPeriod findEnrollmentPeriod(Long enrollmentPeriodId) {
        if (enrollmentPeriodId == null) {
            throw new IllegalArgumentException("수강신청 기간 ID는 필수입니다.");
        }
        return enrollmentPeriodRepository.findById(enrollmentPeriodId)
                .orElseThrow(() -> new IllegalArgumentException("수강신청 기간을 찾을 수 없습니다."));
    }

    /**
     * 교수 조회
     */
    private Professor findProfessor(Long professorId) {
        if (professorId == null) {
            throw new IllegalArgumentException("교수 ID는 필수입니다.");
        }
        return professorRepository.findById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("교수를 찾을 수 없습니다."));
    }

    /**
     * 교수의 주 소속 학과 조회
     */
    private Department findProfessorPrimaryDepartment(Long professorId) {
        ProfessorDepartment professorDepartment = professorDepartmentRepository
                .findByProfessorId(professorId)
                .filter(pd -> pd.getIsPrimary() != null && pd.getIsPrimary())
                .orElseThrow(() -> new IllegalArgumentException("교수의 주 소속 학과를 찾을 수 없습니다."));
        return professorDepartment.getDepartment();
    }

    /**
     * Subject 조회 또는 생성
     */
    private SubjectResult findOrCreateSubject(CreateCourseRequestDto request, Long professorId,
                                              Department department, Long departmentId) {
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("과목을 찾을 수 없습니다: " + request.getSubjectId()));
            log.info("기존 과목 사용: subjectId={}, subjectCode={}", subject.getId(), subject.getSubjectCode());
            return new SubjectResult(subject, false);
        } else {
            return new SubjectResult(createNewSubject(request.getSubject(), professorId, department, departmentId), true);
        }
    }

    /**
     * 새 Subject 생성
     */
    private Subject createNewSubject(CreateCourseRequestDto.SubjectRequestDto subjectReq, Long professorId,
                                     Department department, Long departmentId) {
        if (subjectReq == null) {
            throw new IllegalArgumentException("과목 정보는 필수입니다.");
        }
        // 과목 코드 중복 체크
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
        Subject subject = Subject.builder()
                    .subjectCode(subjectReq.getSubjectCode())
                    .subjectName(subjectReq.getSubjectName())
                    .subjectDescription(subjectReq.getDescription() != null ? subjectReq.getDescription() : "")
                    .department(subjectDept)
                    .courseType(courseType)
                    .credits(subjectReq.getCredits())
                    .description(subjectReq.getDescription())
                    .build();
            
            subject = subjectRepository.save(subject);
            
            // 선수과목 추가
            if (subjectReq.getPrerequisiteSubjectIds() != null && !subjectReq.getPrerequisiteSubjectIds().isEmpty()) {
            addPrerequisites(subject, subjectReq.getPrerequisiteSubjectIds());
            }
            
            log.info("새 과목 생성: subjectId={}, subjectCode={}, subjectName={}", 
                    subject.getId(), subject.getSubjectCode(), subject.getSubjectName());
        return subject;
    }

    /**
     * 선수과목 추가
     */
    private void addPrerequisites(Subject subject, List<Long> prerequisiteIds) {
        for (Long prereqId : prerequisiteIds) {
            if (prereqId == null) {
                continue;
            }
            Subject prereqSubject = subjectRepository.findById(prereqId)
                    .orElseThrow(() -> new IllegalArgumentException("선수과목을 찾을 수 없습니다: " + prereqId));
            subject.addPrerequisite(prereqSubject, true);
        }
        subjectRepository.save(subject);
    }

    /**
     * 전공과목 학과 검증
     */
    private void validateMajorSubjectDepartment(Subject subject, Long departmentId, Department department) {
        CourseType courseType = subject.getCourseType();
        if (courseType.getCategory() == 0) { // 전공과목
            if (!subject.getDepartment().getId().equals(departmentId)) {
                throw new IllegalArgumentException(
                        String.format("전공과목은 해당 학과에서만 개설할 수 있습니다. (과목 학과: %s, 교수 학과: %s)",
                                subject.getDepartment().getDepartmentName(),
                                department.getDepartmentName()));
            }
        }
    }

    /**
     * 강의 중복 체크
     */
    private void validateCourseDuplication(Long subjectId, Long academicTermId, String section) {
        if (courseRepository.existsBySubjectIdAndAcademicTermIdAndSectionNumber(subjectId, academicTermId, section)) {
            throw new IllegalArgumentException("이미 동일한 과목, 학기, 분반으로 개설된 강의가 있습니다.");
        }
    }

    /**
     * 시간표 충돌 체크
     */
    private void validateScheduleConflicts(List<ScheduleRequestDto> schedules, Long professorId,
                                           Long academicTermId, Long excludeCourseId) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        for (ScheduleRequestDto scheduleDto : schedules) {
                DayOfWeek dayOfWeek = DayOfWeek.of(scheduleDto.getDayOfWeek());
                LocalTime startTime = LocalTime.parse(scheduleDto.getStartTime());
                LocalTime endTime = LocalTime.parse(scheduleDto.getEndTime());

            boolean hasConflict = excludeCourseId == null ?
                    courseRepository.existsByProfessorAndTimeConflict(professorId, academicTermId, dayOfWeek, startTime, endTime) :
                    courseRepository.existsByProfessorAndTimeConflictExcludingCourse(professorId, academicTermId, excludeCourseId, dayOfWeek, startTime, endTime);

            if (hasConflict) {
                    throw new IllegalArgumentException(
                            String.format("시간표 충돌이 발생했습니다. (%s %s-%s)",
                                    getDayName(dayOfWeek), scheduleDto.getStartTime(), scheduleDto.getEndTime()));
                }
            }
        }

    /**
     * Course 엔티티 생성
     */
    private Course buildCourse(CreateCourseRequestDto request, Subject subject, Professor professor, AcademicTerm academicTerm) {
        return Course.builder()
                .subject(subject)
                .professor(professor)
                .academicTerm(academicTerm)
                .sectionNumber(request.getSection())
                .maxStudents(request.getMaxStudents())
                .currentStudents(0)
                .description(request.getDescription())
                .build();
    }

    /**
     * 스케줄을 Course에 추가
     */
    private void addSchedulesToCourse(Course course, List<ScheduleRequestDto> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        for (ScheduleRequestDto scheduleDto : schedules) {
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

    /**
     * 기본 16주 주차 생성
     */
    private void createDefaultWeeks(Course course) {
        for (int weekNumber = 1; weekNumber <= 16; weekNumber++) {
            CourseWeek week = CourseWeek.builder()
                    .course(course)
                    .weekNumber(weekNumber)
                    .weekTitle(String.format("%d주차", weekNumber))
                    .build();
            courseWeekRepository.save(week);
        }
    }

    /**
     * CreateCourseResponseDto 생성
     */
    private CreateCourseResponseDto buildCreateCourseResponse(Course course, Subject subject,
                                                              boolean isNewSubject, AcademicTerm academicTerm) {
        List<ScheduleDto> schedules = course.getSchedules().stream()
                .sorted(Comparator.comparing(CourseSchedule::getDayOfWeek)
                        .thenComparing(CourseSchedule::getStartTime))
                .map(this::convertToScheduleDto)
                .collect(Collectors.toList());

        return CreateCourseResponseDto.builder()
                .id(course.getId())
                .courseCode(subject.getSubjectCode())
                .courseName(subject.getSubjectName())
                .section(course.getSectionNumber())
                .credits(subject.getCredits())
                .maxStudents(course.getMaxStudents())
                .description(course.getDescription())
                .status("DRAFT")
                .subjectId(subject.getId())
                .isNewlyCreated(isNewSubject)
                .academicTermId(academicTerm.getId())
                .schedules(schedules)
                .createdAt(course.getCreatedAt())
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

        // 1. 요청 검증
        validateUpdateCourseRequest(courseId, professorId, request);

        // 2. 강의 조회 및 권한 확인
        Course course = findCourseWithPermission(courseId, professorId, "강의 수정 권한이 없습니다.");

        // 3. 정원 검증
        validateMaxStudents(request.getMaxStudents(), course.getCurrentStudents());

        // 4. 강의 정보 수정
        updateCourseInfo(course, request);

        // 5. 스케줄 수정
        updateCourseSchedules(course, request.getSchedules(), professorId);

        Course updatedCourse = courseRepository.save(course);
        log.info("강의 수정 완료: courseId={}", updatedCourse.getId());

        // 6. 응답 DTO 생성
        return buildCreateCourseResponse(updatedCourse, updatedCourse.getSubject(), false, updatedCourse.getAcademicTerm());
    }

    /**
     * 강의 수정 요청 검증
     */
    private void validateUpdateCourseRequest(Long courseId, Long professorId, UpdateCourseRequestDto request) {
        if (courseId == null) {
            throw new IllegalArgumentException("강의 ID는 필수입니다.");
        }
        if (professorId == null) {
            throw new IllegalArgumentException("교수 ID는 필수입니다.");
        }
        if (request == null) {
            throw new IllegalArgumentException("요청 정보는 필수입니다.");
        }
    }

    /**
     * 강의 조회 및 권한 확인
     * 주의: courseId와 professorId는 호출 전에 null 체크가 완료되어야 함
     */
    private Course findCourseWithPermission(Long courseId, Long professorId, String permissionErrorMessage) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException(permissionErrorMessage);
        }
        return course;
    }

    /**
     * 정원 검증
     */
    private void validateMaxStudents(Integer maxStudents, Integer currentStudents) {
        if (maxStudents != null && maxStudents < currentStudents) {
            throw new IllegalArgumentException(
                    String.format("현재 수강생 수(%d명)보다 적은 정원(%d명)으로 설정할 수 없습니다.",
                            currentStudents, maxStudents));
        }
        }

    /**
     * 강의 정보 수정
     */
    private void updateCourseInfo(Course course, UpdateCourseRequestDto request) {
        if (request.getSectionNumber() != null) {
            updateSectionNumber(course, request.getSectionNumber());
        }
        if (request.getMaxStudents() != null) {
            course.setMaxStudents(request.getMaxStudents());
        }
    }

    /**
     * 분반 번호 수정
     */
    private void updateSectionNumber(Course course, String newSectionNumber) {
        // 분반 번호가 같으면 변경하지 않음
        if (course.getSectionNumber().equals(newSectionNumber)) {
            return;
        }

        // 중복 체크
        if (courseRepository.existsBySubjectIdAndAcademicTermIdAndSectionNumber(
                course.getSubject().getId(), course.getAcademicTerm().getId(), newSectionNumber)) {
            throw new IllegalArgumentException("이미 동일한 분반으로 개설된 강의가 있습니다.");
        }

        course.setSectionNumber(newSectionNumber);
    }

    /**
     * 강의 스케줄 수정
     */
    private void updateCourseSchedules(Course course, List<ScheduleRequestDto> schedules, Long professorId) {
        if (schedules == null) {
            return;
        }

        // 시간표 충돌 체크를 먼저 수행 (현재 강의 제외)
        // 충돌이 있으면 예외를 던지므로 기존 스케줄은 그대로 유지됨
        validateScheduleConflicts(schedules, professorId, course.getAcademicTerm().getId(), course.getId());

        // 충돌이 없으면 기존 스케줄 삭제 후 새 스케줄 추가
        course.getSchedules().clear();
        addSchedulesToCourse(course, schedules);
    }

    /**
     * 강의 취소
     */
    public void cancelCourse(Long courseId, Long professorId) {
        log.info("강의 취소 요청: courseId={}, professorId={}", courseId, professorId);

        // 1. 강의 조회 및 권한 확인
        Course course = findCourseWithPermission(courseId, professorId, "강의 취소 권한이 없습니다.");

        // 2. 수강생 확인
        validateNoEnrollments(courseId);

        // 3. 강의 삭제
        courseRepository.delete(course);
        log.info("강의 취소 완료: courseId={}", courseId);
    }

    /**
     * 수강생 존재 여부 검증
     */
    private void validateNoEnrollments(Long courseId) {
        long enrollmentCount = enrollmentRepository.findByCourseId(courseId).size();
        if (enrollmentCount > 0) {
            throw new IllegalArgumentException(
                    String.format("수강생이 %d명 있어 강의를 취소할 수 없습니다.", enrollmentCount));
        }
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
        Course course = findCourseWithPermission(courseId, professorId, "강의 조회 권한이 없습니다.");

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

