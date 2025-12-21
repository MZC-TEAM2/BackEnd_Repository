package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    /**
     * 특정 게시글의 첨부파일 목록 조회
     */
    List<Attachment> findByPost(Post post);
}
