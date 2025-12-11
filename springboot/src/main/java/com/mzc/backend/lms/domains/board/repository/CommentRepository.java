package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.Comment;
import com.mzc.backend.lms.domains.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.isDeleted = false")
    List<Comment> findByPost(Post post);

    @Query("SELECT c FROM Comment c WHERE c.parentComment = :parentComment AND c.isDeleted = false")
    List<Comment> findByParentComment(Comment parentComment);
}
