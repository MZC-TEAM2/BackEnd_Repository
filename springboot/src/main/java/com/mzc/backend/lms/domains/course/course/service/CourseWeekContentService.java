package com.mzc.backend.lms.domains.course.course.service;

import com.mzc.backend.lms.domains.course.course.dto.*;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseWeek;
import com.mzc.backend.lms.domains.course.course.entity.WeekContent;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.course.repository.CourseWeekRepository;
import com.mzc.backend.lms.domains.course.course.repository.WeekContentRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주차별 콘텐츠 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseWeekContentService {

    private final CourseRepository courseRepository;
    private final CourseWeekRepository courseWeekRepository;
    private final WeekContentRepository weekContentRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final List<String> CONTENT_TYPE_PRIORITY = List.of("LINK", "DOCUMENT", "VIDEO", "QUIZ");

    /**
     * 주차 생성
     */
    public WeekDto createWeek(Long courseId, CreateWeekRequestDto request, Long professorId) {
        if (courseId == null || request == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        log.info("주차 생성 요청: courseId={}, weekNumber={}, professorId={}, contentsCount={}",
                courseId, request.getWeekNumber(), professorId,
                request.getContents() != null ? request.getContents().size() : 0);

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("주차 생성 권한이 없습니다.");
        }

        // 2. 중복 주차 번호 체크
        if (courseWeekRepository.existsByCourseIdAndWeekNumber(courseId, request.getWeekNumber())) {
            throw new IllegalArgumentException(
                    String.format("이미 %d주차가 존재합니다.", request.getWeekNumber()));
        }

        // 3. 콘텐츠 유효성 검사
        if (request.getContents() != null && !request.getContents().isEmpty()) {
            for (CreateWeekContentRequestDto contentDto : request.getContents()) {
                // 필수 필드 검증
                if (contentDto.getContentType() == null || contentDto.getContentType().trim().isEmpty()) {
                    throw new IllegalArgumentException("콘텐츠 타입은 필수입니다.");
                }
                if (contentDto.getTitle() == null || contentDto.getTitle().trim().isEmpty()) {
                    throw new IllegalArgumentException("콘텐츠 제목은 필수입니다.");
                }
                if (contentDto.getContentUrl() == null || contentDto.getContentUrl().trim().isEmpty()) {
                    throw new IllegalArgumentException("콘텐츠 URL은 필수입니다.");
                }
                
                // 지원하는 콘텐츠 타입 검증
                String contentType = contentDto.getContentType().toUpperCase();
                if (!contentType.equals("VIDEO") && !contentType.equals("DOCUMENT") 
                    && !contentType.equals("LINK") && !contentType.equals("QUIZ")) {
                    throw new IllegalArgumentException("지원하지 않는 콘텐츠 타입입니다: " + contentType);
                }
            }
        }

        // 4. 주차 생성
        CourseWeek week = CourseWeek.builder()
                .course(course)
                .weekNumber(request.getWeekNumber())
                .weekTitle(request.getWeekTitle())
                .build();

        if (week == null) {
            throw new IllegalArgumentException("주차 생성에 실패했습니다.");
        }

        CourseWeek savedWeek = courseWeekRepository.save(week);

        // 5. 콘텐츠 생성
        List<WeekContentDto> contentDtos = new ArrayList<>();
        if (request.getContents() != null && !request.getContents().isEmpty()) {
            // 기존 콘텐츠의 최대 displayOrder 조회
            List<WeekContent> existingContents = weekContentRepository.findByWeekIdOrderByDisplayOrder(savedWeek.getId());
            int maxOrder = existingContents.stream()
                    .mapToInt(WeekContent::getDisplayOrder)
                    .max()
                    .orElse(0);

            for (int i = 0; i < request.getContents().size(); i++) {
                CreateWeekContentRequestDto contentDto = request.getContents().get(i);
                
                // order가 없으면 자동으로 설정
                int displayOrder = contentDto.getOrder() != null 
                    ? contentDto.getOrder() 
                    : maxOrder + i + 1;
                
                // order 중복 체크
                if (weekContentRepository.existsByWeekIdAndDisplayOrder(savedWeek.getId(), displayOrder)) {
                    throw new IllegalArgumentException(
                            String.format("콘텐츠 순서 %d가 이미 존재합니다.", displayOrder));
                }

                WeekContent content = WeekContent.builder()
                        .week(savedWeek)
                        .contentType(contentDto.getContentType().toUpperCase())
                        .title(contentDto.getTitle())
                        .contentUrl(contentDto.getContentUrl())
                        .duration(contentDto.getDuration())
                        .displayOrder(displayOrder)
                        .build();

                if (content == null) {
                    throw new IllegalArgumentException("콘텐츠 생성에 실패했습니다.");
                }

                WeekContent savedContent = weekContentRepository.save(content);
                
                contentDtos.add(WeekContentDto.builder()
                        .id(savedContent.getId())
                        .contentType(savedContent.getContentType())
                        .title(savedContent.getTitle())
                        .contentUrl(savedContent.getContentUrl())
                        .duration(savedContent.getDuration())
                        .order(savedContent.getDisplayOrder())
                        .createdAt(savedContent.getCreatedAt())
                        .build());
            }
        }

        log.info("주차 생성 완료: weekId={}, weekNumber={}, contentsCount={}", 
                savedWeek.getId(), savedWeek.getWeekNumber(), contentDtos.size());

        return WeekDto.builder()
                .id(savedWeek.getId())
                .weekNumber(savedWeek.getWeekNumber())
                .weekTitle(savedWeek.getWeekTitle())
                .contents(contentDtos)
                .createdAt(savedWeek.getCreatedAt())
                .build();
    }

    /**
     * 주차 수정
     */
    public WeekDto updateWeek(Long courseId, Long weekId, UpdateWeekRequestDto request, Long professorId) {
        if (courseId == null || weekId == null || request == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        log.info("주차 수정 요청: courseId={}, weekId={}, professorId={}", courseId, weekId, professorId);

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("주차 수정 권한이 없습니다.");
        }

        // 2. 주차 조회
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("주차를 찾을 수 없습니다."));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 주차가 아닙니다.");
        }

        // 3. 주차 번호 변경 시 중복 체크
        if (request.getWeekNumber() != null && !week.getWeekNumber().equals(request.getWeekNumber())) {
            if (courseWeekRepository.existsByCourseIdAndWeekNumber(courseId, request.getWeekNumber())) {
                throw new IllegalArgumentException(
                        String.format("이미 %d주차가 존재합니다.", request.getWeekNumber()));
            }
        }

        // 4. 주차 정보 수정
        week.update(request.getWeekNumber(), request.getWeekTitle());

        CourseWeek updatedWeek = courseWeekRepository.save(week);

        log.info("주차 수정 완료: weekId={}", updatedWeek.getId());

        return WeekDto.builder()
                .id(updatedWeek.getId())
                .weekNumber(updatedWeek.getWeekNumber())
                .weekTitle(updatedWeek.getWeekTitle())
                .build();
    }

    /**
     * 주차 삭제
     */
    public void deleteWeek(Long courseId, Long weekId, Long professorId) {
        log.info("주차 삭제 요청: courseId={}, weekId={}, professorId={}", courseId, weekId, professorId);

        if (courseId == null || weekId == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("주차 삭제 권한이 없습니다.");
        }

        // 2. 주차 조회
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("주차를 찾을 수 없습니다."));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 주차가 아닙니다.");
        }

        // 3. 주차 삭제 (연관된 콘텐츠도 함께 삭제됨 - Cascade)
        weekContentRepository.deleteByWeekId(weekId);
        courseWeekRepository.delete(week);

        log.info("주차 삭제 완료: weekId={}", weekId);
    }

    /**
     * 콘텐츠 등록
     */
    public WeekContentDto createContent(Long courseId, Long weekId, CreateWeekContentRequestDto request, Long professorId) {
        log.info("콘텐츠 등록 요청: courseId={}, weekId={}, contentType={}, professorId={}",
                courseId, weekId, request.getContentType(), professorId);
        
        if (courseId == null || weekId == null || request == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 등록 권한이 없습니다.");
        }

        // 2. 주차 조회
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("주차를 찾을 수 없습니다."));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 주차가 아닙니다.");
        }

        // 3. displayOrder는 서버에서 자동 부여 (순서 변경 기능 제거)
        List<WeekContent> existingContents = weekContentRepository.findByWeekIdOrderByDisplayOrder(weekId);
        int displayOrder = existingContents.stream()
                .mapToInt(WeekContent::getDisplayOrder)
                .max()
                .orElse(0) + 1;

        // 4. 콘텐츠 생성
        WeekContent content = WeekContent.builder()
                .week(week)
                .contentType(request.getContentType() != null ? request.getContentType().trim().toUpperCase() : null)
                .title(request.getTitle())
                .contentUrl(request.getContentUrl())
                .duration(request.getDuration())
                .displayOrder(displayOrder)
                .build();

        if (content == null) {
            throw new IllegalArgumentException("콘텐츠 생성에 실패했습니다.");
        }

        WeekContent savedContent = weekContentRepository.save(content);

        log.info("콘텐츠 등록 완료: contentId={}, title={}", savedContent.getId(), savedContent.getTitle());

        return convertToWeekContentDto(savedContent);
    }

    /**
     * 콘텐츠 수정
     */
    public WeekContentDto updateContent(Long courseId, Long weekId, Long contentId,
                                        UpdateWeekContentRequestDto request, Long professorId) {
        log.info("콘텐츠 수정 요청: courseId={}, weekId={}, contentId={}, professorId={}",
                courseId, weekId, contentId, professorId);

        if (courseId == null || weekId == null || contentId == null || request == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 수정 권한이 없습니다.");
        }

        // 2. 콘텐츠 조회
        WeekContent content = weekContentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        if (!content.getWeek().getId().equals(weekId)) {
            throw new IllegalArgumentException("해당 주차의 콘텐츠가 아닙니다.");
        }

        if (!content.getWeek().getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 콘텐츠가 아닙니다.");
        }

        // 3. 순서 변경은 지원하지 않음 (displayOrder 무시)
        Integer displayOrder = null;

        // 4. 콘텐츠 정보 수정
        content.update(
                request.getContentType(),
                request.getTitle(),
                request.getContentUrl(),
                request.getDuration(),
                displayOrder
        );

        WeekContent updatedContent = weekContentRepository.save(content);

        log.info("콘텐츠 수정 완료: contentId={}", updatedContent.getId());

        return convertToWeekContentDto(updatedContent);
    }

    /**
     * 콘텐츠 삭제
     */
    public void deleteContent(Long courseId, Long weekId, Long contentId, Long professorId) {
        log.info("콘텐츠 삭제 요청: courseId={}, weekId={}, contentId={}, professorId={}",
                courseId, weekId, contentId, professorId);

        if (courseId == null || weekId == null || contentId == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 삭제 권한이 없습니다.");
        }

        // 2. 콘텐츠 조회
        WeekContent content = weekContentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        if (!content.getWeek().getId().equals(weekId)) {
            throw new IllegalArgumentException("해당 주차의 콘텐츠가 아닙니다.");
        }

        if (!content.getWeek().getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 콘텐츠가 아닙니다.");
        }

        // 3. 콘텐츠 삭제
        weekContentRepository.delete(content);

        log.info("콘텐츠 삭제 완료: contentId={}", contentId);
    }

    /**
     * 콘텐츠 수정 (단일 contentId, 교수용)
     * - 순서 변경은 지원하지 않음 (displayOrder 무시)
     */
    public WeekContentDto updateContentByContentId(Long contentId, UpdateWeekContentRequestDto request, Long professorId) {
        if (contentId == null || request == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        WeekContent content = weekContentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        Course course = content.getWeek().getCourse();
        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 수정 권한이 없습니다.");
        }

        content.update(
                request.getContentType(),
                request.getTitle(),
                request.getContentUrl(),
                request.getDuration(),
                null
        );

        WeekContent updated = weekContentRepository.save(content);
        return convertToWeekContentDto(updated);
    }

    /**
     * 콘텐츠 삭제 (단일 contentId, 교수용)
     */
    public void deleteContentByContentId(Long contentId, Long professorId) {
        if (contentId == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        WeekContent content = weekContentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        Course course = content.getWeek().getCourse();
        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 삭제 권한이 없습니다.");
        }

        weekContentRepository.delete(content);
    }

    /**
     * 주차별 콘텐츠 목록 조회
     */
    @Transactional(readOnly = true)
    public WeekContentsResponseDto getWeekContents(Long courseId, Long weekId, Long requesterId) {
        log.info("주차별 콘텐츠 목록 조회: courseId={}, weekId={}, requesterId={}", courseId, weekId, requesterId);

        if (courseId == null || weekId == null || requesterId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        assertCanViewCourse(course, requesterId);

        // 2. 주차 조회
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("주차를 찾을 수 없습니다."));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 주차가 아닙니다.");
        }

        // 3. 콘텐츠 목록 조회
        List<WeekContent> contents = weekContentRepository.findByWeekIdOrderByDisplayOrder(weekId);

        List<WeekContentDto> contentDtos = contents.stream()
                .sorted(contentPriorityComparator())
                .map(this::convertToWeekContentDto)
                .collect(Collectors.toList());

        return WeekContentsResponseDto.builder()
                .weekId(week.getId())
                .weekNumber(week.getWeekNumber())
                .weekTitle(week.getWeekTitle())
                .contents(contentDtos)
                .build();
    }

    /**
     * 강의 주차 목록 조회 (교수/수강중 학생)
     */
    @Transactional(readOnly = true)
    public List<WeekListResponseDto> getWeeks(Long courseId, Long requesterId) {
        log.info("강의 주차 목록 조회: courseId={}, requesterId={}", courseId, requesterId);

        if (courseId == null || requesterId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 강의 조회 및 권한 확인
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        assertCanViewCourse(course, requesterId);

        // 2. 주차 목록 조회
        List<CourseWeek> weeks = courseWeekRepository.findByCourseId(courseId);

        // 3. 각 주차별 콘텐츠 조회 및 DTO 변환
        return weeks.stream()
                .sorted(Comparator.comparing(CourseWeek::getWeekNumber))
                .map(week -> {
                    List<WeekContent> contents = weekContentRepository.findByWeekIdOrderByDisplayOrder(week.getId());
                    List<WeekContentDto> contentDtos = contents.stream()
                            .sorted(contentPriorityComparator())
                            .map(this::convertToWeekContentDto)
                            .collect(Collectors.toList());

                    return WeekListResponseDto.builder()
                            .id(week.getId())
                            .weekNumber(week.getWeekNumber())
                            .weekTitle(week.getWeekTitle())
                            .contents(contentDtos)
                            .createdAt(week.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 주차/콘텐츠 조회 권한 체크
     * - 교수: 본인 강의
     * - 학생: 해당 강의를 수강신청(수강중)한 경우
     */
    private void assertCanViewCourse(Course course, Long requesterId) {
        if (course == null || requesterId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        boolean isProfessorOfCourse = course.getProfessor() != null
                && course.getProfessor().getProfessorId() != null
                && course.getProfessor().getProfessorId().equals(requesterId);

        boolean isEnrolledStudent = enrollmentRepository.existsByStudentIdAndCourseId(requesterId, course.getId());

        if (!isProfessorOfCourse && !isEnrolledStudent) {
            throw new IllegalArgumentException("주차/콘텐츠 조회 권한이 없습니다.");
        }
    }

    private Comparator<WeekContent> contentPriorityComparator() {
        return Comparator
                .comparingInt((WeekContent wc) -> {
                    String type = wc.getContentType() != null ? wc.getContentType().trim().toUpperCase() : "";
                    int idx = CONTENT_TYPE_PRIORITY.indexOf(type);
                    return idx >= 0 ? idx : Integer.MAX_VALUE;
                })
                .thenComparingInt(wc -> wc.getDisplayOrder() != null ? wc.getDisplayOrder() : Integer.MAX_VALUE)
                .thenComparing(wc -> wc.getId() != null ? wc.getId() : Long.MAX_VALUE);
    }

    /**
     * WeekContentDto 변환
     */
    private WeekContentDto convertToWeekContentDto(WeekContent content) {
        return WeekContentDto.builder()
                .id(content.getId())
                .contentType(content.getContentType())
                .title(content.getTitle())
                .contentUrl(content.getContentUrl())
                .duration(content.getDuration())
                .order(content.getDisplayOrder())
                .createdAt(content.getCreatedAt())
                .build();
    }
}

