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

    /**
     * 주차 생성
     */
    public WeekDto createWeek(Long courseId, CreateWeekRequestDto request, Long professorId) {
        log.info("주차 생성 요청: courseId={}, weekNumber={}, professorId={}, contentsCount={}",
                courseId, request.getWeekNumber(), professorId,
                request.getContents() != null ? request.getContents().size() : 0);

        // 1. 강의 조회 및 권한 확인
        if (courseId == null) {
            throw new IllegalArgumentException("강의 ID는 필수입니다.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("주차 생성 권한이 없습니다.");
        }

        // 2. 주차 번호 16주 초과 체크
        if (request.getWeekNumber() > 16) {
            throw new IllegalArgumentException("주차는 최대 16주까지만 생성할 수 있습니다.");
        }

        // 3. 중복 주차 번호 체크
        if (courseWeekRepository.existsByCourseIdAndWeekNumber(courseId, request.getWeekNumber())) {
            throw new IllegalArgumentException(
                    String.format("이미 %d주차가 존재합니다.", request.getWeekNumber()));
        }

        // 4. 콘텐츠 유효성 검사
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

        // 5. 주차 생성
        CourseWeek week = CourseWeek.builder()
                .course(course)
                .weekNumber(request.getWeekNumber())
                .weekTitle(request.getWeekTitle())
                .build();

        CourseWeek savedWeek = courseWeekRepository.save(week);

        // 6. 콘텐츠 생성
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
        log.info("주차 수정 요청: courseId={}, weekId={}, professorId={}", courseId, weekId, professorId);

        // 1. 강의 조회 및 권한 확인
        if (courseId == null) {
            throw new IllegalArgumentException("강의 ID는 필수입니다.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("주차 수정 권한이 없습니다.");
        }

        // 2. 주차 조회
        if (weekId == null) {
            throw new IllegalArgumentException("주차 ID는 필수입니다.");
        }
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("주차를 찾을 수 없습니다."));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 주차가 아닙니다.");
        }

        // 3. 주차 번호 변경 시 16주 초과 체크
        if (request.getWeekNumber() != null && request.getWeekNumber() > 16) {
            throw new IllegalArgumentException("주차는 최대 16주까지만 생성할 수 있습니다.");
        }

        // 4. 주차 번호 변경 시 중복 체크
        if (request.getWeekNumber() != null && !week.getWeekNumber().equals(request.getWeekNumber())) {
            if (courseWeekRepository.existsByCourseIdAndWeekNumber(courseId, request.getWeekNumber())) {
                throw new IllegalArgumentException(
                        String.format("이미 %d주차가 존재합니다.", request.getWeekNumber()));
            }
        }

        // 5. 주차 정보 수정
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

        // 3. displayOrder 설정 (order가 없으면 마지막 순서로)
        int displayOrder;
        if (request.getOrder() != null) {
            displayOrder = request.getOrder();
            // displayOrder 중복 체크
            if (weekContentRepository.existsByWeekIdAndDisplayOrder(weekId, displayOrder)) {
                throw new IllegalArgumentException(
                        String.format("이미 동일한 순서(%d)의 콘텐츠가 존재합니다.", displayOrder));
            }
        } else {
            // 기존 콘텐츠의 최대 displayOrder 조회
            List<WeekContent> existingContents = weekContentRepository.findByWeekIdOrderByDisplayOrder(weekId);
            displayOrder = existingContents.stream()
                    .mapToInt(WeekContent::getDisplayOrder)
                    .max()
                    .orElse(0) + 1;
        }

        // 4. 콘텐츠 생성
        WeekContent content = WeekContent.builder()
                .week(week)
                .contentType(request.getContentType())
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

        // 3. displayOrder 변경 시 중복 체크
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder != null && !content.getDisplayOrder().equals(displayOrder)) {
            if (weekContentRepository.existsByWeekIdAndDisplayOrder(weekId, displayOrder)) {
                throw new IllegalArgumentException(
                        String.format("이미 동일한 순서(%d)의 콘텐츠가 존재합니다.", displayOrder));
            }
        }

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
     * 콘텐츠 수정 (contentId만으로)
     */
    public WeekContentDto updateContentByContentId(Long contentId, UpdateWeekContentRequestDto request, Long professorId) {
        log.info("콘텐츠 수정 요청: contentId={}, professorId={}", contentId, professorId);

        if (contentId == null || request == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 콘텐츠 조회
        WeekContent content = weekContentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        // 2. 강의 조회 및 권한 확인
        Course course = content.getWeek().getCourse();
        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 수정 권한이 없습니다.");
        }

        // 3. displayOrder 변경 시 중복 체크
        Integer displayOrder = request.getDisplayOrder();
        Long weekId = content.getWeek().getId();
        if (displayOrder != null && !content.getDisplayOrder().equals(displayOrder)) {
            if (weekContentRepository.existsByWeekIdAndDisplayOrder(weekId, displayOrder)) {
                throw new IllegalArgumentException(
                        String.format("이미 동일한 순서(%d)의 콘텐츠가 존재합니다.", displayOrder));
            }
        }

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
     * 콘텐츠 삭제 (contentId만으로)
     */
    public void deleteContentByContentId(Long contentId, Long professorId) {
        log.info("콘텐츠 삭제 요청: contentId={}, professorId={}", contentId, professorId);

        if (contentId == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 콘텐츠 조회
        WeekContent content = weekContentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        // 2. 강의 조회 및 권한 확인
        Course course = content.getWeek().getCourse();
        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 삭제 권한이 없습니다.");
        }

        // 3. 콘텐츠 삭제
        weekContentRepository.delete(content);

        log.info("콘텐츠 삭제 완료: contentId={}", contentId);
    }

    /**
     * 주차별 콘텐츠 목록 조회
     */
    @Transactional(readOnly = true)
    public WeekContentsResponseDto getWeekContents(Long courseId, Long weekId, Long professorId) {
        log.info("주차별 콘텐츠 목록 조회: courseId={}, weekId={}, professorId={}", courseId, weekId, professorId);

        if (courseId == null || weekId == null || professorId == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        // 1. 강의 조회 및 권한 확인
        if (courseId == null) {
            throw new IllegalArgumentException("강의 ID는 필수입니다.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 조회 권한이 없습니다.");
        }

        // 2. 주차 조회
        if (weekId == null) {
            throw new IllegalArgumentException("주차 ID는 필수입니다.");
        }
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("주차를 찾을 수 없습니다."));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 주차가 아닙니다.");
        }

        // 3. 콘텐츠 목록 조회
        List<WeekContent> contents = weekContentRepository.findByWeekIdOrderByDisplayOrder(weekId);

        List<WeekContentDto> contentDtos = contents.stream()
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

        // 1. 강의 조회 및 권한 확인
        if (courseId == null) {
            throw new IllegalArgumentException("강의 ID는 필수입니다.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        // 교수이거나 수강 중인 학생인지 확인
        boolean isProfessor = course.getProfessor().getProfessorId().equals(requesterId);
        boolean isEnrolledStudent = enrollmentRepository.existsByStudentIdAndCourseId(requesterId, courseId);

        if (!isProfessor && !isEnrolledStudent) {
            throw new IllegalArgumentException("주차 조회 권한이 없습니다.");
        }

        // 2. 주차 목록 조회
        List<CourseWeek> weeks = courseWeekRepository.findByCourseId(courseId);

        // 3. 각 주차별 콘텐츠 조회 및 DTO 변환
        return weeks.stream()
                .sorted(Comparator.comparing(CourseWeek::getWeekNumber))
                .map(week -> {
                    List<WeekContent> contents = weekContentRepository.findByWeekIdOrderByDisplayOrder(week.getId());
                    List<WeekContentDto> contentDtos = contents.stream()
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
     * 콘텐츠 순서 변경
     */
    public ReorderContentsResponseDto reorderContents(Long courseId, Long weekId,
                                                      ReorderContentsRequestDto request, Long professorId) {
        log.info("콘텐츠 순서 변경: courseId={}, weekId={}, professorId={}", courseId, weekId, professorId);

        // 1. 강의 조회 및 권한 확인
        if (courseId == null) {
            throw new IllegalArgumentException("강의 ID는 필수입니다.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        if (!course.getProfessor().getProfessorId().equals(professorId)) {
            throw new IllegalArgumentException("콘텐츠 순서 변경 권한이 없습니다.");
        }

        // 2. 주차 조회
        if (weekId == null) {
            throw new IllegalArgumentException("주차 ID는 필수입니다.");
        }
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("주차를 찾을 수 없습니다."));

        if (!week.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 주차가 아닙니다.");
        }

        // 3. 콘텐츠 순서 변경
        if (request.getContentOrders() != null && !request.getContentOrders().isEmpty()) {
            for (ContentOrderDto orderDto : request.getContentOrders()) {
                if (orderDto.getContentId() == null) {
                    throw new IllegalArgumentException("콘텐츠 ID는 필수입니다.");
                }
                WeekContent content = weekContentRepository.findById(orderDto.getContentId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format("콘텐츠를 찾을 수 없습니다. contentId: %d", orderDto.getContentId())));

                if (!content.getWeek().getId().equals(weekId)) {
                    throw new IllegalArgumentException("해당 주차의 콘텐츠가 아닙니다.");
                }

                // displayOrder 업데이트
                content.update(
                        null, // contentType
                        null, // title
                        null, // contentUrl
                        null, // duration
                        orderDto.getOrder() // displayOrder
                );
                weekContentRepository.save(content);
            }
        }

        log.info("콘텐츠 순서 변경 완료: weekId={}", weekId);

        // 4. 응답 DTO 생성
        List<ContentOrderDto> reorderedContents = request.getContentOrders() != null
                ? request.getContentOrders()
                : List.of();

        return ReorderContentsResponseDto.builder()
                .weekId(weekId)
                .reorderedContents(reorderedContents)
                .build();
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

