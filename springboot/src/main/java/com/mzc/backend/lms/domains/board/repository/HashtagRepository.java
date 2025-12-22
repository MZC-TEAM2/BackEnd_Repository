package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    boolean existsByNameAndIsActive(String name, boolean isActive);

    /**
     * 해시태그 이름으로 검색 (자동완성용)
     * - 활성화된 해시태그만 검색
     * - 이름에 키워드가 포함된 해시태그 반환
     * - 최대 10개 반환
     * - 이름 순 정렬
     */
    @Query("SELECT h FROM Hashtag h WHERE h.isActive = true AND h.name LIKE :keyword ORDER BY h.name ASC")
    List<Hashtag> searchActiveHashtagsByKeyword(@Param("keyword") String keyword);

    /**
     * 해시태그 이름으로 검색 (display_name 기준, 자동완성용)
     * - 활성화된 해시태그만 검색
     * - 표시 이름에 키워드가 포함된 해시태그 반환
     * - 최대 10개 반환
     * - 이름 순 정렬
     */
    @Query("SELECT h FROM Hashtag h WHERE h.isActive = true AND (h.name LIKE :keyword OR h.displayName LIKE :keyword) ORDER BY h.name ASC")
    List<Hashtag> searchActiveHashtagsByKeywordInBoth(@Param("keyword") String keyword);
}
