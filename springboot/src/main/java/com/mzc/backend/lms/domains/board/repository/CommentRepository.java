package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.Comment;
import com.mzc.backend.lms.domains.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * 특정 게시글의 댓글 목록 조회
     */
    List<Comment> findByPost(Post post);

    /**
     * 특정 부모 댓글의 대댓글 목록 조회
     */
    List<Comment> findByParentComment(Comment parentComment);
}
