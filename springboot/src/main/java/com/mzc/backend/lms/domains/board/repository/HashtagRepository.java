package com.mzc.backend.lms.domains.board.repository;

import com.mzc.backend.lms.domains.board.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    boolean existsByNameAndIsActive(String name, boolean isActive);
}
