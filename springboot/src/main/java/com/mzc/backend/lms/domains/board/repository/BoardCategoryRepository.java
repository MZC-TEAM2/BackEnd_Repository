package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
    /**
     * 게시판 유형으로 카테고리 조회
     */
    Optional<BoardCategory> findByBoardType(BoardType boardType);
}
