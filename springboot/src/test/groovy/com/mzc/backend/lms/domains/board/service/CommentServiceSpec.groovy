package com.mzc.backend.lms.domains.board.service

import com.mzc.backend.lms.domains.board.dto.request.CommentCreateRequestDto
import com.mzc.backend.lms.domains.board.dto.request.CommentUpdateRequestDto
import com.mzc.backend.lms.domains.board.entity.Attachment
import com.mzc.backend.lms.domains.board.entity.BoardCategory
import com.mzc.backend.lms.domains.board.entity.Comment
import com.mzc.backend.lms.domains.board.entity.Post
import com.mzc.backend.lms.domains.board.exception.BoardException
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository
import com.mzc.backend.lms.domains.board.repository.CommentRepository
import com.mzc.backend.lms.domains.board.repository.PostRepository
import com.mzc.backend.lms.domains.user.profile.dto.UserBasicInfoDto
import com.mzc.backend.lms.domains.user.profile.service.UserInfoCacheService
import spock.lang.Specification
import spock.lang.Subject

/**
 * CommentService 테스트
 * 댓글 CRUD 및 정책 검증 테스트
 */
class CommentServiceSpec extends Specification {

    def commentRepository = Mock(CommentRepository)
    def postRepository = Mock(PostRepository)
    def attachmentRepository = Mock(AttachmentRepository)
    def userInfoCacheService = Mock(UserInfoCacheService)

    @Subject
    def commentService = new CommentService(
            commentRepository,
            postRepository,
            attachmentRepository,
            userInfoCacheService
    )

    def "댓글을 생성한다"() {
        given: "게시글과 댓글 생성 요청"
        def postId = 1L
        def authorId = 100L
        def category = Mock(BoardCategory) {
            isAllowComments() >> true
        }
        def post = Mock(Post) {
            getId() >> postId
            getCategory() >> category
            getAuthorId() >> 200L
        }
        def request = Mock(CommentCreateRequestDto) {
            getPostId() >> postId
            getContent() >> "테스트 댓글"
            getAuthorId() >> authorId
            getParentCommentId() >> null
            getAttachmentIds() >> null
        }

        def savedComment = Mock(Comment) {
            getId() >> 1L
            getPost() >> post
            getContent() >> "테스트 댓글"
            getAuthorId() >> authorId
            getDepth() >> 0
            isDeletedByAdmin() >> false
            getCreatedAt() >> null
            getUpdatedAt() >> null
            getParentComment() >> null
            getChildComments() >> []
            getAttachments() >> []
        }

        postRepository.findById(postId) >> Optional.of(post)
        userInfoCacheService.getUserInfoMap(_) >> [:]

        when: "댓글을 생성하면"
        def result = commentService.createComment(request)

        then: "댓글이 저장된다"
        1 * commentRepository.save(_) >> savedComment
        result != null
    }

    def "댓글이 허용되지 않은 게시판에서는 예외가 발생한다"() {
        given: "댓글이 비허용된 게시판"
        def postId = 1L
        def category = Mock(BoardCategory) {
            isAllowComments() >> false
        }
        def post = Mock(Post) {
            getId() >> postId
            getCategory() >> category
        }
        def request = Mock(CommentCreateRequestDto) {
            getPostId() >> postId
            getParentCommentId() >> null
        }

        postRepository.findById(postId) >> Optional.of(post)

        when: "댓글을 생성하면"
        commentService.createComment(request)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "대댓글을 생성한다"() {
        given: "부모 댓글이 있는 대댓글 요청"
        def postId = 1L
        def parentCommentId = 10L
        def authorId = 100L
        def category = Mock(BoardCategory) {
            isAllowComments() >> true
        }
        def post = Mock(Post) {
            getId() >> postId
            getCategory() >> category
            getAuthorId() >> 200L
        }
        def parentComment = Mock(Comment) {
            getId() >> parentCommentId
            getDepth() >> 0
            getAuthorId() >> 99L
        }
        def request = Mock(CommentCreateRequestDto) {
            getPostId() >> postId
            getContent() >> "대댓글"
            getAuthorId() >> authorId
            getParentCommentId() >> parentCommentId
            getAttachmentIds() >> null
        }

        def savedComment = Mock(Comment) {
            getId() >> 2L
            getPost() >> post
            getContent() >> "대댓글"
            getAuthorId() >> authorId
            getDepth() >> 1
            isDeletedByAdmin() >> false
            getCreatedAt() >> null
            getUpdatedAt() >> null
            getParentComment() >> parentComment
            getChildComments() >> []
            getAttachments() >> []
        }

        postRepository.findById(postId) >> Optional.of(post)
        commentRepository.findById(parentCommentId) >> Optional.of(parentComment)
        userInfoCacheService.getUserInfoMap(_) >> [:]

        when: "대댓글을 생성하면"
        def result = commentService.createComment(request)

        then: "댓글이 저장된다"
        1 * commentRepository.save(_) >> savedComment
    }

    def "대댓글의 대댓글은 생성할 수 없다 (깊이 제한)"() {
        given: "깊이가 1인 부모 댓글"
        def postId = 1L
        def parentCommentId = 10L
        def category = Mock(BoardCategory) {
            isAllowComments() >> true
        }
        def post = Mock(Post) {
            getId() >> postId
            getCategory() >> category
        }
        def parentComment = Mock(Comment) {
            getId() >> parentCommentId
            getDepth() >> 1
        }
        def request = Mock(CommentCreateRequestDto) {
            getPostId() >> postId
            getParentCommentId() >> parentCommentId
        }

        postRepository.findById(postId) >> Optional.of(post)
        commentRepository.findById(parentCommentId) >> Optional.of(parentComment)

        when: "대댓글의 대댓글을 생성하면"
        commentService.createComment(request)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "존재하지 않는 게시글에 댓글 생성 시 예외가 발생한다"() {
        given: "존재하지 않는 게시글 ID"
        def request = Mock(CommentCreateRequestDto) {
            getPostId() >> 999L
        }
        postRepository.findById(999L) >> Optional.empty()

        when: "댓글을 생성하면"
        commentService.createComment(request)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "게시글의 모든 댓글을 조회한다"() {
        given: "댓글이 있는 게시글"
        def postId = 1L
        def post = Mock(Post) {
            getId() >> postId
            getAuthorId() >> 100L
        }
        def comment1 = Mock(Comment) {
            getId() >> 1L
            getPost() >> post
            getParentComment() >> null
            getContent() >> "댓글1"
            getAuthorId() >> 1L
            getDepth() >> 0
            isDeletedByAdmin() >> false
            getCreatedAt() >> null
            getUpdatedAt() >> null
            getAttachments() >> []
            getChildComments() >> []
            isDeleted() >> false
        }
        def comment2 = Mock(Comment) {
            getId() >> 2L
            getPost() >> post
            getParentComment() >> null
            getContent() >> "댓글2"
            getAuthorId() >> 2L
            getDepth() >> 0
            isDeletedByAdmin() >> false
            getCreatedAt() >> null
            getUpdatedAt() >> null
            getAttachments() >> []
            getChildComments() >> []
            isDeleted() >> false
        }

        postRepository.findById(postId) >> Optional.of(post)
        commentRepository.findByPost(post) >> [comment1, comment2]

        when: "댓글을 조회하면"
        def result = commentService.getCommentsByPost(postId)

        then: "최상위 댓글만 반환된다"
        result.size() == 2
    }

    def "댓글을 수정한다"() {
        given: "수정할 댓글"
        def commentId = 1L
        def updatedBy = 100L
        def comment = Mock(Comment) {
            getId() >> commentId
            isDeleted() >> false
            getAttachments() >> new ArrayList()
            getAuthorId() >> updatedBy
            getContent() >> "수정된 댓글"
            getParentComment() >> null
            getChildComments() >> []
            getPost() >> Mock(Post) { getId() >> 1L; getAuthorId() >> 200L }
        }
        def request = Mock(CommentUpdateRequestDto) {
            getContent() >> "수정된 댓글"
            getRemovedAttachmentIds() >> null
            getAttachmentIds() >> null
        }

        commentRepository.findById(commentId) >> Optional.of(comment)

        when: "댓글을 수정하면"
        commentService.updateComment(commentId, request, updatedBy)

        then: "댓글 내용이 업데이트된다"
        1 * comment.updateContent("수정된 댓글")
    }

    def "삭제된 댓글은 수정할 수 없다"() {
        given: "삭제된 댓글"
        def commentId = 1L
        def comment = Mock(Comment) {
            isDeleted() >> true
        }
        def request = Mock(CommentUpdateRequestDto)

        commentRepository.findById(commentId) >> Optional.of(comment)

        when: "삭제된 댓글을 수정하면"
        commentService.updateComment(commentId, request, 1L)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "댓글을 삭제한다 (본인만 가능)"() {
        given: "삭제할 댓글"
        def commentId = 1L
        def authorId = 100L
        def comment = Mock(Comment) {
            getId() >> commentId
            getAuthorId() >> authorId
            isDeleted() >> false
        }

        commentRepository.findById(commentId) >> Optional.of(comment)

        when: "본인이 댓글을 삭제하면"
        commentService.deleteComment(commentId, authorId)

        then: "댓글이 삭제된다"
        1 * comment.delete()
    }

    def "다른 사용자의 댓글은 삭제할 수 없다"() {
        given: "다른 사용자의 댓글"
        def commentId = 1L
        def authorId = 100L
        def deletedBy = 200L
        def comment = Mock(Comment) {
            getId() >> commentId
            getAuthorId() >> authorId
            isDeleted() >> false
        }

        commentRepository.findById(commentId) >> Optional.of(comment)

        when: "다른 사용자가 댓글을 삭제하면"
        commentService.deleteComment(commentId, deletedBy)

        then: "예외가 발생한다"
        thrown(BoardException)
    }

    def "이미 삭제된 댓글은 다시 삭제할 수 없다"() {
        given: "이미 삭제된 댓글"
        def commentId = 1L
        def comment = Mock(Comment) {
            isDeleted() >> true
        }

        commentRepository.findById(commentId) >> Optional.of(comment)

        when: "삭제를 시도하면"
        commentService.deleteComment(commentId, 1L)

        then: "예외가 발생한다"
        thrown(BoardException)
    }
}
