package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import com.mzc.backend.lms.domains.academy.repository.EnrollmentPeriodRepository;
import com.mzc.backend.lms.domains.course.constants.CourseConstants;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.subject.entity.SubjectPrerequisites;
import com.mzc.backend.lms.domains.course.subject.repository.SubjectPrerequisitesRepository;
import com.mzc.backend.lms.domains.enrollment.dto.*;
import com.mzc.backend.lms.domains.enrollment.entity.CourseCart;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import com.mzc.backend.lms.domains.enrollment.repository.CourseCartRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.user.student.repository.StudentRepository;
import com.mzc.backend.lms.views.UserViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 장바구니 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CourseCartRepository courseCartRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentPeriodRepository enrollmentPeriodRepository;
    private final SubjectPrerequisitesRepository subjectPrerequisitesRepository;
    private final StudentRepository studentRepository;
    private final UserViewService userViewService;

    private static final int MAX_CREDITS_PER_TERM = 21; // 학기당 최대 학점

    @Override
    public CartResponseDto getCart(String studentId) {
        Long studentIdLong = Long.parseLong(studentId);

        // 학생의 장바구니 목록 조회
        List<CourseCart> cartItems = courseCartRepository.findByStudentId(studentIdLong);

        // DTO 변환
        List<CartItemDto> cartItemDtos = cartItems.stream()
                .map(cart -> convertToCartItemDto(cart))
                .collect(Collectors.toList());

        // 총 강의 수 계산
        int totalCourses = cartItemDtos.size();

        // 총 학점 계산
        int totalCredits = cartItemDtos.stream()
                .mapToInt(item -> item.getCourse().getCredits())
                .sum();

        return CartResponseDto.builder()
                .totalCourses(totalCourses)
                .totalCredits(totalCredits)
                .courses(cartItemDtos)
                .build();
    }

    private CartItemDto convertToCartItemDto(CourseCart cart) {
        Course course = cart.getCourse();

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

        // CourseType 이름 변환
        String courseTypeName = CourseConstants.COURSE_TYPE_NAME_MAP.getOrDefault(
                course.getSubject().getCourseType().getTypeCode(), "기타"
        );

        // 강의 정보 DTO
        CartItemDto.CourseInfoDto courseInfo = CartItemDto.CourseInfoDto.builder()
                .id(course.getId())
                .code(course.getSubject().getSubjectCode())
                .name(course.getSubject().getSubjectName())
                .section(course.getSectionNumber())
                .credits(course.getSubject().getCredits())
                .courseType(courseTypeName)
                .build();

        // 교수 정보 DTO
        ProfessorDto professorDto = ProfessorDto.builder()
                .id(course.getProfessor().getProfessorId())
                .name(professorName != null ? professorName : "교수")
                .build();

        // 수강신청 정보 DTO
        EnrollmentDto enrollmentDto = EnrollmentDto.builder()
                .current(course.getCurrentStudents())
                .max(course.getMaxStudents())
                .isFull(course.getCurrentStudents() >= course.getMaxStudents())
                .availableSeats(course.getMaxStudents() - course.getCurrentStudents())
                .build();

        return CartItemDto.builder()
                .cartId(cart.getId())
                .course(courseInfo)
                .professor(professorDto)
                .schedule(schedules)
                .enrollment(enrollmentDto)
                .addedAt(cart.getAddedAt())
                .build();
    }

    private ScheduleDto convertToScheduleDto(CourseSchedule schedule) {
        DayOfWeek dayOfWeek = schedule.getDayOfWeek();
        return ScheduleDto.builder()
                .dayOfWeek(dayOfWeek.getValue())
                .dayName(CourseConstants.DAY_NAME_MAP.get(dayOfWeek))
                .startTime(schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .endTime(schedule.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                .classroom(schedule.getScheduleRoom())
                .build();
    }

    @Override
    public CartBulkAddResponseDto addToCartBulk(CartBulkAddRequestDto request, String studentId) {
        
        // 1. 수강신청 기간 체크
        if (!isEnrollmentPeriodActive()) {
            throw new IllegalArgumentException("수강신청 기간이 아닙니다.");
        }

        Long studentIdLong = Long.parseLong(studentId);

        // 2. 학생 정보 조회
        Student student = studentRepository.findById(studentIdLong)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 3. 과목 존재 여부 체크 및 조회
        List<Long> courseIds = request.getCourseIds();
        if (courseIds == null || courseIds.isEmpty()) {
            throw new IllegalArgumentException("강의 ID 목록이 비어있습니다.");
        }

        List<Course> courses = courseRepository.findAllById(courseIds);
        if (courses.size() != courseIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 강의가 포함되어 있습니다.");
        }

        // 4. 기존 장바구니 및 수강신청 정보 조회
        List<CourseCart> existingCarts = courseCartRepository.findByStudentId(studentIdLong);
        List<Enrollment> existingEnrollments = enrollmentRepository.findByStudentId(studentIdLong);

        Set<Long> cartCourseIds = existingCarts.stream()
                .map(cart -> cart.getCourse().getId())
                .collect(Collectors.toSet());
        Set<Long> enrolledCourseIds = existingEnrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());
        Set<Long> enrolledSubjectIds = existingEnrollments.stream()
                .map(enrollment -> enrollment.getCourse().getSubject().getId())
                .collect(Collectors.toSet());

        // 장바구니에 있는 과목의 subject_id도 체크용으로 수집
        Set<Long> cartSubjectIds = existingCarts.stream()
                .map(cart -> cart.getCourse().getSubject().getId())
                .collect(Collectors.toSet());

        // 5. 각 강의에 대한 검증
        Set<Long> newSubjectIds = new HashSet<>(); // 새로 추가하려는 강의들의 subject_id
        List<String> validationErrors = new ArrayList<>();
        
        for (Course course : courses) {
            Long subjectId = course.getSubject().getId();
            
            // 5-1. 이미 장바구니에 있는지 체크
            if (cartCourseIds.contains(course.getId())) {
                validationErrors.add(String.format("강의 %s(%s)는 이미 장바구니에 있습니다.", 
                        course.getSubject().getSubjectName(), course.getSubject().getSubjectCode()));
                continue;
            }

            // 5-2. 이미 수강신청했는지 체크
            if (enrolledCourseIds.contains(course.getId())) {
                validationErrors.add(String.format("강의 %s(%s)는 이미 수강신청했습니다.", 
                        course.getSubject().getSubjectName(), course.getSubject().getSubjectCode()));
                continue;
            }

            // 5-3. 동일 과목 다른 분반 체크 (장바구니 + 수강신청 + 새로 추가하는 강의들 간)
            if (cartSubjectIds.contains(subjectId) || enrolledSubjectIds.contains(subjectId)) {
                validationErrors.add(String.format("강의 %s(%s)는 이미 다른 분반이 장바구니나 수강신청 목록에 있습니다.", 
                        course.getSubject().getSubjectName(), course.getSubject().getSubjectCode()));
                continue;
            }
            
            // 새로 추가하는 강의들 간 중복 체크
            if (newSubjectIds.contains(subjectId)) {
                validationErrors.add(String.format("강의 %s(%s)는 같은 요청에 중복된 과목입니다.", 
                        course.getSubject().getSubjectName(), course.getSubject().getSubjectCode()));
                continue;
            }
            
            newSubjectIds.add(subjectId);

            // 5-4. 선수과목 이수 여부 체크
            List<SubjectPrerequisites> prerequisites = subjectPrerequisitesRepository.findBySubjectId(subjectId);
            for (SubjectPrerequisites prerequisite : prerequisites) {
                Long prerequisiteSubjectId = prerequisite.getPrerequisite().getId();
                // 선수과목을 이수했는지 확인 (수강신청한 강의 중에서)
                boolean hasPrerequisite = existingEnrollments.stream()
                        .anyMatch(enrollment -> enrollment.getCourse().getSubject().getId().equals(prerequisiteSubjectId));
                
                if (!hasPrerequisite && prerequisite.getIsMandatory()) {
                    validationErrors.add(String.format("강의 %s(%s)의 필수 선수과목 %s(%s)를 이수하지 않았습니다.", 
                            course.getSubject().getSubjectName(), 
                            course.getSubject().getSubjectCode(),
                            prerequisite.getPrerequisite().getSubjectName(),
                            prerequisite.getPrerequisite().getSubjectCode()));
                    break;
                }
            }
        }

        // 하나라도 검증 실패하면 전체 실패
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", validationErrors));
        }

        // 6. 학점 제한 체크
        int currentCredits = existingCarts.stream()
                .mapToInt(cart -> cart.getCourse().getSubject().getCredits())
                .sum();
        int newCredits = courses.stream()
                .mapToInt(course -> course.getSubject().getCredits())
                .sum();
        
        if (currentCredits + newCredits > MAX_CREDITS_PER_TERM) {
            throw new IllegalArgumentException(String.format("학점 제한을 초과합니다. (현재: %d학점, 추가: %d학점, 최대: %d학점)", 
                    currentCredits, newCredits, MAX_CREDITS_PER_TERM));
        }

        // 7. 시간표 충돌 체크
        List<CourseSchedule> existingSchedules = new ArrayList<>();
        for (CourseCart cart : existingCarts) {
            existingSchedules.addAll(cart.getCourse().getSchedules());
        }
        for (Enrollment enrollment : existingEnrollments) {
            existingSchedules.addAll(enrollment.getCourse().getSchedules());
        }

        // 모든 새 강의의 스케줄 수집
        List<CourseSchedule> newSchedules = courses.stream()
                .flatMap(course -> course.getSchedules().stream())
                .collect(Collectors.toList());

        // 기존 강의와의 충돌 체크
        for (CourseSchedule newSchedule : newSchedules) {
            Course newCourse = findCourseBySchedule(newSchedule, courses);
            for (CourseSchedule existingSchedule : existingSchedules) {
                if (hasScheduleConflict(newSchedule, existingSchedule)) {
                    throw new IllegalArgumentException(String.format(
                        "강의 %s(%s)의 시간표가 기존 강의와 충돌합니다.", 
                        newCourse.getSubject().getSubjectName(), newCourse.getSubject().getSubjectCode()));
                }
            }
        }

        // 새로 추가하는 강의들 간의 충돌 체크
        for (int i = 0; i < newSchedules.size(); i++) {
            CourseSchedule schedule1 = newSchedules.get(i);
            Course course1 = findCourseBySchedule(schedule1, courses);
        
            for (int j = i + 1; j < newSchedules.size(); j++) {
                CourseSchedule schedule2 = newSchedules.get(j);
                Course course2 = findCourseBySchedule(schedule2, courses);

                if (hasScheduleConflict(schedule1, schedule2) && 
                    !course1.getId().equals(course2.getId())) {
                    throw new IllegalArgumentException(String.format(
                        "강의 %s(%s)와 %s(%s)의 시간표가 충돌합니다.", 
                        course1.getSubject().getSubjectName(), course1.getSubject().getSubjectCode(),
                        course2.getSubject().getSubjectName(), course2.getSubject().getSubjectCode()));
                }
            }
        }

        // 8. 모든 검증 통과 - 장바구니에 추가
        LocalDateTime now = LocalDateTime.now();
        List<CartBulkAddResponseDto.SucceededItemDto> succeededItems = new ArrayList<>();

        for (Course course : courses) {
            CourseCart cart = CourseCart.builder()
                    .student(student)
                    .course(course)
                    .addedAt(now)
                    .build();
            
            CourseCart savedCart = courseCartRepository.save(cart);
            if (savedCart == null || savedCart.getId() == null) {
                throw new IllegalStateException("장바구니 저장에 실패했습니다.");
            }
            
            succeededItems.add(CartBulkAddResponseDto.SucceededItemDto.builder()
                    .cartId(savedCart.getId())
                    .courseId(course.getId())
                    .courseCode(course.getSubject().getSubjectCode())
                    .courseName(course.getSubject().getSubjectName())
                    .credits(course.getSubject().getCredits())
                    .addedAt(savedCart.getAddedAt())
                    .build());
        }

        CartBulkAddResponseDto.SummaryDto summary = CartBulkAddResponseDto.SummaryDto.builder()
                .totalAttempted(courseIds.size())
                .successCount(succeededItems.size())
                .failedCount(0)
                .build();

        return CartBulkAddResponseDto.builder()
                .summary(summary)
                .succeeded(succeededItems)
                .build();
    }

    /**
     * 수강신청 기간 활성화 여부 확인
     */
    private boolean isEnrollmentPeriodActive() {
        LocalDateTime now = LocalDateTime.now();
        List<EnrollmentPeriod> periods = enrollmentPeriodRepository.findAll();
        
        return periods.stream()
                .anyMatch(period -> {
                    LocalDateTime start = period.getStartDatetime();
                    LocalDateTime end = period.getEndDatetime();
                    return !now.isBefore(start) && !now.isAfter(end);
                });
    }

    /**
     * 시간표 충돌 확인
     */
    private boolean hasScheduleConflict(CourseSchedule schedule1, CourseSchedule schedule2) {
        // 같은 요일이 아니면 충돌 없음
        if (!schedule1.getDayOfWeek().equals(schedule2.getDayOfWeek())) {
            return false;
        }

        LocalTime start1 = schedule1.getStartTime();
        LocalTime end1 = schedule1.getEndTime();
        LocalTime start2 = schedule2.getStartTime();
        LocalTime end2 = schedule2.getEndTime();

        // 시간 겹침 확인
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * 스케줄로부터 해당하는 강의 찾기
     */
    private Course findCourseBySchedule(CourseSchedule schedule, List<Course> courses) {
        return courses.stream()
                .filter(course -> course.getSchedules().contains(schedule))
                .findFirst()
                .orElse(null);
    }

    @Override
    public CartBulkDeleteResponseDto deleteFromCartBulk(CartBulkDeleteRequestDto request, String studentId) {
        Long studentIdLong = Long.parseLong(studentId);

        // cartIds 필수 체크
        if (request.getCartIds() == null || request.getCartIds().isEmpty()) {
            throw new IllegalArgumentException("cartIds는 필수입니다.");
        }

        // 장바구니 항목 조회 및 소유권 확인
        List<CourseCart> cartsToDelete = courseCartRepository.findAllById(request.getCartIds());
        
        if (cartsToDelete.size() != request.getCartIds().size()) {
            throw new IllegalArgumentException("일부 장바구니 항목을 찾을 수 없습니다.");
        }

        // 소유권 확인 및 삭제할 항목 수집
        List<CartBulkDeleteResponseDto.RemovedCourseDto> removedCourses = new ArrayList<>();
        int totalRemovedCredits = 0;

        for (CourseCart cart : cartsToDelete) {
            // 소유권 확인
            if (!cart.getStudent().getStudentId().equals(studentIdLong)) {
                throw new IllegalArgumentException(
                    String.format("장바구니 항목 %d에 대한 접근 권한이 없습니다.", cart.getId()));
            }

            // 삭제할 항목 정보 수집
            Course course = cart.getCourse();
            removedCourses.add(CartBulkDeleteResponseDto.RemovedCourseDto.builder()
                    .cartId(cart.getId())
                    .courseCode(course.getSubject().getSubjectCode())
                    .courseName(course.getSubject().getSubjectName())
                    .credits(course.getSubject().getCredits())
                    .build());
            
            totalRemovedCredits += course.getSubject().getCredits();
        }

        // 장바구니에서 삭제
        courseCartRepository.deleteAll(cartsToDelete);

        return CartBulkDeleteResponseDto.builder()
                .removedCount(removedCourses.size())
                .removedCredits(totalRemovedCredits)
                .removedCourses(removedCourses)
                .build();
    }

    @Override
    public CartBulkDeleteResponseDto deleteAllCart(String studentId) {
        Long studentIdLong = Long.parseLong(studentId);

        // 장바구니 항목 조회 및 소유권 확인
        List<CourseCart> cartsToDelete = courseCartRepository.findByStudentId(studentIdLong);

        // 장바구니에서 삭제
        courseCartRepository.deleteByStudentId(studentIdLong);

        return CartBulkDeleteResponseDto.builder()
                .removedCount(cartsToDelete.size())
                .removedCredits(cartsToDelete.stream()
                        .mapToInt(cart -> cart.getCourse().getSubject().getCredits())
                        .sum())
                .removedCourses(cartsToDelete.stream()
                        .map(cart -> CartBulkDeleteResponseDto.RemovedCourseDto.builder()
                                .cartId(cart.getId())
                                .courseCode(cart.getCourse().getSubject().getSubjectCode())
                                .courseName(cart.getCourse().getSubject().getSubjectName())
                                .credits(cart.getCourse().getSubject().getCredits())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
