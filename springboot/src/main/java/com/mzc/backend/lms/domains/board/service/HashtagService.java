package com.mzc.backend.lms.domains.board.service;

import com.mzc.backend.lms.domains.board.entity.Hashtag;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.repository.HashtagRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 해시태그 서비스
 * 게시글 작성 시 해시태그 생성 및 연결 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final EntityManager entityManager;

    /**
     * 해시태그 이름으로 조회 또는 생성
     * - 이미 존재하면 기존 해시태그 반환
     * - 없으면 새로 생성하여 반환
     * 
     * @param tagName 해시태그 이름 (예: "Java", "Spring")
     * @param userId 생성자 ID
     * @return Hashtag 엔티티
     */
    @Transactional
    public Hashtag getOrCreateHashtag(String tagName, Long userId) {
        if (tagName == null || tagName.isBlank()) {
            throw new IllegalArgumentException("해시태그 이름은 필수입니다.");
        }

        // 소문자로 변환하여 검색 (name은 소문자로 저장됨)
        String normalizedName = tagName.trim().toLowerCase();

        // 기존 해시태그 조회
        return hashtagRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    Hashtag newHashtag = Hashtag.builder()
                            .name(normalizedName)
                            .displayName(tagName.trim()) // 원본 케이스 유지
                            .color("#007bff") // 기본 색상
                            .tagCategory(null) // 자동 분류는 추후 구현
                            .createdBy(userId)
                            .build();

                    Hashtag saved = hashtagRepository.save(newHashtag);
                    log.info("✅ 새 해시태그 생성: id={}, name={}", saved.getId(), saved.getName());
                    return saved;
                });
    }

    /**
     * 게시글에 해시태그 목록 연결
     * 
     * @param post 게시글 엔티티
     * @param tagNames 해시태그 이름 리스트 (예: ["Java", "Spring", "JPA"])
     * @param userId 생성자 ID
     */
    @Transactional
    public void attachHashtagsToPost(Post post, List<String> tagNames, Long userId) {
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다.");
        }

        if (tagNames == null || tagNames.isEmpty()) {
            return; // 해시태그가 없으면 처리 안 함
        }

        // 중복 제거 및 빈 문자열 필터링 (소문자 변환 후 중복 제거)
        List<String> uniqueTagNames = tagNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.trim().toLowerCase()) // 소문자 변환
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        if (uniqueTagNames.isEmpty()) {
            return;
        }

        // 각 해시태그를 조회/생성 후 게시글에 연결
        for (String tagName : uniqueTagNames) {
            Hashtag hashtag = getOrCreateHashtag(tagName, userId);
            post.addHashtag(hashtag, userId);
        }

        log.info("게시글에 해시태그 {}개 연결: postId={}, tags={}", 
                uniqueTagNames.size(), post.getId(), uniqueTagNames);
    }

    /**
     * 게시글의 해시태그 업데이트
     * - 기존 해시태그를 모두 제거하고 새 해시태그로 교체
     * 
     * @param post 게시글 엔티티
     * @param tagNames 새 해시태그 이름 리스트
     * @param userId 생성자 ID
     */
    @Transactional
    public void updatePostHashtags(Post post, List<String> tagNames, Long userId) {
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다.");
        }

        // 기존 해시태그 제거
        post.clearHashtags();
        
        // 변경사항을 데이터베이스에 즉시 반영 (orphanRemoval 작동)
        entityManager.flush();

        // 새 해시태그 추가
        if (tagNames != null && !tagNames.isEmpty()) {
            attachHashtagsToPost(post, tagNames, userId);
        }

        log.info("게시글 해시태그 업데이트 완료: postId={}, newTagCount={}", 
                post.getId(), tagNames != null ? tagNames.size() : 0);
    }

    /**
     * 해시태그 검색 (자동완성용)
     * - 활성화된 해시태그 중 키워드가 포함된 항목 반환
     * - name 또는 displayName에서 검색
     * - 최대 10개 반환
     * 
     * @param keyword 검색 키워드
     * @return 해시태그 리스트
     */
    public List<Hashtag> searchHashtags(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        // LIKE 패턴 생성: %keyword%
        String likePattern = "%" + keyword.trim().toLowerCase() + "%";
        
        log.info("해시태그 검색: keyword={}, pattern={}", keyword, likePattern);
        
        // name과 displayName 모두에서 검색
        List<Hashtag> results = hashtagRepository
                .searchActiveHashtagsByKeywordInBoth(likePattern);

        // 최대 10개로 제한
        if (results.size() > 10) {
            results = results.subList(0, 10);
        }

        log.info("해시태그 검색 결과: keyword={}, resultCount={}", keyword, results.size());
        return results;
    }
}
