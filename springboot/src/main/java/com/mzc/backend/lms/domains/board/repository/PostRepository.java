package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.isDeleted = false")
    List<Post> findByCategory(@Param("category") BoardCategory category);

    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.isDeleted = false")
    Page<Post> findByCategory(@Param("category") BoardCategory category, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title% AND p.isDeleted = false")
    Page<Post> findByTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.title LIKE %:title% AND p.isDeleted = false")
    Page<Post> findByCategoryAndTitleContaining(@Param("category") BoardCategory category, @Param("title") String title, Pageable pageable);
}
