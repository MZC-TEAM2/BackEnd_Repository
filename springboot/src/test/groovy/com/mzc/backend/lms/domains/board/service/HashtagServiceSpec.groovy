package com.mzc.backend.lms.domains.board.service

import com.mzc.backend.lms.domains.board.entity.Hashtag
import com.mzc.backend.lms.domains.board.entity.Post
import com.mzc.backend.lms.domains.board.repository.HashtagRepository
import jakarta.persistence.EntityManager
import spock.lang.Specification
import spock.lang.Subject

/**
 * HashtagService 테스트
 * 해시태그 생성, 조회, 게시글 연결 기능 테스트
 */
class HashtagServiceSpec extends Specification {

    def hashtagRepository = Mock(HashtagRepository)
    def entityManager = Mock(EntityManager)

    @Subject
    def hashtagService = new HashtagService(hashtagRepository, entityManager)

    def "기존 해시태그가 있으면 조회하여 반환한다"() {
        given: "이미 존재하는 해시태그"
        def tagName = "Java"
        def userId = 1L
        def existingHashtag = Mock(Hashtag) {
            getId() >> 1L
            getName() >> "java"
        }
        hashtagRepository.findByName("java") >> Optional.of(existingHashtag)

        when: "해시태그를 조회/생성하면"
        def result = hashtagService.getOrCreateHashtag(tagName, userId)

        then: "기존 해시태그가 반환된다"
        result == existingHashtag

        and: "새로 저장하지 않는다"
        0 * hashtagRepository.save(_)
    }

    def "기존 해시태그가 없으면 새로 생성한다"() {
        given: "존재하지 않는 해시태그"
        def tagName = "Spring"
        def userId = 1L
        hashtagRepository.findByName("spring") >> Optional.empty()
        hashtagRepository.save(_) >> { Hashtag h -> h }

        when: "해시태그를 조회/생성하면"
        def result = hashtagService.getOrCreateHashtag(tagName, userId)

        then: "새 해시태그가 저장된다"
        1 * hashtagRepository.save(_) >> { Hashtag h ->
            assert h.name == "spring"
            assert h.displayName == "Spring"
            h
        }
    }

    def "빈 해시태그 이름은 예외를 발생시킨다"() {
        when: "빈 이름으로 해시태그 생성을 시도하면"
        hashtagService.getOrCreateHashtag(tagName, 1L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)

        where:
        tagName << [null, "", "   "]
    }

    def "게시글에 해시태그 목록을 연결한다"() {
        given: "게시글과 해시태그 목록"
        def post = Mock(Post) {
            getId() >> 1L
        }
        def tagNames = ["Java", "Spring"]
        def userId = 1L

        hashtagRepository.findByName("java") >> Optional.empty()
        hashtagRepository.findByName("spring") >> Optional.empty()
        hashtagRepository.save(_) >> { Hashtag h -> h }

        when: "해시태그를 연결하면"
        hashtagService.attachHashtagsToPost(post, tagNames, userId)

        then: "각 해시태그가 게시글에 추가된다"
        2 * post.addHashtag(_, userId)
    }

    def "빈 해시태그 목록은 처리하지 않는다"() {
        given: "게시글과 빈 해시태그 목록"
        def post = Mock(Post)

        when: "빈 목록으로 연결을 시도하면"
        hashtagService.attachHashtagsToPost(post, tagNames, 1L)

        then: "아무 동작도 하지 않는다"
        0 * post.addHashtag(_, _)
        0 * hashtagRepository.save(_)

        where:
        tagNames << [null, [], ["", "  "]]
    }

    def "중복된 해시태그는 하나만 연결된다"() {
        given: "중복된 해시태그 목록"
        def post = Mock(Post) {
            getId() >> 1L
        }
        def tagNames = ["Java", "java", "JAVA"]
        def userId = 1L

        hashtagRepository.findByName("java") >> Optional.empty()
        hashtagRepository.save(_) >> { Hashtag h -> h }

        when: "해시태그를 연결하면"
        hashtagService.attachHashtagsToPost(post, tagNames, userId)

        then: "중복 제거 후 하나만 연결된다"
        1 * post.addHashtag(_, userId)
    }

    def "null 게시글에 해시태그 연결 시 예외가 발생한다"() {
        when: "null 게시글에 해시태그 연결을 시도하면"
        hashtagService.attachHashtagsToPost(null, ["Java"], 1L)

        then: "예외가 발생한다"
        thrown(IllegalArgumentException)
    }

    def "게시글의 해시태그를 업데이트한다"() {
        given: "게시글과 새 해시태그 목록"
        def post = Mock(Post) {
            getId() >> 1L
        }
        def newTagNames = ["NewTag"]
        def userId = 1L

        hashtagRepository.findByName("newtag") >> Optional.empty()
        hashtagRepository.save(_) >> { Hashtag h -> h }

        when: "해시태그를 업데이트하면"
        hashtagService.updatePostHashtags(post, newTagNames, userId)

        then: "기존 해시태그가 제거된다"
        1 * post.clearHashtags()

        and: "변경사항이 flush된다"
        1 * entityManager.flush()

        and: "새 해시태그가 추가된다"
        1 * post.addHashtag(_, userId)
    }

    def "빈 해시태그 목록으로 업데이트하면 모든 해시태그가 제거된다"() {
        given: "게시글"
        def post = Mock(Post) {
            getId() >> 1L
        }

        when: "빈 목록으로 업데이트하면"
        hashtagService.updatePostHashtags(post, null, 1L)

        then: "기존 해시태그가 제거된다"
        1 * post.clearHashtags()

        and: "새 해시태그는 추가되지 않는다"
        0 * post.addHashtag(_, _)
    }

    def "해시태그 검색 - 키워드로 검색한다"() {
        given: "검색 키워드와 결과"
        def keyword = "java"
        def hashtag1 = Mock(Hashtag)
        def hashtag2 = Mock(Hashtag)
        hashtagRepository.searchActiveHashtagsByKeywordInBoth("%java%") >> [hashtag1, hashtag2]

        when: "해시태그를 검색하면"
        def result = hashtagService.searchHashtags(keyword)

        then: "결과가 반환된다"
        result.size() == 2
    }

    def "해시태그 검색 - 빈 키워드는 빈 결과를 반환한다"() {
        when: "빈 키워드로 검색하면"
        def result = hashtagService.searchHashtags(keyword)

        then: "빈 결과가 반환된다"
        result.isEmpty()

        and: "Repository 호출이 없다"
        0 * hashtagRepository.searchActiveHashtagsByKeywordInBoth(_)

        where:
        keyword << [null, "", "   "]
    }

    def "해시태그 검색 - 최대 10개로 제한된다"() {
        given: "10개 이상의 검색 결과"
        def keyword = "test"
        def manyHashtags = (1..15).collect { Mock(Hashtag) }
        hashtagRepository.searchActiveHashtagsByKeywordInBoth("%test%") >> manyHashtags

        when: "해시태그를 검색하면"
        def result = hashtagService.searchHashtags(keyword)

        then: "최대 10개만 반환된다"
        result.size() == 10
    }
}
