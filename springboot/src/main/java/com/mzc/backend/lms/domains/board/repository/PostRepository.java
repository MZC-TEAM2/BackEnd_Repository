package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * ID로 게시글 조회 (모든 연관관계 즉시 로딩)
     * 생성 직후 응답을 위해 사용 - 한 번의 쿼리로 모든 데이터 로드
     * 
     * 참고: comments는 제외 (MultipleBagFetchException 방지)
     * - 게시글 생성 직후에는 댓글이 없음
     * - 필요시 지연 로딩으로 조회됨
     */
    @EntityGraph(attributePaths = {"attachments", "postHashtags", "postHashtags.hashtag"})
    Optional<Post> findWithAllById(Long id);
    
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.isDeleted = false")
    List<Post> findByCategory(@Param("category") BoardCategory category);

    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.isDeleted = false")
    Page<Post> findByCategory(@Param("category") BoardCategory category, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title% AND p.isDeleted = false")
    Page<Post> findByTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.title LIKE %:title% AND p.isDeleted = false")
    Page<Post> findByCategoryAndTitleContaining(@Param("category") BoardCategory category, @Param("title") String title, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
           "JOIN p.postHashtags ph " +
           "JOIN ph.hashtag h " +
           "WHERE p.category = :category " +
           "AND LOWER(h.name) = LOWER(:hashtagName) " +
           "AND h.isActive = true " +
           "AND p.isDeleted = false " +
           "AND ph.isDeleted = false")
    Page<Post> findByCategoryAndHashtagName(@Param("category") BoardCategory category, 
                                             @Param("hashtagName") String hashtagName, 
                                             Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
           "JOIN p.postHashtags ph " +
           "JOIN ph.hashtag h " +
           "WHERE p.category = :category " +
           "AND p.title LIKE %:title% " +
           "AND LOWER(h.name) = LOWER(:hashtagName) " +
           "AND h.isActive = true " +
           "AND p.isDeleted = false " +
           "AND ph.isDeleted = false")
    Page<Post> findByCategoryAndTitleContainingAndHashtagName(@Param("category") BoardCategory category, 
                                                                @Param("title") String title, 
                                                                @Param("hashtagName") String hashtagName, 
                                                                Pageable pageable);

    /**
     * 게시글 상세 조회 (해시태그 포함, fetch join 사용)
     * attachments와 comments는 트랜잭션 내에서 자동 로드됨
     */
    @Query("SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.postHashtags ph " +
           "LEFT JOIN FETCH ph.hashtag h " +
           "WHERE p.id = :postId AND p.isDeleted = false")
    java.util.Optional<Post> findByIdWithHashtags(@Param("postId") Long postId);
}
