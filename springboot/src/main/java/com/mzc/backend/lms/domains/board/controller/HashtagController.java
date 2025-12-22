package com.mzc.backend.lms.domains.board.controller;

import com.mzc.backend.lms.domains.board.dto.response.HashtagSearchResponseDto;
import com.mzc.backend.lms.domains.board.entity.Hashtag;
import com.mzc.backend.lms.domains.board.service.HashtagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 해시태그 컨트롤러
 * 해시태그 검색 및 자동완성 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hashtags")
@RequiredArgsConstructor
public class HashtagController {

    private final HashtagService hashtagService;

    /**
     * 해시태그 검색 (자동완성용)
     * 
     * @param keyword 검색 키워드
     * @return 해시태그 검색 결과 리스트 (최대 10개)
     */
    @GetMapping("/search")
    public ResponseEntity<List<HashtagSearchResponseDto>> searchHashtags(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        
        log.info("해시태그 검색 API 호출: keyword={}", keyword);
        
        List<Hashtag> hashtags = hashtagService.searchHashtags(keyword);
        
        List<HashtagSearchResponseDto> response = hashtags.stream()
                .map(HashtagSearchResponseDto::from)
                .collect(Collectors.toList());
        
        log.info("해시태그 검색 결과: count={}", response.size());
        
        return ResponseEntity.ok(response);
    }
}
