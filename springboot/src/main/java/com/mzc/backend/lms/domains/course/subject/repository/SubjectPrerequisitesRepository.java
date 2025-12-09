package com.mzc.backend.lms.domains.course.subject.repository;

import com.mzc.backend.lms.domains.course.subject.entity.Subject;
import com.mzc.backend.lms.domains.course.subject.entity.SubjectPrerequisites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectPrerequisitesRepository extends JpaRepository<SubjectPrerequisites, Long> {
    /**
     * 과목으로 선수과목 조회
     */
    Optional<SubjectPrerequisites> findBySubject(Subject subject);
    /**
     * 과목 ID로 선수과목 존재 여부 확인
     */
    boolean existsBySubject(Subject subject);

    /**
     * 과목 ID로 선수과목 목록 조회
     */
    List<SubjectPrerequisites> findBySubjectId(Long subjectId);

    /**
     * 선수과목으로 선수과목 목록 조회
     */
    List<SubjectPrerequisites> findByPrerequisiteSubject(Subject prerequisiteSubject);

    /**
     * 선수과목 ID로 선수과목 목록 조회
     */

    List<SubjectPrerequisites> findByPrerequisiteSubjectId(Long prerequisiteSubjectId);
}
