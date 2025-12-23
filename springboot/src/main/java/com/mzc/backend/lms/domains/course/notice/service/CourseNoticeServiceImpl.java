package com.mzc.backend.lms.domains.course.notice.service;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.repository.CourseRepository;
import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeCommentRequest;
import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeCreateRequest;
import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeUpdateRequest;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeCommentResponse;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeDetailResponse;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeResponse;
import com.mzc.backend.lms.domains.course.notice.entity.CourseNotice;
import com.mzc.backend.lms.domains.course.notice.entity.CourseNoticeComment;
import com.mzc.backend.lms.domains.course.notice.event.CourseNoticeCreatedEvent;
import com.mzc.backend.lms.domains.course.notice.repository.CourseNoticeCommentRepository;
import com.mzc.backend.lms.domains.course.notice.repository.CourseNoticeRepository;
import com.mzc.backend.lms.domains.enrollment.repository.EnrollmentRepository;
import com.mzc.backend.lms.domains.user.profile.dto.UserBasicInfoDto;
import com.mzc.backend.lms.domains.user.profile.service.UserInfoCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseNoticeServiceImpl implements CourseNoticeService {
	
	private final CourseNoticeRepository courseNoticeRepository;
	private final CourseNoticeCommentRepository courseNoticeCommentRepository;
	private final CourseRepository courseRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final UserInfoCacheService userInfoCacheService;
	private final ApplicationEventPublisher eventPublisher;
	
	// === 공지 CRUD ===
	
	@Override
	@Transactional
	public CourseNoticeResponse createNotice(Long courseId, CourseNoticeCreateRequest request, Long professorId) {
		Course course = findCourseById(courseId);
		validateProfessorPermission(course, professorId);
		
		CourseNotice notice = CourseNotice.create(
				course,
				request.getTitle(),
				request.getContent(),
				request.getAllowComments(),
				professorId
		);
		
		courseNoticeRepository.save(notice);
		log.info("공지사항 생성: courseId={}, noticeId={}, professorId={}", courseId, notice.getId(), professorId);
		
		// 수강생들에게 알림 발송을 위한 이벤트 발행
		eventPublisher.publishEvent(new CourseNoticeCreatedEvent(
				notice.getId(),
				courseId,
				course.getSubject().getSubjectName(),
				notice.getTitle(),
				professorId
		));
		
		String authorName = getAuthorName(professorId);
		return CourseNoticeResponse.from(notice, authorName);
	}
	
	@Override
	public Page<CourseNoticeResponse> getNotices(Long courseId, Long userId, Pageable pageable) {
		validateCourseAccess(courseId, userId);
		
		Page<CourseNotice> notices = courseNoticeRepository.findByCourseId(courseId, pageable);
		
		Set<Long> authorIds = notices.stream()
				.map(CourseNotice::getCreatedBy)
				.collect(Collectors.toSet());
		Map<Long, UserBasicInfoDto> userInfoMap = userInfoCacheService.getUserInfoMap(authorIds);
		
		return notices.map(notice -> {
			String authorName = getAuthorNameFromMap(userInfoMap, notice.getCreatedBy());
			return CourseNoticeResponse.from(notice, authorName);
		});
	}
	
	@Override
	public CourseNoticeDetailResponse getNotice(Long courseId, Long noticeId, Long userId) {
		validateCourseAccess(courseId, userId);
		
		CourseNotice notice = findNoticeByIdAndCourseId(noticeId, courseId);
		String authorName = getAuthorName(notice.getCreatedBy());
		
		List<CourseNoticeCommentResponse> comments = new ArrayList<>();
		if (notice.isCommentsAllowed()) {
			comments = getCommentsWithReplies(noticeId);
		}
		
		return CourseNoticeDetailResponse.from(notice, authorName, comments);
	}
	
	@Override
	@Transactional
	public CourseNoticeResponse updateNotice(Long courseId, Long noticeId, CourseNoticeUpdateRequest request, Long professorId) {
		Course course = findCourseById(courseId);
		validateProfessorPermission(course, professorId);
		
		CourseNotice notice = findNoticeByIdAndCourseId(noticeId, courseId);
		notice.update(request.getTitle(), request.getContent(), request.getAllowComments(), professorId);
		
		log.info("공지사항 수정: courseId={}, noticeId={}, professorId={}", courseId, noticeId, professorId);
		
		String authorName = getAuthorName(notice.getCreatedBy());
		return CourseNoticeResponse.from(notice, authorName);
	}
	
	@Override
	@Transactional
	public void deleteNotice(Long courseId, Long noticeId, Long professorId) {
		Course course = findCourseById(courseId);
		validateProfessorPermission(course, professorId);
		
		CourseNotice notice = findNoticeByIdAndCourseId(noticeId, courseId);
		notice.delete();
		
		log.info("공지사항 삭제: courseId={}, noticeId={}, professorId={}", courseId, noticeId, professorId);
	}
	
	// === 댓글 CRUD ===
	
	@Override
	@Transactional
	public CourseNoticeCommentResponse createComment(Long courseId, Long noticeId, CourseNoticeCommentRequest request, Long userId) {
		validateCourseAccess(courseId, userId);
		
		CourseNotice notice = findNoticeByIdAndCourseId(noticeId, courseId);
		validateCommentsAllowed(notice);
		
		CourseNoticeComment comment = CourseNoticeComment.create(notice, request.getContent(), userId);
		courseNoticeCommentRepository.save(comment);
		
		log.info("댓글 작성: noticeId={}, commentId={}, userId={}", noticeId, comment.getId(), userId);
		
		String authorName = getAuthorName(userId);
		return CourseNoticeCommentResponse.from(comment, authorName);
	}
	
	@Override
	@Transactional
	public CourseNoticeCommentResponse createReply(Long courseId, Long noticeId, Long parentId, CourseNoticeCommentRequest request, Long userId) {
		validateCourseAccess(courseId, userId);
		
		CourseNotice notice = findNoticeByIdAndCourseId(noticeId, courseId);
		validateCommentsAllowed(notice);
		
		CourseNoticeComment parent = findCommentByIdAndNoticeId(parentId, noticeId);
		
		CourseNoticeComment reply = CourseNoticeComment.createReply(notice, parent, request.getContent(), userId);
		courseNoticeCommentRepository.save(reply);
		
		log.info("대댓글 작성: noticeId={}, parentId={}, replyId={}, userId={}", noticeId, parentId, reply.getId(), userId);
		
		String authorName = getAuthorName(userId);
		return CourseNoticeCommentResponse.from(reply, authorName);
	}
	
	@Override
	@Transactional
	public CourseNoticeCommentResponse updateComment(Long courseId, Long noticeId, Long commentId, CourseNoticeCommentRequest request, Long userId) {
		validateCourseAccess(courseId, userId);
		
		CourseNoticeComment comment = findCommentByIdAndNoticeId(commentId, noticeId);
		validateCommentAuthor(comment, userId);
		
		comment.update(request.getContent(), userId);
		
		log.info("댓글 수정: noticeId={}, commentId={}, userId={}", noticeId, commentId, userId);
		
		String authorName = getAuthorName(userId);
		return CourseNoticeCommentResponse.from(comment, authorName);
	}
	
	@Override
	@Transactional
	public void deleteComment(Long courseId, Long noticeId, Long commentId, Long userId) {
		validateCourseAccess(courseId, userId);
		
		CourseNoticeComment comment = findCommentByIdAndNoticeId(commentId, noticeId);
		validateCommentAuthor(comment, userId);
		
		comment.delete();
		
		log.info("댓글 삭제: noticeId={}, commentId={}, userId={}", noticeId, commentId, userId);
	}
	
	// === Private Helper Methods ===
	
	private Course findCourseById(Long courseId) {
		return courseRepository.findById(courseId)
				.orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다. courseId=" + courseId));
	}
	
	private CourseNotice findNoticeByIdAndCourseId(Long noticeId, Long courseId) {
		CourseNotice notice = courseNoticeRepository.findByIdAndNotDeleted(noticeId)
				.orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. noticeId=" + noticeId));
		
		if (!notice.belongsToCourse(courseId)) {
			throw new IllegalArgumentException("해당 강의의 공지사항이 아닙니다.");
		}
		
		return notice;
	}
	
	private CourseNoticeComment findCommentByIdAndNoticeId(Long commentId, Long noticeId) {
		CourseNoticeComment comment = courseNoticeCommentRepository.findByIdAndNotDeleted(commentId)
				.orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. commentId=" + commentId));
		
		if (!comment.belongsToNotice(noticeId)) {
			throw new IllegalArgumentException("해당 공지사항의 댓글이 아닙니다.");
		}
		
		return comment;
	}
	
	private void validateProfessorPermission(Course course, Long userId) {
		if (!course.getProfessor().getProfessorId().equals(userId)) {
			throw new IllegalArgumentException("해당 강의의 담당 교수만 접근 가능합니다.");
		}
	}
	
	private void validateCourseAccess(Long courseId, Long userId) {
		Course course = findCourseById(courseId);
		
		// 담당 교수인 경우 접근 허용
		if (course.getProfessor().getProfessorId().equals(userId)) {
			return;
		}
		
		// 수강생인 경우 접근 허용
		boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(userId, courseId);
		if (!isEnrolled) {
			throw new IllegalArgumentException("해당 강의의 수강생 또는 담당 교수만 접근 가능합니다.");
		}
	}
	
	private void validateCommentsAllowed(CourseNotice notice) {
		if (!notice.isCommentsAllowed()) {
			throw new IllegalArgumentException("해당 공지사항은 댓글이 허용되지 않습니다.");
		}
	}
	
	private void validateCommentAuthor(CourseNoticeComment comment, Long userId) {
		if (!comment.isAuthor(userId)) {
			throw new IllegalArgumentException("댓글 작성자만 수정/삭제할 수 있습니다.");
		}
	}
	
	private List<CourseNoticeCommentResponse> getCommentsWithReplies(Long noticeId) {
		List<CourseNoticeComment> rootComments = courseNoticeCommentRepository.findAllByNoticeIdWithChildren(noticeId);
		
		Set<Long> authorIds = rootComments.stream()
				.flatMap(c -> {
					List<Long> ids = new ArrayList<>();
					ids.add(c.getAuthorId());
					c.getChildren().stream()
							.filter(child -> !child.isDeleted())
							.forEach(child -> ids.add(child.getAuthorId()));
					return ids.stream();
				})
				.collect(Collectors.toSet());
		
		Map<Long, UserBasicInfoDto> userInfoMap = userInfoCacheService.getUserInfoMap(authorIds);
		
		return rootComments.stream()
				.map(comment -> {
					String authorName = getAuthorNameFromMap(userInfoMap, comment.getAuthorId());
					List<CourseNoticeCommentResponse> children = comment.getChildren().stream()
							.filter(child -> !child.isDeleted())
							.map(child -> {
								String childAuthorName = getAuthorNameFromMap(userInfoMap, child.getAuthorId());
								return CourseNoticeCommentResponse.from(child, childAuthorName);
							})
							.collect(Collectors.toList());
					return CourseNoticeCommentResponse.from(comment, authorName, children);
				})
				.collect(Collectors.toList());
	}
	
	private String getAuthorName(Long userId) {
		Map<Long, UserBasicInfoDto> userInfoMap = userInfoCacheService.getUserInfoMap(Set.of(userId));
		UserBasicInfoDto userInfo = userInfoMap.get(userId);
		return userInfo != null ? userInfo.getName() : "알 수 없음";
	}
	
	private String getAuthorNameFromMap(Map<Long, UserBasicInfoDto> userInfoMap, Long userId) {
		UserBasicInfoDto userInfo = userInfoMap.get(userId);
		return userInfo != null ? userInfo.getName() : "알 수 없음";
	}
}
