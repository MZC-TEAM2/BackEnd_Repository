package com.mzc.backend.lms.domains.board.service

import com.mzc.backend.lms.domains.board.dto.request.PostCreateRequestDto
import com.mzc.backend.lms.domains.board.entity.BoardCategory
import com.mzc.backend.lms.domains.board.entity.Post
import com.mzc.backend.lms.domains.board.entity.PostLike
import com.mzc.backend.lms.domains.board.enums.BoardType
import com.mzc.backend.lms.domains.board.enums.PostType
import com.mzc.backend.lms.domains.board.exception.BoardException
import com.mzc.backend.lms.domains.board.repository.*
import com.mzc.backend.lms.domains.user.professor.repository.ProfessorDepartmentRepository
import com.mzc.backend.lms.domains.user.profile.dto.UserBasicInfoDto
import com.mzc.backend.lms.domains.user.profile.service.UserInfoCacheService
import com.mzc.backend.lms.domains.user.student.repository.StudentDepartmentRepository
import com.mzc.backend.lms.domains.user.user.entity.User
import com.mzc.backend.lms.domains.user.user.repository.UserRepository
import com.mzc.backend.lms.util.file.FileUploadUtils
import jakarta.persistence.EntityManager
import spock.lang.Specification
import spock.lang.Subject

/**
 * PostService 테스트
 * 게시글 CRUD, 좋아요, 권한 검증 기능 테스트
 */
class PostServiceSpec extends Specification {

    def postRepository = Mock(PostRepository)
    def boardCategoryRepository = Mock(BoardCategoryRepository)
    def postLikeRepository = Mock(PostLikeRepository)
    def userRepository = Mock(UserRepository)
    def userTypeQueryRepository = Mock(UserTypeQueryRepository)
    def userInfoCacheService = Mock(UserInfoCacheService)
    def fileStorageService = Mock(FileUploadUtils)
    def attachmentRepository = Mock(AttachmentRepository)
    def hashtagService = Mock(HashtagService)
    def entityManager = Mock(EntityManager)
    def studentDepartmentRepository = Mock(StudentDepartmentRepository)
    def professorDepartmentRepository = Mock(ProfessorDepartmentRepository)

    @Subject
    def postService = new PostService(
            postRepository,
            boardCategoryRepository,
            postLikeRepository,
            userRepository,
            userTypeQueryRepository,
            userInfoCacheService,
            fileStorageService,
            attachmentRepository,
            hashtagService,
            entityManager,
            studentDepartmentRepository,
            professorDepartmentRepository
    )

    def "게시글을 조회한다"() {
        given: "존재하는 게시글"
        def postId = 1L
        def category = Mock(BoardCategory) {
            getBoardType() >> BoardType.FREE
        }
        def post = Mock(Post) {
            getId() >> postId
            getCategory() >> category
            isDeleted() >> false
            getAuthorId() >> 1L
            getTitle() >> "테스트 제목"
            getContent() >> "테스트 내용"
            getViewCount() >> 0
            getLikeCount() >> 0
            getPostHashtags() >> []
            getAttachments() >> []
        }

        postRepository.findByIdWithHashtags(postId) >> Optional.of(post)
        userInfoCacheService.getUserInfoMap(_) >> [1L: new UserBasicInfoDto(1L, "테스터", null, "STUDENT")]

        when: "게시글을 조회하면"
        def result = postService.getPost("FREE", postId, 1L)

        then: "조회수가 증가한다"
        1 * post.increaseViewCount()

        and: "게시글 정보가 반환된다"
        result != null
    }

    def "존재하지 않는 게시글 조회 시 예외가 발생한다"() {
        given: "존재하지 않는 게시글 ID"
        postRepository.findByIdWithHashtags(999L) >> Optional.empty()

        when: "게시글을 조회하면"
        postService.getPost("FREE", 999L, 1L)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "삭제된 게시글 조회 시 예외가 발생한다"() {
        given: "삭제된 게시글"
        def post = Mock(Post) {
            isDeleted() >> true
        }
        postRepository.findByIdWithHashtags(1L) >> Optional.of(post)

        when: "게시글을 조회하면"
        postService.getPost("FREE", 1L, 1L)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "게시글을 삭제한다 (Soft Delete)"() {
        given: "삭제할 게시글"
        def postId = 1L
        def post = Mock(Post) {
            getId() >> postId
            isDeleted() >> false
        }
        postRepository.findById(postId) >> Optional.of(post)

        when: "게시글을 삭제하면"
        postService.deletePost(postId)

        then: "첨부파일이 삭제된다"
        1 * fileStorageService.deletePostFiles(post)

        and: "게시글이 soft delete 된다"
        1 * post.delete()
    }

    def "이미 삭제된 게시글은 다시 삭제할 수 없다"() {
        given: "이미 삭제된 게시글"
        def post = Mock(Post) {
            isDeleted() >> true
        }
        postRepository.findById(1L) >> Optional.of(post)

        when: "삭제를 시도하면"
        postService.deletePost(1L)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "게시글 좋아요를 추가한다"() {
        given: "좋아요하지 않은 게시글"
        def postId = 1L
        def userId = 100L
        def post = Mock(Post) {
            getId() >> postId
        }
        def user = Mock(User) {
            getId() >> userId
        }

        postRepository.findById(postId) >> Optional.of(post)
        userRepository.findById(userId) >> Optional.of(user)
        postLikeRepository.findByUserIdAndPostId(userId, postId) >> Optional.empty()

        when: "좋아요를 토글하면"
        def result = postService.toggleLike(postId, userId)

        then: "좋아요가 추가된다"
        1 * postLikeRepository.save(_)
        1 * post.increaseLikeCount()
        result == true
    }

    def "게시글 좋아요를 취소한다"() {
        given: "이미 좋아요한 게시글"
        def postId = 1L
        def userId = 100L
        def post = Mock(Post) {
            getId() >> postId
        }
        def user = Mock(User) {
            getId() >> userId
        }
        def existingLike = Mock(PostLike)

        postRepository.findById(postId) >> Optional.of(post)
        userRepository.findById(userId) >> Optional.of(user)
        postLikeRepository.findByUserIdAndPostId(userId, postId) >> Optional.of(existingLike)

        when: "좋아요를 토글하면"
        def result = postService.toggleLike(postId, userId)

        then: "좋아요가 삭제된다"
        1 * postLikeRepository.delete(existingLike)
        1 * post.decreaseLikeCount()
        result == false
    }

    def "사용자의 좋아요 여부를 확인한다"() {
        given: "게시글과 사용자"
        def postId = 1L
        def userId = 100L
        postLikeRepository.existsByUserIdAndPostId(userId, postId) >> isLiked

        when: "좋아요 여부를 확인하면"
        def result = postService.isLikedByUser(postId, userId)

        then: "정확한 결과를 반환한다"
        result == isLiked

        where:
        isLiked << [true, false]
    }

    def "잘못된 게시판 타입으로 조회 시 예외가 발생한다"() {
        given: "잘못된 게시판 타입"
        def invalidBoardType = "INVALID_TYPE"

        when: "게시글을 조회하면"
        postService.getPost(invalidBoardType, 1L, 1L)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "게시글이 해당 게시판에 속하지 않으면 예외가 발생한다"() {
        given: "다른 게시판의 게시글"
        def postId = 1L
        def category = Mock(BoardCategory) {
            getBoardType() >> BoardType.PROFESSOR
        }
        def post = Mock(Post) {
            getId() >> postId
            getCategory() >> category
            isDeleted() >> false
        }

        postRepository.findByIdWithHashtags(postId) >> Optional.of(post)

        when: "다른 게시판 타입으로 조회하면"
        postService.getPost("FREE", postId, 1L)

        then: "예외가 발생한다"
        thrown(BoardException)
    }
}
