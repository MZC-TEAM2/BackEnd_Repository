-- =====================================================
-- V7: 추가 게시판 게시글 더미데이터
-- V6 이후 실행 (STUDENT, CONTEST, CAREER, STUDY_RECRUITMENT 게시판 생성 이후)
-- =====================================================

-- 학생 게시판 게시글 (8개)
SET @student_category_id = (SELECT id FROM board_categories WHERE board_type = 'STUDENT' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@student_category_id, 20241001, '학업 고민 상담 받습니다', '학점 관리에 어려움을 겪고 있어요. 조언 부탁드립니다.', 'NORMAL', true, 145, 27, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@student_category_id, 20241001, '진로 상담 - IT 개발자', 'IT 분야로 진로를 고민 중입니다. 선배님들 조언 부탁드려요.', 'NORMAL', false, 189, 34, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@student_category_id, 20241001, '대외활동 추천해주세요', '스펙 쌓기 위한 대외활동 추천 부탁드립니다!', 'NORMAL', false, 167, 31, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),
(@student_category_id, 20241001, '학생회 활동 후기', '학생회 활동하면서 느낀 점을 공유합니다.', 'NORMAL', false, 98, 18, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@student_category_id, 20241001, '동아리 모집 - 코딩 동아리', '코딩 동아리에서 신입 부원을 모집합니다!', 'NORMAL', false, 234, 42, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),
(@student_category_id, 20241001, '복수전공 vs 부전공', '복수전공과 부전공 중 어떤 게 나을까요?', 'NORMAL', true, 178, 33, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),
(@student_category_id, 20241001, '교환학생 준비 팁', '교환학생 가려는데 준비 과정 공유합니다.', 'NORMAL', false, 156, 29, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),
(@student_category_id, 20241001, '학과 공부 잘하는 법', '전공 공부 효율적으로 하는 방법 공유해요!', 'NORMAL', false, 212, 39, false, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 20241001, 20241001);

-- 공모전 게시판 게시글 (8개)
SET @contest_category_id = (SELECT id FROM board_categories WHERE board_type = 'CONTEST' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@contest_category_id, 20241001, 'IT/소프트웨어 공모전 정보', '네이버 해커톤 공모전 일정 공유합니다!', 'NORMAL', false, 267, 48, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@contest_category_id, 20241001, '디자인 공모전 - UI/UX', 'UI/UX 디자인 공모전 참가자 모집', 'NORMAL', false, 198, 36, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@contest_category_id, 20241001, '마케팅 아이디어 공모전', '대학생 마케팅 공모전 정보 공유', 'NORMAL', false, 145, 27, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@contest_category_id, 20241001, '창의적 아이디어 공모전', '사회 문제 해결 아이디어 공모전', 'NORMAL', false, 234, 42, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@contest_category_id, 20241001, '스타트업 창업 경진대회', '대학생 창업 경진대회 참가 후기', 'NORMAL', false, 289, 52, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@contest_category_id, 20241001, '사회혁신 아이디어 공모전', '사회혁신 프로젝트 공모전 안내', 'NORMAL', false, 212, 39, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),
(@contest_category_id, 20241001, '앱 개발 공모전 팀원 모집', '앱 개발 공모전 함께 할 팀원 구합니다!', 'NORMAL', false, 178, 33, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),
(@contest_category_id, 20241001, '빅데이터 분석 공모전', '공공데이터 활용 아이디어 공모전', 'NORMAL', false, 256, 46, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001);

-- 취업정보 게시판 게시글 (10개)
SET @career_category_id = (SELECT id FROM board_categories WHERE board_type = 'CAREER' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@career_category_id, 20241001, 'IT 대기업 채용 공고', '삼성전자 SW 직군 채용 공고 공유합니다.', 'NORMAL', false, 345, 62, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@career_category_id, 20241001, '네이버 개발자 면접 후기', '네이버 신입 개발자 면접 경험 공유', 'NORMAL', false, 412, 74, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@career_category_id, 20241001, '인턴 경험 공유 - 스타트업', '스타트업 인턴 3개월 후기', 'NORMAL', false, 278, 50, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@career_category_id, 20241001, '자소서 첨삭 도와주세요', '자기소개서 첨삭 부탁드립니다.', 'NORMAL', true, 189, 34, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@career_category_id, 20241001, 'IT 포트폴리오 작성 팁', '개발자 포트폴리오 만드는 방법 공유', 'NORMAL', false, 456, 82, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@career_category_id, 20241001, '이력서 작성 가이드', '합격하는 이력서 작성법', 'NORMAL', false, 389, 70, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),
(@career_category_id, 20241001, '카카오 공채 면접 후기', '카카오 개발자 면접 경험담', 'NORMAL', false, 501, 90, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@career_category_id, 20241001, '외국계 IT 기업 채용', '구글 코리아 채용 공고 정보', 'NORMAL', false, 523, 94, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@career_category_id, 20241001, '인턴에서 정규직 전환', '인턴 정규직 전환 경험 공유합니다.', 'NORMAL', false, 312, 56, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@career_category_id, 20241001, 'IT 직무 자소서 예시', '합격한 자기소개서 예시 공유', 'NORMAL', false, 434, 78, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001);

-- 스터디모집 게시판 게시글 (8개)
SET @study_category_id = (SELECT id FROM board_categories WHERE board_type = 'STUDY_RECRUITMENT' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@study_category_id, 20241001, '코딩테스트 스터디 모집', '백준 알고리즘 스터디 함께 하실 분!', 'NORMAL', false, 267, 48, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@study_category_id, 20241001, '정보처리기사 자격증 스터디', '정보처리기사 함께 준비하실 분 모집', 'NORMAL', false, 198, 36, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@study_category_id, 20241001, 'React 프로젝트 스터디', 'React로 토이 프로젝트 만들 팀원 구합니다!', 'NORMAL', false, 234, 42, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@study_category_id, 20241001, '토익 900점 목표 스터디', 'TOEIC 900점 목표 스터디원 모집', 'NORMAL', false, 156, 29, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@study_category_id, 20241001, '운영체제 전공 스터디', '운영체제 전공 공부 스터디 모집합니다.', 'NORMAL', false, 189, 34, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@study_category_id, 20241001, 'Spring Boot 프로젝트', 'Spring Boot로 웹 서비스 만들기', 'NORMAL', false, 278, 50, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),
(@study_category_id, 20241001, '알고리즘 스터디 - 프로그래머스', '프로그래머스 Lv2 문제 풀이 스터디', 'NORMAL', false, 245, 44, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),
(@study_category_id, 20241001, 'AWS 자격증 준비 스터디', 'AWS SAA 자격증 함께 준비해요!', 'NORMAL', false, 212, 39, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001);

-- =====================================================
-- 게시글-해시태그 연결 (추가 게시판)
-- =====================================================

-- 학생 게시판 게시글 해시태그 연결
INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('학업고민')
WHERE p.title LIKE '%학업 고민%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('진로상담')
WHERE p.title LIKE '%진로 상담%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('대외활동')
WHERE p.title LIKE '%대외활동%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('학생회')
WHERE p.title LIKE '%학생회%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('동아리모집')
WHERE p.title LIKE '%동아리 모집%';

-- 공모전 게시판 게시글 해시태그 연결
INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('it/소프트웨어')
WHERE p.title LIKE '%IT/소프트웨어%' OR p.title LIKE '%앱 개발%' OR p.title LIKE '%빅데이터%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('디자인')
WHERE p.title LIKE '%디자인 공모전%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('마케팅')
WHERE p.title LIKE '%마케팅%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('아이디어')
WHERE p.title LIKE '%아이디어 공모전%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('창업')
WHERE p.title LIKE '%창업%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('사회혁신')
WHERE p.title LIKE '%사회혁신%';

-- 취업정보 게시판 게시글 해시태그 연결
INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('채용공고')
WHERE p.title LIKE '%채용 공고%' OR p.title LIKE '%채용%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('면접후기')
WHERE p.title LIKE '%면접 후기%' OR p.title LIKE '%면접 경험%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('인턴')
WHERE p.title LIKE '%인턴%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('자소서첨삭')
WHERE p.title LIKE '%자소서%' OR p.title LIKE '%자기소개서%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('포트폴리오')
WHERE p.title LIKE '%포트폴리오%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('이력서')
WHERE p.title LIKE '%이력서%';

-- 스터디모집 게시판 게시글 해시태그 연결
INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('코딩테스트')
WHERE p.title LIKE '%코딩테스트%' OR p.title LIKE '%알고리즘 스터디%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('자격증')
WHERE p.title LIKE '%자격증%' OR p.title LIKE '%정보처리기사%' OR p.title LIKE '%AWS%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('프로젝트')
WHERE p.title LIKE '%프로젝트%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('토익토스')
WHERE p.title LIKE '%토익%' OR p.title LIKE '%TOEIC%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('전공공부')
WHERE p.title LIKE '%전공 스터디%' OR p.title LIKE '%운영체제 전공%';
