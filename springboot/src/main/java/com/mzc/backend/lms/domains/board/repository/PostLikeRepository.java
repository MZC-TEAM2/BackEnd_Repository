package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 좋아요 Repository
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 사용자의 특정 게시글 좋아요 여부 조회
     */
    @Query("SELECT pl FROM PostLike pl WHERE pl.user.id = :userId AND pl.post.id = :postId AND pl.likeType = 'POST'")
    Optional<PostLike> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 사용자의 특정 게시글 좋아요 여부 확인
     */
    @Query("SELECT COUNT(pl) > 0 FROM PostLike pl WHERE pl.user.id = :userId AND pl.post.id = :postId AND pl.likeType = 'POST'")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 사용자가 좋아요한 게시글 ID 목록 조회
     */
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId AND pl.post.id IN :postIds AND pl.likeType = 'POST'")
    List<Long> findLikedPostIdsByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    /**
     * 게시글의 좋아요 수 카운트
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId AND pl.likeType = 'POST'")
    long countByPostId(@Param("postId") Long postId);

    /**
     * 게시글의 모든 좋아요 삭제 (게시글 삭제 시)
     */
    void deleteByPostId(Long postId);
}
