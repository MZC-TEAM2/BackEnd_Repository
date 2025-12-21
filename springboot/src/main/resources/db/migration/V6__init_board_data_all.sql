-- =====================================================
-- V6: Board 더미 데이터 통합 파일
-- 원본: V5 + 구 V6 + V7 + V12 + V13 통합
-- board_categories.name 컬럼 참조 제거
-- =====================================================

-- =====================================================
-- 1. 관리자 사용자 생성 (게시글 작성자용)
-- =====================================================
INSERT IGNORE INTO users (id, email, password, created_at) VALUES
(20241001, 'admin@lms.ac.kr', '$2a$10$dummyHashedPassword123456789012345678901234567890', NOW());

INSERT IGNORE INTO user_type_mappings (user_id, user_type_id, assigned_at) VALUES
(20241001, 2, NOW());  -- 교수 타입

INSERT IGNORE INTO user_profiles (user_id, name, created_at) VALUES
(20241001, '관리자', NOW());

-- =====================================================
-- 2. 게시판 카테고리 초기화 데이터 (name 컬럼 제거됨)
-- =====================================================
INSERT INTO board_categories (board_type, allow_comments, allow_attachments, allow_anonymous, is_deleted, created_at, updated_at) VALUES
('NOTICE', true, true, false, false, NOW(), NOW()),
('FREE', true, true, true, false, NOW(), NOW()),
('QUESTION', true, true, true, false, NOW(), NOW()),
('DISCUSSION', true, true, false, false, NOW(), NOW()),
('DEPARTMENT', true, true, false, false, NOW(), NOW()),
('PROFESSOR', true, true, false, false, NOW(), NOW()),
('STUDENT', true, true, true, false, NOW(), NOW()),
('CONTEST', true, true, false, false, NOW(), NOW()),
('CAREER', true, true, true, false, NOW(), NOW()),
('ASSIGNMENT', false, true, false, false, NOW(), NOW()),
('EXAM', false, true, false, false, NOW(), NOW()),
('QUIZ', false, false, false, false, NOW(), NOW()),
('STUDY_RECRUITMENT', true, false, false, false, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at=NOW();

-- =====================================================
-- 3. 게시글 더미 데이터 (원본 V5)
-- =====================================================

SET @notice_category_id = (SELECT id FROM board_categories WHERE board_type = 'NOTICE' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@notice_category_id, 20241001, '[중요] 2025학년도 1학기 수강신청 일정 안내', '2025학년도 1학기 수강신청 일정을 다음과 같이 안내합니다.\n\n1차 수강신청: 2025년 1월 15일 ~ 1월 17일\n2차 수강신청: 2025년 1월 22일 ~ 1월 24일\n\n자세한 내용은 첨부파일을 참고하시기 바랍니다.', 'URGENT', false, 245, 12, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 2024학년도 2학기 기말고사 시험 일정', '2024학년도 2학기 기말고사 일정이 확정되었습니다.\n\n시험기간: 2024년 12월 16일 ~ 12월 20일\n성적 공개: 2024년 12월 27일\n\n수험생 여러분의 건승을 기원합니다.', 'NOTICE', false, 189, 8, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 동계 방학 도서관 운영시간 변경', '동계 방학 기간 중 도서관 운영시간이 다음과 같이 변경됩니다.\n\n평일: 09:00 ~ 18:00\n주말: 10:00 ~ 16:00\n\n2025년 1월 6일부터 적용됩니다.', 'NORMAL', false, 156, 5, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 학생 건강검진 실시 안내', '2024학년도 학생 건강검진을 다음과 같이 실시합니다.\n\n대상: 1학년, 3학년 재학생\n일시: 2025년 1월 13일 ~ 1월 17일\n장소: 학생회관 2층 보건소\n\n필수 참여 바랍니다.', 'URGENT', false, 312, 15, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 2025년도 등록금 납부 안내', '2025학년도 1학기 등록금 납부 기간을 안내합니다.\n\n납부기간: 2025년 1월 20일 ~ 2월 5일\n납부방법: 가상계좌 이체\n\n기한 내 납부하지 않을 시 제적될 수 있으니 유의하시기 바랍니다.', 'URGENT', false, 421, 23, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 졸업논문 제출 일정 공지', '2024학년도 졸업예정자 논문 제출 일정입니다.\n\n제출기한: 2025년 1월 31일까지\n제출처: 학과 사무실\n제출물: 논문 3부, 요약본 1부\n\n기한 엄수 바랍니다.', 'NORMAL', false, 198, 9, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 학사경고자 면담 일정 안내', '2024학년도 2학기 학사경고 대상자 면담 일정을 안내합니다.\n\n일시: 2025년 1월 8일 ~ 1월 10일\n장소: 학생상담센터\n\n해당 학생은 반드시 참석하시기 바랍니다.', 'URGENT', false, 167, 7, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 학생증 재발급 신청 안내', '분실 또는 훼손된 학생증 재발급 신청을 받습니다.\n\n신청기간: 상시\n신청방법: 학생지원팀 방문\n재발급비: 5,000원\n\n온라인 신청은 학사포털에서 가능합니다.', 'NORMAL', false, 134, 4, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 2025학년도 장학금 신청 안내', '2025학년도 1학기 장학금 신청을 다음과 같이 받습니다.\n\n신청기간: 2025년 1월 15일 ~ 1월 31일\n신청방법: 학사포털 로그인 후 신청\n문의: 학생지원팀 (02-1234-5678)', 'NOTICE', false, 389, 28, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 코로나19 방역수칙 준수 안내', '코로나19 확산 방지를 위해 다음 방역수칙을 준수해 주시기 바랍니다.\n\n- 마스크 착용 권장\n- 발열 시 등교 자제\n- 손 소독제 사용\n\n학생 여러분의 협조 부탁드립니다.', 'URGENT', false, 278, 14, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 교내 Wi-Fi 시스템 점검 안내', '교내 무선인터넷 시스템 정기 점검을 실시합니다.\n\n점검일시: 2025년 1월 12일 02:00 ~ 06:00\n점검내용: 네트워크 장비 업그레이드\n\n점검 시간 동안 Wi-Fi 사용이 불가합니다.', 'NORMAL', false, 223, 11, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 동아리 등록 신청 안내', '2025학년도 신규 동아리 등록 신청을 받습니다.\n\n신청기간: 2025년 1월 20일 ~ 2월 10일\n신청자격: 재학생 10인 이상\n제출서류: 동아리 활동계획서\n\n학생복지팀으로 제출해 주세요.', 'NORMAL', false, 145, 6, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 복수전공/부전공 신청 안내', '2025학년도 복수전공 및 부전공 신청을 받습니다.\n\n신청자격: 2학년 이상, 평점 3.0 이상\n신청기간: 2025년 1월 22일 ~ 2월 5일\n신청방법: 학사포털\n\n자세한 내용은 학사공지를 참고하세요.', 'URGENT', false, 334, 19, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 학생 주차장 이용 안내', '학생 주차장 이용 수칙을 안내합니다.\n\n이용시간: 평일 08:00 ~ 22:00\n주차요금: 일일 3,000원\n주차권구입: 학생지원팀\n\n불법주차 차량은 견인 조치됩니다.', 'NORMAL', false, 167, 5, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 졸업사진 촬영 일정 안내', '2024학년도 졸업예정자 졸업사진 촬영 일정입니다.\n\n촬영일시: 2025년 1월 29일 ~ 1월 31일\n촬영장소: 본관 앞\n준비물: 학사복(대여 가능)\n\n해당 학생은 시간을 확인하시기 바랍니다.', 'NOTICE', false, 256, 16, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 학점 포기 신청 안내', '2024학년도 2학기 학점 포기 제도 시행을 안내합니다.\n\n신청기간: 2025년 1월 6일 ~ 1월 10일\n신청방법: 학사포털\n포기가능: 최대 3학점\n\n신청 후 취소 불가하니 신중히 결정하세요.', 'URGENT', false, 298, 21, false, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 기숙사 입사 신청 안내', '2025학년도 1학기 기숙사 입사 신청을 받습니다.\n\n신청기간: 2025년 1월 13일 ~ 1월 27일\n신청대상: 재학생 및 신입생\n선발방법: 거리순, 성적순\n\n학생생활관 홈페이지에서 신청 가능합니다.', 'NOTICE', false, 412, 31, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 학생 상담센터 운영 안내', '학생 상담센터에서는 다음과 같은 상담을 제공합니다.\n\n- 학업 상담\n- 진로 상담\n- 심리 상담\n\n예약: 학사포털 또는 전화 (02-1234-5679)', 'NORMAL', false, 178, 8, false, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 해외 교환학생 프로그램 안내', '2025학년도 해외 교환학생 프로그램 신청을 받습니다.\n\n신청자격: 3학년, 평점 3.5 이상\n파견국가: 미국, 일본, 독일 등\n신청기간: 2025년 2월 1일 ~ 2월 28일\n\n국제교류팀으로 문의하세요.', 'URGENT', false, 367, 25, false, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 취업특강 개최 안내', '취업 준비를 위한 특강을 개최합니다.\n\n주제: 이력서 작성법 및 면접 노하우\n일시: 2025년 1월 23일 14:00\n장소: 대강당\n강사: 현직 인사담당자\n\n참석을 원하시는 분은 사전 신청해 주세요.', 'NORMAL', false, 289, 17, false, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY, 20241001, 20241001);

SET @first_post_id = (SELECT id FROM posts WHERE title = '[중요] 2025학년도 1학기 수강신청 일정 안내' LIMIT 1);
SET @second_post_id = (SELECT id FROM posts WHERE title = '[공지] 2024학년도 2학기 기말고사 시험 일정' LIMIT 1);
SET @third_post_id = (SELECT id FROM posts WHERE title = '[중요] 학생 건강검진 실시 안내' LIMIT 1);

INSERT INTO comments (post_id, author_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@first_post_id, 20241001, '수강신청 일정 공지 감사합니다. 꼭 확인하겠습니다!', 0, false, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '1차 수강신청에서 정정이 가능한가요?', 0, false, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '유의사항도 자세히 안내해주셔서 감사합니다.', 0, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '시간표 짜는 것부터 어려운데 잘 준비해야겠어요.', 0, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '2차 수강신청 일정도 놓치지 않도록 주의해야겠네요!', 0, false, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001);

SET @parent_comment_id = (SELECT id FROM comments WHERE content = '1차 수강신청에서 정정이 가능한가요?' LIMIT 1);
INSERT INTO comments (post_id, author_id, parent_comment_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by, is_deleted_by_admin) VALUES
(@first_post_id, 20241001, @parent_comment_id, '1차 수강신청 기간 내에는 자유롭게 정정 가능합니다.', 1, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001, false),
(@first_post_id, 20241001, @parent_comment_id, '다만 정정 마감 시간을 꼭 확인하시기 바랍니다.', 1, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001, false);

INSERT INTO comments (post_id, author_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@second_post_id, 20241001, '기말고사 일정 확인했습니다. 열심히 준비하겠습니다!', 0, false, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@second_post_id, 20241001, '성적 공개는 12월 27일이군요. 기다려집니다.', 0, false, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@second_post_id, 20241001, '시험 기간 동안 도서관 운영시간도 연장되나요?', 0, false, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001);

INSERT INTO comments (post_id, author_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@third_post_id, 20241001, '건강검진 꼭 받아야 하나요?', 0, false, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@third_post_id, 20241001, '대상 학년이 아니라 안타깝네요. 건강 챙기세요!', 0, false, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@third_post_id, 20241001, '보건소 위치가 어디인가요?', 0, false, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@third_post_id, 20241001, '건강검진 결과는 언제 나오나요?', 0, false, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001);

SET @health_comment_id = (SELECT id FROM comments WHERE content = '건강검진 꼭 받아야 하나요?' LIMIT 1);
INSERT INTO comments (post_id, author_id, parent_comment_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by, is_deleted_by_admin) VALUES
(@third_post_id, 20241001, @health_comment_id, '네, 필수로 받으셔야 합니다. 미이수 시 졸업에 영향을 줄 수 있습니다.', 1, false, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001, false);

-- =====================================================
-- 해시태그 더미데이터
-- =====================================================

-- 자유 게시판 해시태그

-- =====================================================
-- 통합 Hashtags 데이터 (중복 제거됨)
-- =====================================================
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('대학생활', '대학생활', '#1976d2', 'FREE', 1, 20241001, NOW()),
('맛집추천', '맛집추천', '#d32f2f', 'FREE', 1, 20241001, NOW()),
('동아리', '동아리', '#0288d1', 'FREE', 1, 20241001, NOW()),
('취미', '취미', '#9c27b0', 'FREE', 1, 20241001, NOW()),
('일상', '일상', '#f57c00', 'FREE', 1, 20241001, NOW()),
('고민상담', '고민상담', '#388e3c', 'FREE', 1, 20241001, NOW()),
('자료구조', '자료구조', '#1976d2', 'QUESTION', 1, 20241001, NOW()),
('알고리즘', '알고리즘', '#9c27b0', 'QUESTION', 1, 20241001, NOW()),
('데이터베이스', '데이터베이스', '#0288d1', 'QUESTION', 1, 20241001, NOW()),
('웹개발', '웹개발', '#388e3c', 'QUESTION', 1, 20241001, NOW()),
('운영체제', '운영체제', '#f57c00', 'QUESTION', 1, 20241001, NOW()),
('네트워크', '네트워크', '#d32f2f', 'QUESTION', 1, 20241001, NOW()),
('ai', 'AI', '#1976d2', 'QUESTION', 1, 20241001, NOW()),
('ai윤리토론', 'AI윤리토론', '#1976d2', 'DISCUSSION', 1, 20241001, NOW()),
('기후변화대응', '기후변화대응', '#388e3c', 'DISCUSSION', 1, 20241001, NOW()),
('교육개혁', '교육개혁', '#f57c00', 'DISCUSSION', 1, 20241001, NOW()),
('메타버스미래', '메타버스미래', '#0288d1', 'DISCUSSION', 1, 20241001, NOW()),
('청년정책', '청년정책', '#9c27b0', 'DISCUSSION', 1, 20241001, NOW()),
('esg경영', 'ESG경영', '#388e3c', 'DISCUSSION', 1, 20241001, NOW()),
('디지털전환', '디지털전환', '#1976d2', 'DISCUSSION', 1, 20241001, NOW()),
('사회적가치', '사회적가치', '#d32f2f', 'DISCUSSION', 1, 20241001, NOW()),
('학업고민', '학업고민', '#1976d2', 'STUDENT', 1, 20241001, NOW()),
('진로상담', '진로상담', '#9c27b0', 'STUDENT', 1, 20241001, NOW()),
('대외활동', '대외활동', '#0288d1', 'STUDENT', 1, 20241001, NOW()),
('학생회', '학생회', '#f57c00', 'STUDENT', 1, 20241001, NOW()),
('동아리모집', '동아리모집', '#388e3c', 'STUDENT', 1, 20241001, NOW()),
('it/소프트웨어', 'IT/소프트웨어', '#1976d2', 'CONTEST', 1, 20241001, NOW()),
('디자인', '디자인', '#9c27b0', 'CONTEST', 1, 20241001, NOW()),
('마케팅', '마케팅', '#0288d1', 'CONTEST', 1, 20241001, NOW()),
('아이디어', '아이디어', '#f57c00', 'CONTEST', 1, 20241001, NOW()),
('창업', '창업', '#d32f2f', 'CONTEST', 1, 20241001, NOW()),
('사회혁신', '사회혁신', '#388e3c', 'CONTEST', 1, 20241001, NOW()),
('채용공고', '채용공고', '#1976d2', 'CAREER', 1, 20241001, NOW()),
('면접후기', '면접후기', '#9c27b0', 'CAREER', 1, 20241001, NOW()),
('인턴', '인턴', '#0288d1', 'CAREER', 1, 20241001, NOW()),
('자소서첨삭', '자소서첨삭', '#f57c00', 'CAREER', 1, 20241001, NOW()),
('포트폴리오', '포트폴리오', '#388e3c', 'CAREER', 1, 20241001, NOW()),
('이력서', '이력서', '#d32f2f', 'CAREER', 1, 20241001, NOW()),
('코딩테스트', '코딩테스트', '#1976d2', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('자격증', '자격증', '#9c27b0', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('프로젝트', '프로젝트', '#0288d1', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('토익토스', '토익토스', '#f57c00', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('전공공부', '전공공부', '#388e3c', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('학과공지', '학과공지', '#1976d2', 'DEPARTMENT', 1, 20241001, NOW()),
('전공수업', '전공수업', '#9c27b0', 'DEPARTMENT', 1, 20241001, NOW()),
('졸업요건', '졸업요건', '#0288d1', 'DEPARTMENT', 1, 20241001, NOW()),
('학과행사', '학과행사', '#f57c00', 'DEPARTMENT', 1, 20241001, NOW()),
('교수님공지', '교수님공지', '#d32f2f', 'DEPARTMENT', 1, 20241001, NOW()),
('학과세미나', '학과세미나', '#388e3c', 'DEPARTMENT', 1, 20241001, NOW()),
('졸업논문', '졸업논문', '#1976d2', 'DEPARTMENT', 1, 20241001, NOW()),
('학과동아리', '학과동아리', '#9c27b0', 'DEPARTMENT', 1, 20241001, NOW()),
('코딩과제', '코딩과제', '#0288d1', 'ASSIGNMENT', 1, 20241001, NOW()),
('프로그래밍', '프로그래밍', '#f57c00', 'ASSIGNMENT', 1, 20241001, NOW()),
('동적계획법', '동적계획법', '#388e3c', 'ASSIGNMENT', 1, 20241001, NOW());


-- 질문 게시판 해시태그

-- 토론 게시판 해시태그

-- 학생 게시판 해시태그

-- 공모전 게시판 해시태그

-- 취업정보 게시판 해시태그

-- 스터디모집 게시판 해시태그

-- =====================================================
-- 게시글 더미데이터
-- =====================================================

SET @free_category_id = (SELECT id FROM board_categories WHERE board_type = 'FREE' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@free_category_id, 20241001, '신촌 맛집 추천해주세요!', '신촌에서 혼밥하기 좋은 맛집 있을까요?', 'NORMAL', false, 45, 8, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '대학생활 고민 상담', '대학생활이 처음이라 어렵네요. 선배님들 조언 부탁드립니다.', 'NORMAL', true, 67, 12, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '취미로 시작하기 좋은 운동', '요즘 운동을 시작하고 싶은데 추천해주세요!', 'NORMAL', false, 34, 5, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '동아리 추천 부탁드려요', '1학년인데 어떤 동아리에 가입하면 좋을까요?', 'NORMAL', false, 89, 15, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '강남역 근처 카페 추천', '공부하기 좋은 조용한 카페 있나요?', 'NORMAL', false, 23, 3, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '일상 브이로그 시작했어요', '유튜브 시작했는데 어떻게 생각하세요?', 'NORMAL', false, 56, 9, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '시험기간 고민상담', '시험 스트레스가 심한데 어떻게 해소하시나요?', 'NORMAL', true, 78, 14, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '홍대 맛집 리스트', '홍대에서 먹어본 맛집들 공유합니다!', 'NORMAL', false, 102, 21, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '사진 취미 시작하기', '카메라 입문하려는데 추천 기종 있나요?', 'NORMAL', false, 41, 7, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '대학생활 일상 공유', '오늘 하루 일과를 공유합니다~', 'NORMAL', false, 29, 4, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001);

SET @question_category_id = (SELECT id FROM board_categories WHERE board_type = 'QUESTION' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@question_category_id, 20241001, '자료구조 이진트리 질문', '이진트리의 순회 방법에 대해 설명해주세요.', 'NORMAL', false, 134, 23, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@question_category_id, 20241001, '알고리즘 시간복잡도 질문', 'O(n^2)와 O(nlogn)의 차이가 뭔가요?', 'NORMAL', false, 98, 17, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'SQL JOIN 종류 질문', 'INNER JOIN과 OUTER JOIN의 차이를 알려주세요.', 'NORMAL', false, 156, 28, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'React useState 사용법', 'useState hook을 어떻게 사용하나요?', 'NORMAL', false, 87, 15, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@question_category_id, 20241001, '운영체제 프로세스 vs 스레드', '프로세스와 스레드의 차이점이 궁금합니다.', 'NORMAL', false, 201, 34, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'TCP vs UDP 차이점', '네트워크 프로토콜 차이를 알려주세요.', 'NORMAL', false, 143, 25, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'AI 머신러닝 기초 질문', '머신러닝과 딥러닝의 차이가 뭔가요?', 'NORMAL', false, 178, 31, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Java 스트림 API 질문', 'Stream을 언제 사용하면 좋나요?', 'NORMAL', false, 91, 16, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Python 리스트 컴프리헨션', '리스트 컴프리헨션 사용법을 알려주세요.', 'NORMAL', false, 76, 13, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'DB 정규화 질문', '제3정규형까지 정규화하는 방법이 궁금합니다.', 'NORMAL', false, 167, 29, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Spring Boot JPA 연관관계', '양방향 연관관계 설정 방법 질문입니다.', 'NORMAL', false, 124, 22, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),
(@question_category_id, 20241001, '네트워크 OSI 7계층', 'OSI 7계층을 쉽게 설명해주세요.', 'NORMAL', false, 189, 33, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Git 브랜치 전략 질문', 'Git Flow와 GitHub Flow 차이가 뭔가요?', 'NORMAL', false, 145, 26, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'RESTful API 설계 질문', 'REST API 설계 원칙을 알려주세요.', 'NORMAL', false, 203, 35, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Docker 컨테이너 질문', 'Docker와 VM의 차이점이 궁금합니다.', 'NORMAL', false, 112, 20, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001);

SET @discussion_category_id = (SELECT id FROM board_categories WHERE board_type = 'DISCUSSION' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@discussion_category_id, 20241001, 'AI 윤리 문제에 대한 토론', '인공지능 발전에 따른 윤리 문제를 어떻게 생각하시나요?', 'NORMAL', false, 234, 42, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '기후변화 대응 방안', '개인이 실천할 수 있는 기후변화 대응책은?', 'NORMAL', false, 189, 35, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '대학 교육개혁 토론', '현재 대학 교육 시스템의 문제점과 개선방안', 'NORMAL', false, 267, 48, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '메타버스의 미래', '메타버스가 우리 생활을 어떻게 바꿀까요?', 'NORMAL', false, 312, 56, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '청년 일자리 정책 토론', '청년 실업 문제 해결 방안에 대해 토론해봅시다.', 'NORMAL', false, 198, 37, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, 'ESG 경영의 필요성', '기업의 ESG 경영은 정말 필요할까요?', 'NORMAL', false, 245, 44, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '디지털 전환 시대', '디지털 전환이 산업에 미치는 영향', 'NORMAL', false, 223, 41, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '사회적 가치 창출', '기업의 사회적 책임에 대해 토론합시다.', 'NORMAL', false, 201, 38, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '원격근무의 장단점', '코로나 이후 원격근무 문화는 지속될까요?', 'NORMAL', false, 276, 50, false, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '인공지능과 일자리', 'AI가 대체할 직업과 새로 생길 직업', 'NORMAL', false, 298, 53, false, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY, 20241001, 20241001);

-- =====================================================
-- 게시글-해시태그 연결
-- =====================================================
INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('맛집추천', '일상')
WHERE p.title LIKE '%신촌 맛집%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('대학생활', '고민상담')
WHERE p.title LIKE '%대학생활 고민%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('취미')
WHERE p.title LIKE '%취미로 시작%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('동아리', '대학생활')
WHERE p.title LIKE '%동아리 추천%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('맛집추천')
WHERE p.title LIKE '%강남역 근처%' OR p.title LIKE '%홍대 맛집%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('일상')
WHERE p.title LIKE '%일상 브이로그%' OR p.title LIKE '%일상 공유%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('고민상담')
WHERE p.title LIKE '%시험기간 고민%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('취미')
WHERE p.title LIKE '%사진 취미%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('자료구조', '알고리즘')
WHERE p.title LIKE '%자료구조 이진트리%' OR p.title LIKE '%알고리즘 시간복잡도%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('데이터베이스')
WHERE p.title LIKE '%SQL JOIN%' OR p.title LIKE '%DB 정규화%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('웹개발')
WHERE p.title LIKE '%React%' OR p.title LIKE '%Spring Boot%' OR p.title LIKE '%RESTful%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('운영체제')
WHERE p.title LIKE '%운영체제 프로세스%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('네트워크')
WHERE p.title LIKE '%TCP vs UDP%' OR p.title LIKE '%네트워크 OSI%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('ai')
WHERE p.title LIKE '%AI 머신러닝%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('ai윤리토론')
WHERE p.title LIKE '%AI 윤리%' OR p.title LIKE '%인공지능과 일자리%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('기후변화대응')
WHERE p.title LIKE '%기후변화 대응%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('교육개혁')
WHERE p.title LIKE '%교육개혁%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('메타버스미래')
WHERE p.title LIKE '%메타버스%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('청년정책')
WHERE p.title LIKE '%청년 일자리%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('esg경영')
WHERE p.title LIKE '%ESG 경영%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('디지털전환')
WHERE p.title LIKE '%디지털 전환%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('사회적가치')
WHERE p.title LIKE '%사회적 가치%';

-- =====================================================
-- 학과 게시판 더미데이터
-- =====================================================

-- 학과 게시판 해시태그

SET @department_category_id = (SELECT id FROM board_categories WHERE board_type = 'DEPARTMENT' LIMIT 1);

INSERT INTO posts (category_id, author_id, department_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
-- 컴퓨터공학과 관련 게시글
(@department_category_id, 20241001, 1, '[컴공] 2025-1학기 전공 수강신청 안내', '컴퓨터공학과 2025학년도 1학기 전공 수강신청 안내입니다.\n\n필수 이수 과목:\n- 자료구조 및 실습\n- 알고리즘\n- 데이터베이스\n\n선이수 과목을 꼭 확인하시기 바랍니다.', 'URGENT', false, 342, 45, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 1, '[컴공] 캡스톤디자인 팀 구성 안내', '2024학년도 2학기 캡스톤디자인 프로젝트 팀 구성 안내\n\n팀 구성: 3~4명\n주제 제출: 2025년 1월 20일까지\n중간발표: 2025년 5월 중순\n최종발표: 2025년 6월 초\n\n학과 사무실로 팀 명단을 제출해주세요.', 'NOTICE', false, 289, 38, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 1, '[컴공] 졸업논문 심사 일정 공지', '컴퓨터공학과 2024학년도 졸업논문 심사 일정입니다.\n\n논문 제출: 2025년 1월 31일\n예비 심사: 2025년 2월 10일\n본 심사: 2025년 2월 20일\n\n지도교수님과 충분히 상담하시기 바랍니다.', 'URGENT', false, 267, 34, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 1, '[컴공] AI 특강 개최 안내', '인공지능 분야 특강을 개최합니다.\n\n주제: 딥러닝과 컴퓨터 비전의 최신 동향\n강사: 김AI 교수님 (서울대학교)\n일시: 2025년 1월 25일 14:00\n장소: 공학관 301호\n\n컴퓨터공학 전공자라면 누구나 참석 가능합니다.', 'NORMAL', false, 198, 27, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 1, '[컴공] 학과 MT 참가 신청', '컴퓨터공학과 신입생 환영 MT를 개최합니다!\n\n일시: 2025년 2월 15일 ~ 16일 (1박 2일)\n장소: 강원도 평창\n참가비: 5만원\n신청: 학과 사무실 또는 온라인 신청\n\n많은 참여 부탁드립니다!', 'NORMAL', false, 423, 56, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),

-- 경영학과 관련 게시글
(@department_category_id, 20241001, 6, '[경영] 마케팅 전략 특강 안내', '경영학과 마케팅 전략 특강을 개최합니다.\n\n주제: 디지털 마케팅의 이해와 실전 전략\n강사: 박마케팅 대표 (네이버 출신)\n일시: 2025년 1월 28일 15:00\n장소: 경영관 대강당\n\n경영학 전공자 필수 참석 권장합니다.', 'NOTICE', false, 312, 41, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 6, '[경영] 2025 취업 박람회 참가 안내', '경영대학 취업 박람회가 개최됩니다.\n\n일시: 2025년 2월 5일 10:00 ~ 17:00\n장소: 학생회관 대강당\n참가 기업: 삼성, LG, 현대 등 30개사\n\n이력서 지참 필수입니다.', 'URGENT', false, 467, 62, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 6, '[경영] 경영학과 학술제 개최', '제10회 경영학과 학술제를 개최합니다.\n\n주제: ESG 경영과 지속가능한 성장\n일시: 2025년 2월 3일\n발표 신청: 1월 25일까지\n\n우수 발표자에게는 장학금이 수여됩니다.', 'NORMAL', false, 234, 32, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),
(@department_category_id, 20241001, 6, '[경영] 재무관리 세미나 안내', '재무관리 분야 세미나를 개최합니다.\n\n주제: 최신 재무관리 기법과 사례 분석\n강사: 이재무 교수님 (고려대학교)\n일시: 2025년 1월 24일 13:00\n장소: 경영관 204호\n\n경영학 전공자라면 누구나 참석 가능합니다.', 'NORMAL', false, 176, 24, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001),
-- 기계공학과 관련 게시글
(@department_category_id, 20241001, 3, '[기계] 공작기계 실습 안전교육 필수 이수', '기계공학과 전공 실습을 위한 안전교육 이수 안내\n\n대상: 기계공학 전공자 전원\n교육 일시: 2025년 1월 22일 ~ 24일\n장소: 공학관 실습실\n\n미이수 시 실습 참여 불가합니다.', 'URGENT', false, 278, 36, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 3, '[기계] 로봇공학 특강 개최', '로봇공학 최신 기술 동향 특강\n\n주제: 협동로봇의 현재와 미래\n강사: 이로봇 박사 (KAIST)\n일시: 2025년 1월 30일 14:00\n장소: 기계관 세미나실\n\n관심있는 학생들의 참석 바랍니다.', 'NORMAL', false, 189, 25, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),
-- 전자공학과 관련 게시글
(@department_category_id, 20241001, 2, '[전자] 임베디드 시스템 경진대회', '전자공학과 임베디드 시스템 경진대회 개최\n\n신청 기간: 2025년 1월 15일 ~ 30일\n대회 일시: 2025년 2월 20일\n참가 자격: 전자공학 전공자\n시상: 대상 100만원, 금상 50만원\n\n많은 관심과 참여 부탁드립니다.', 'NOTICE', false, 356, 48, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 2, '[전자] 반도체 공정 견학 안내', '삼성전자 반도체 공장 견학 프로그램\n\n일시: 2025년 2월 8일\n장소: 삼성전자 화성사업장\n인원: 30명 (선착순)\n신청: 학과 사무실\n\n실무 경험의 좋은 기회입니다!', 'NORMAL', false, 412, 54, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),

-- 화학과 관련 게시글
(@department_category_id, 20241001, 4, '[화학] 실험실 안전교육 필수 이수', '화학과 실험실 안전교육 이수 안내\n\n교육 일시: 2025년 1월 20일\n장소: 화학관 강의실\n대상: 실험 수업 수강자 전원\n\n미이수 시 실험 참여가 제한됩니다.', 'URGENT', false, 245, 31, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 4, '[화학] 신소재 화학 세미나', '신소재 화학 최신 연구 동향 세미나\n\n주제: 나노 소재의 응용\n강사: 최화학 교수님 (서울대)\n일시: 2025년 1월 27일 15:00\n장소: 자연과학관 세미나실', 'NORMAL', false, 167, 22, false, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 20241001, 20241001),

-- 영어영문학과 관련 게시글
(@department_category_id, 20241001, 11, '[영문] 영미문학 독서 토론회', '영미문학 고전 작품 독서 토론회를 개최합니다.\n\n작품: "위대한 개츠비"\n일시: 2025년 1월 29일 16:00\n장소: 인문관 세미나실\n\n작품을 미리 읽고 오시면 더욱 좋습니다.', 'NORMAL', false, 134, 18, false, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 11, '[영문] TOEIC Speaking 특강', '영어영문학과 토익스피킹 특강\n\n강사: 원어민 교수\n일시: 매주 목요일 18:00\n장소: 어학관 301호\n신청: 학과 사무실\n\n무료로 진행되니 많이 참여해주세요!', 'NOTICE', false, 298, 39, false, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY, 20241001, 20241001),

-- 수학과 관련 게시글
(@department_category_id, 20241001, 14, '[수학] 해석학 스터디 모집', '해석학 심화 스터디 그룹을 모집합니다.\n\n교재: Rudin의 해석학 원론\n일시: 매주 화, 목 19:00\n장소: 자연과학관 스터디룸\n모집 인원: 6명\n\n관심있으신 분은 댓글 남겨주세요!', 'NORMAL', false, 89, 12, false, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY, 20241001, 20241001),

(@department_category_id, 20241001, 14, '[수학] 수학 경시대회 안내', '전국 대학생 수학 경시대회 참가자 모집\n\n신청 기간: 2025년 1월 25일까지\n대회 일시: 2025년 2월 15일\n시상: 대상 200만원\n\n수학과의 명예를 위해 많은 참여 부탁드립니다.', 'NOTICE', false, 201, 28, false, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 20241001, 20241001);

-- 학과 게시판 게시글-해시태그 연결
INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('학과공지', '전공수업')
WHERE p.title LIKE '[컴공] 2025-1학기 전공 수강신청 안내';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('학과행사')
WHERE p.title LIKE '%캡스톤디자인%' OR p.title LIKE '%MT 참가%' OR p.title LIKE '%학술제%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('졸업논문', '졸업요건')
WHERE p.title LIKE '%졸업논문%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('학과세미나', '교수님공지')
WHERE p.title LIKE '%특강%' OR p.title LIKE '%세미나%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('학과공지')
WHERE p.title LIKE '[경영] 2025 취업 박람회%' OR p.title LIKE '%안전교육%' OR p.title LIKE '%경진대회%';

INSERT INTO post_hashtags (post_id, hashtag_id, created_by, created_at) 
SELECT p.id, h.id, 20241001, NOW()
FROM posts p
JOIN hashtags h ON h.name IN ('학과동아리')
WHERE p.title LIKE '%독서 토론회%' OR p.title LIKE '%스터디 모집%';

SET @notice_category_id = (SELECT id FROM board_categories WHERE board_type = 'NOTICE' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@notice_category_id, 20241001, '[중요] 2025학년도 1학기 수강신청 일정 안내', '2025학년도 1학기 수강신청 일정을 다음과 같이 안내합니다.\n\n1차 수강신청: 2025년 1월 15일 ~ 1월 17일\n2차 수강신청: 2025년 1월 22일 ~ 1월 24일\n\n자세한 내용은 첨부파일을 참고하시기 바랍니다.', 'URGENT', false, 245, 12, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 2024학년도 2학기 기말고사 시험 일정', '2024학년도 2학기 기말고사 일정이 확정되었습니다.\n\n시험기간: 2024년 12월 16일 ~ 12월 20일\n성적 공개: 2024년 12월 27일\n\n수험생 여러분의 건승을 기원합니다.', 'NOTICE', false, 189, 8, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 동계 방학 도서관 운영시간 변경', '동계 방학 기간 중 도서관 운영시간이 다음과 같이 변경됩니다.\n\n평일: 09:00 ~ 18:00\n주말: 10:00 ~ 16:00\n\n2025년 1월 6일부터 적용됩니다.', 'NORMAL', false, 156, 5, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 학생 건강검진 실시 안내', '2024학년도 학생 건강검진을 다음과 같이 실시합니다.\n\n대상: 1학년, 3학년 재학생\n일시: 2025년 1월 13일 ~ 1월 17일\n장소: 학생회관 2층 보건소\n\n필수 참여 바랍니다.', 'URGENT', false, 312, 15, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 2025년도 등록금 납부 안내', '2025학년도 1학기 등록금 납부 기간을 안내합니다.\n\n납부기간: 2025년 1월 20일 ~ 2월 5일\n납부방법: 가상계좌 이체\n\n기한 내 납부하지 않을 시 제적될 수 있으니 유의하시기 바랍니다.', 'URGENT', false, 421, 23, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 졸업논문 제출 일정 공지', '2024학년도 졸업예정자 논문 제출 일정입니다.\n\n제출기한: 2025년 1월 31일까지\n제출처: 학과 사무실\n제출물: 논문 3부, 요약본 1부\n\n기한 엄수 바랍니다.', 'NORMAL', false, 198, 9, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 학사경고자 면담 일정 안내', '2024학년도 2학기 학사경고 대상자 면담 일정을 안내합니다.\n\n일시: 2025년 1월 8일 ~ 1월 10일\n장소: 학생상담센터\n\n해당 학생은 반드시 참석하시기 바랍니다.', 'URGENT', false, 167, 7, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 학생증 재발급 신청 안내', '분실 또는 훼손된 학생증 재발급 신청을 받습니다.\n\n신청기간: 상시\n신청방법: 학생지원팀 방문\n재발급비: 5,000원\n\n온라인 신청은 학사포털에서 가능합니다.', 'NORMAL', false, 134, 4, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 2025학년도 장학금 신청 안내', '2025학년도 1학기 장학금 신청을 다음과 같이 받습니다.\n\n신청기간: 2025년 1월 15일 ~ 1월 31일\n신청방법: 학사포털 로그인 후 신청\n문의: 학생지원팀 (02-1234-5678)', 'NOTICE', false, 389, 28, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 코로나19 방역수칙 준수 안내', '코로나19 확산 방지를 위해 다음 방역수칙을 준수해 주시기 바랍니다.\n\n- 마스크 착용 권장\n- 발열 시 등교 자제\n- 손 소독제 사용\n\n학생 여러분의 협조 부탁드립니다.', 'URGENT', false, 278, 14, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 교내 Wi-Fi 시스템 점검 안내', '교내 무선인터넷 시스템 정기 점검을 실시합니다.\n\n점검일시: 2025년 1월 12일 02:00 ~ 06:00\n점검내용: 네트워크 장비 업그레이드\n\n점검 시간 동안 Wi-Fi 사용이 불가합니다.', 'NORMAL', false, 223, 11, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 동아리 등록 신청 안내', '2025학년도 신규 동아리 등록 신청을 받습니다.\n\n신청기간: 2025년 1월 20일 ~ 2월 10일\n신청자격: 재학생 10인 이상\n제출서류: 동아리 활동계획서\n\n학생복지팀으로 제출해 주세요.', 'NORMAL', false, 145, 6, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 복수전공/부전공 신청 안내', '2025학년도 복수전공 및 부전공 신청을 받습니다.\n\n신청자격: 2학년 이상, 평점 3.0 이상\n신청기간: 2025년 1월 22일 ~ 2월 5일\n신청방법: 학사포털\n\n자세한 내용은 학사공지를 참고하세요.', 'URGENT', false, 334, 19, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 학생 주차장 이용 안내', '학생 주차장 이용 수칙을 안내합니다.\n\n이용시간: 평일 08:00 ~ 22:00\n주차요금: 일일 3,000원\n주차권구입: 학생지원팀\n\n불법주차 차량은 견인 조치됩니다.', 'NORMAL', false, 167, 5, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 졸업사진 촬영 일정 안내', '2024학년도 졸업예정자 졸업사진 촬영 일정입니다.\n\n촬영일시: 2025년 1월 29일 ~ 1월 31일\n촬영장소: 본관 앞\n준비물: 학사복(대여 가능)\n\n해당 학생은 시간을 확인하시기 바랍니다.', 'NOTICE', false, 256, 16, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 학점 포기 신청 안내', '2024학년도 2학기 학점 포기 제도 시행을 안내합니다.\n\n신청기간: 2025년 1월 6일 ~ 1월 10일\n신청방법: 학사포털\n포기가능: 최대 3학점\n\n신청 후 취소 불가하니 신중히 결정하세요.', 'URGENT', false, 298, 21, false, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 기숙사 입사 신청 안내', '2025학년도 1학기 기숙사 입사 신청을 받습니다.\n\n신청기간: 2025년 1월 13일 ~ 1월 27일\n신청대상: 재학생 및 신입생\n선발방법: 거리순, 성적순\n\n학생생활관 홈페이지에서 신청 가능합니다.', 'NOTICE', false, 412, 31, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[안내] 학생 상담센터 운영 안내', '학생 상담센터에서는 다음과 같은 상담을 제공합니다.\n\n- 학업 상담\n- 진로 상담\n- 심리 상담\n\n예약: 학사포털 또는 전화 (02-1234-5679)', 'NORMAL', false, 178, 8, false, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[중요] 해외 교환학생 프로그램 안내', '2025학년도 해외 교환학생 프로그램 신청을 받습니다.\n\n신청자격: 3학년, 평점 3.5 이상\n파견국가: 미국, 일본, 독일 등\n신청기간: 2025년 2월 1일 ~ 2월 28일\n\n국제교류팀으로 문의하세요.', 'URGENT', false, 367, 25, false, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY, 20241001, 20241001),

(@notice_category_id, 20241001, '[공지] 취업특강 개최 안내', '취업 준비를 위한 특강을 개최합니다.\n\n주제: 이력서 작성법 및 면접 노하우\n일시: 2025년 1월 23일 14:00\n장소: 대강당\n강사: 현직 인사담당자\n\n참석을 원하시는 분은 사전 신청해 주세요.', 'NORMAL', false, 289, 17, false, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY, 20241001, 20241001);

SET @first_post_id = (SELECT id FROM posts WHERE title = '[중요] 2025학년도 1학기 수강신청 일정 안내' LIMIT 1);
SET @second_post_id = (SELECT id FROM posts WHERE title = '[공지] 2024학년도 2학기 기말고사 시험 일정' LIMIT 1);
SET @third_post_id = (SELECT id FROM posts WHERE title = '[중요] 학생 건강검진 실시 안내' LIMIT 1);


SET @parent_comment_id = (SELECT id FROM comments WHERE content = '1차 수강신청에서 정정이 가능한가요?' LIMIT 1);



SET @health_comment_id = (SELECT id FROM comments WHERE content = '건강검진 꼭 받아야 하나요?' LIMIT 1);

-- =====================================================
-- 해시태그 더미데이터
-- =====================================================

-- 자유 게시판 해시태그

-- 질문 게시판 해시태그

-- 토론 게시판 해시태그

-- 학생 게시판 해시태그

-- 공모전 게시판 해시태그

-- 취업정보 게시판 해시태그

-- 스터디모집 게시판 해시태그

-- =====================================================
-- 게시글 더미데이터
-- =====================================================

SET @free_category_id = (SELECT id FROM board_categories WHERE board_type = 'FREE' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@free_category_id, 20241001, '신촌 맛집 추천해주세요!', '신촌에서 혼밥하기 좋은 맛집 있을까요?', 'NORMAL', false, 45, 8, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '대학생활 고민 상담', '대학생활이 처음이라 어렵네요. 선배님들 조언 부탁드립니다.', 'NORMAL', true, 67, 12, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '취미로 시작하기 좋은 운동', '요즘 운동을 시작하고 싶은데 추천해주세요!', 'NORMAL', false, 34, 5, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '동아리 추천 부탁드려요', '1학년인데 어떤 동아리에 가입하면 좋을까요?', 'NORMAL', false, 89, 15, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '강남역 근처 카페 추천', '공부하기 좋은 조용한 카페 있나요?', 'NORMAL', false, 23, 3, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '일상 브이로그 시작했어요', '유튜브 시작했는데 어떻게 생각하세요?', 'NORMAL', false, 56, 9, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '시험기간 고민상담', '시험 스트레스가 심한데 어떻게 해소하시나요?', 'NORMAL', true, 78, 14, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '홍대 맛집 리스트', '홍대에서 먹어본 맛집들 공유합니다!', 'NORMAL', false, 102, 21, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '사진 취미 시작하기', '카메라 입문하려는데 추천 기종 있나요?', 'NORMAL', false, 41, 7, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),
(@free_category_id, 20241001, '대학생활 일상 공유', '오늘 하루 일과를 공유합니다~', 'NORMAL', false, 29, 4, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001);

SET @question_category_id = (SELECT id FROM board_categories WHERE board_type = 'QUESTION' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@question_category_id, 20241001, '자료구조 이진트리 질문', '이진트리의 순회 방법에 대해 설명해주세요.', 'NORMAL', false, 134, 23, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@question_category_id, 20241001, '알고리즘 시간복잡도 질문', 'O(n^2)와 O(nlogn)의 차이가 뭔가요?', 'NORMAL', false, 98, 17, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'SQL JOIN 종류 질문', 'INNER JOIN과 OUTER JOIN의 차이를 알려주세요.', 'NORMAL', false, 156, 28, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'React useState 사용법', 'useState hook을 어떻게 사용하나요?', 'NORMAL', false, 87, 15, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@question_category_id, 20241001, '운영체제 프로세스 vs 스레드', '프로세스와 스레드의 차이점이 궁금합니다.', 'NORMAL', false, 201, 34, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'TCP vs UDP 차이점', '네트워크 프로토콜 차이를 알려주세요.', 'NORMAL', false, 143, 25, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'AI 머신러닝 기초 질문', '머신러닝과 딥러닝의 차이가 뭔가요?', 'NORMAL', false, 178, 31, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Java 스트림 API 질문', 'Stream을 언제 사용하면 좋나요?', 'NORMAL', false, 91, 16, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Python 리스트 컴프리헨션', '리스트 컴프리헨션 사용법을 알려주세요.', 'NORMAL', false, 76, 13, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'DB 정규화 질문', '제3정규형까지 정규화하는 방법이 궁금합니다.', 'NORMAL', false, 167, 29, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Spring Boot JPA 연관관계', '양방향 연관관계 설정 방법 질문입니다.', 'NORMAL', false, 124, 22, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),
(@question_category_id, 20241001, '네트워크 OSI 7계층', 'OSI 7계층을 쉽게 설명해주세요.', 'NORMAL', false, 189, 33, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Git 브랜치 전략 질문', 'Git Flow와 GitHub Flow 차이가 뭔가요?', 'NORMAL', false, 145, 26, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'RESTful API 설계 질문', 'REST API 설계 원칙을 알려주세요.', 'NORMAL', false, 203, 35, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),
(@question_category_id, 20241001, 'Docker 컨테이너 질문', 'Docker와 VM의 차이점이 궁금합니다.', 'NORMAL', false, 112, 20, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001);

SET @discussion_category_id = (SELECT id FROM board_categories WHERE board_type = 'DISCUSSION' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@discussion_category_id, 20241001, 'AI 윤리 문제에 대한 토론', '인공지능 발전에 따른 윤리 문제를 어떻게 생각하시나요?', 'NORMAL', false, 234, 42, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '기후변화 대응 방안', '개인이 실천할 수 있는 기후변화 대응책은?', 'NORMAL', false, 189, 35, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '대학 교육개혁 토론', '현재 대학 교육 시스템의 문제점과 개선방안', 'NORMAL', false, 267, 48, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '메타버스의 미래', '메타버스가 우리 생활을 어떻게 바꿀까요?', 'NORMAL', false, 312, 56, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '청년 일자리 정책 토론', '청년 실업 문제 해결 방안에 대해 토론해봅시다.', 'NORMAL', false, 198, 37, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, 'ESG 경영의 필요성', '기업의 ESG 경영은 정말 필요할까요?', 'NORMAL', false, 245, 44, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '디지털 전환 시대', '디지털 전환이 산업에 미치는 영향', 'NORMAL', false, 223, 41, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '사회적 가치 창출', '기업의 사회적 책임에 대해 토론합시다.', 'NORMAL', false, 201, 38, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '원격근무의 장단점', '코로나 이후 원격근무 문화는 지속될까요?', 'NORMAL', false, 276, 50, false, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY, 20241001, 20241001),
(@discussion_category_id, 20241001, '인공지능과 일자리', 'AI가 대체할 직업과 새로 생길 직업', 'NORMAL', false, 298, 53, false, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY, 20241001, 20241001);

-- =====================================================
-- 게시글-해시태그 연결
-- =====================================================












































-- =====================================================
-- 학과 게시판 더미데이터
-- =====================================================

-- 학과 게시판 해시태그

SET @department_category_id = (SELECT id FROM board_categories WHERE board_type = 'DEPARTMENT' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
-- 컴퓨터공학과 관련 게시글
(@department_category_id, 20241001, '[컴공] 2025-1학기 전공 수강신청 안내', '컴퓨터공학과 2025학년도 1학기 전공 수강신청 안내입니다.\n\n필수 이수 과목:\n- 자료구조 및 실습\n- 알고리즘\n- 데이터베이스\n\n선이수 과목을 꼭 확인하시기 바랍니다.', 'URGENT', false, 342, 45, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[컴공] 캡스톤디자인 팀 구성 안내', '2024학년도 2학기 캡스톤디자인 프로젝트 팀 구성 안내\n\n팀 구성: 3~4명\n주제 제출: 2025년 1월 20일까지\n중간발표: 2025년 5월 중순\n최종발표: 2025년 6월 초\n\n학과 사무실로 팀 명단을 제출해주세요.', 'NOTICE', false, 289, 38, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[컴공] 졸업논문 심사 일정 공지', '컴퓨터공학과 2024학년도 졸업논문 심사 일정입니다.\n\n논문 제출: 2025년 1월 31일\n예비 심사: 2025년 2월 10일\n본 심사: 2025년 2월 20일\n\n지도교수님과 충분히 상담하시기 바랍니다.', 'URGENT', false, 267, 34, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[컴공] AI 특강 개최 안내', '인공지능 분야 특강을 개최합니다.\n\n주제: 딥러닝과 컴퓨터 비전의 최신 동향\n강사: 김AI 교수님 (서울대학교)\n일시: 2025년 1월 25일 14:00\n장소: 공학관 301호\n\n컴퓨터공학 전공자라면 누구나 참석 가능합니다.', 'NORMAL', false, 198, 27, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[컴공] 학과 MT 참가 신청', '컴퓨터공학과 신입생 환영 MT를 개최합니다!\n\n일시: 2025년 2월 15일 ~ 16일 (1박 2일)\n장소: 강원도 평창\n참가비: 5만원\n신청: 학과 사무실 또는 온라인 신청\n\n많은 참여 부탁드립니다!', 'NORMAL', false, 423, 56, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),

-- 경영학과 관련 게시글
(@department_category_id, 20241001, '[경영] 마케팅 전략 특강 안내', '경영학과 마케팅 전략 특강을 개최합니다.\n\n주제: 디지털 마케팅의 이해와 실전 전략\n강사: 박마케팅 대표 (네이버 출신)\n일시: 2025년 1월 28일 15:00\n장소: 경영관 대강당\n\n경영학 전공자 필수 참석 권장합니다.', 'NOTICE', false, 312, 41, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[경영] 2025 취업 박람회 참가 안내', '경영대학 취업 박람회가 개최됩니다.\n\n일시: 2025년 2월 5일 10:00 ~ 17:00\n장소: 학생회관 대강당\n참가 기업: 삼성, LG, 현대 등 30개사\n\n이력서 지참 필수입니다.', 'URGENT', false, 467, 62, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[경영] 경영학과 학술제 개최', '제10회 경영학과 학술제를 개최합니다.\n\n주제: ESG 경영과 지속가능한 성장\n일시: 2025년 2월 3일\n발표 신청: 1월 25일까지\n\n우수 발표자에게는 장학금이 수여됩니다.', 'NORMAL', false, 234, 32, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),

-- 기계공학과 관련 게시글
(@department_category_id, 20241001, '[기계] 공작기계 실습 안전교육 필수 이수', '기계공학과 전공 실습을 위한 안전교육 이수 안내\n\n대상: 기계공학 전공자 전원\n교육 일시: 2025년 1월 22일 ~ 24일\n장소: 공학관 실습실\n\n미이수 시 실습 참여 불가합니다.', 'URGENT', false, 278, 36, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[기계] 로봇공학 특강 개최', '로봇공학 최신 기술 동향 특강\n\n주제: 협동로봇의 현재와 미래\n강사: 이로봇 박사 (KAIST)\n일시: 2025년 1월 30일 14:00\n장소: 기계관 세미나실\n\n관심있는 학생들의 참석 바랍니다.', 'NORMAL', false, 189, 25, false, NOW() - INTERVAL 11 DAY, NOW() - INTERVAL 11 DAY, 20241001, 20241001),

-- 전자공학과 관련 게시글
(@department_category_id, 20241001, '[전자] 임베디드 시스템 경진대회', '전자공학과 임베디드 시스템 경진대회 개최\n\n신청 기간: 2025년 1월 15일 ~ 30일\n대회 일시: 2025년 2월 20일\n참가 자격: 전자공학 전공자\n시상: 대상 100만원, 금상 50만원\n\n많은 관심과 참여 부탁드립니다.', 'NOTICE', false, 356, 48, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[전자] 반도체 공정 견학 안내', '삼성전자 반도체 공장 견학 프로그램\n\n일시: 2025년 2월 8일\n장소: 삼성전자 화성사업장\n인원: 30명 (선착순)\n신청: 학과 사무실\n\n실무 경험의 좋은 기회입니다!', 'NORMAL', false, 412, 54, false, NOW() - INTERVAL 13 DAY, NOW() - INTERVAL 13 DAY, 20241001, 20241001),

-- 화학과 관련 게시글
(@department_category_id, 20241001, '[화학] 실험실 안전교육 필수 이수', '화학과 실험실 안전교육 이수 안내\n\n교육 일시: 2025년 1월 20일\n장소: 화학관 강의실\n대상: 실험 수업 수강자 전원\n\n미이수 시 실험 참여가 제한됩니다.', 'URGENT', false, 245, 31, false, NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[화학] 신소재 화학 세미나', '신소재 화학 최신 연구 동향 세미나\n\n주제: 나노 소재의 응용\n강사: 최화학 교수님 (서울대)\n일시: 2025년 1월 27일 15:00\n장소: 자연과학관 세미나실', 'NORMAL', false, 167, 22, false, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 20241001, 20241001),

-- 영어영문학과 관련 게시글
(@department_category_id, 20241001, '[영문] 영미문학 독서 토론회', '영미문학 고전 작품 독서 토론회를 개최합니다.\n\n작품: "위대한 개츠비"\n일시: 2025년 1월 29일 16:00\n장소: 인문관 세미나실\n\n작품을 미리 읽고 오시면 더욱 좋습니다.', 'NORMAL', false, 134, 18, false, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 17 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[영문] TOEIC Speaking 특강', '영어영문학과 토익스피킹 특강\n\n강사: 원어민 교수\n일시: 매주 목요일 18:00\n장소: 어학관 301호\n신청: 학과 사무실\n\n무료로 진행되니 많이 참여해주세요!', 'NOTICE', false, 298, 39, false, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY, 20241001, 20241001),

-- 수학과 관련 게시글
(@department_category_id, 20241001, '[수학] 해석학 스터디 모집', '해석학 심화 스터디 그룹을 모집합니다.\n\n교재: Rudin의 해석학 원론\n일시: 매주 화, 목 19:00\n장소: 자연과학관 스터디룸\n모집 인원: 6명\n\n관심있으신 분은 댓글 남겨주세요!', 'NORMAL', false, 89, 12, false, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 19 DAY, 20241001, 20241001),

(@department_category_id, 20241001, '[수학] 수학 경시대회 안내', '전국 대학생 수학 경시대회 참가자 모집\n\n신청 기간: 2025년 1월 25일까지\n대회 일시: 2025년 2월 15일\n시상: 대상 200만원\n\n수학과의 명예를 위해 많은 참여 부탁드립니다.', 'NOTICE', false, 201, 28, false, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 20241001, 20241001);

-- 학과 게시판 게시글-해시태그 연결












-- =====================================================
-- 3. V7 데이터 추가: 학생/공모전/취업/스터디모집 게시판
-- =====================================================
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










-- 공모전 게시판 게시글 해시태그 연결












-- 취업정보 게시판 게시글 해시태그 연결












-- 스터디모집 게시판 게시글 해시태그 연결









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










-- 공모전 게시판 게시글 해시태그 연결












-- 취업정보 게시판 게시글 해시태그 연결












-- 스터디모집 게시판 게시글 해시태그 연결










-- =====================================================
-- 4. V12 데이터 추가: 교수/과제/시험/퀴즈 게시판
-- =====================================================
-- =====================================================
-- 1. 교수 게시판 게시글 (8개)
-- =====================================================
SET @professor_category_id = (SELECT id FROM board_categories WHERE board_type = 'PROFESSOR' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@professor_category_id, 20241001, '신학기 교수회의 안내', '2025학년도 1학기 교수회의를 다음과 같이 개최하오니 참석 부탁드립니다.\n\n일시: 2025년 3월 2일 (화) 14:00\n장소: 본관 3층 대회의실\n\n주요 안건:\n1. 신학기 교육과정 조정\n2. 학생 평가 기준 논의\n3. 연구실적 평가 방안\n\n문의: 기획처 (내선 1111)', 'NORMAL', false, 42, 8, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '[긴급] 연구실적 평가 기준 개정안', '교수님들께 공지드립니다.\n\n연구실적 평가 기준이 다음과 같이 개정되었습니다.\n\n주요 변경사항:\n- SCI급 논문: 10점 → 15점\n- 국내 학술지: 5점 → 7점\n- 저서: 20점 (신설)\n- 특허: 8점 → 10점\n\n시행일: 2025년 1월 1일부터\n문의: 연구처 (내선 1234)', 'URGENT', false, 67, 12, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, 'LMS 플랫폼 업그레이드 안내', '안녕하세요, 교수님들.\n\nLMS 시스템이 다음 주말에 업그레이드 됩니다.\n\n업그레이드 일정:\n- 2025년 1월 18일 (토) 22:00 ~ 1월 19일 (일) 06:00\n- 약 8시간 예상\n\n신규 기능:\n- AI 기반 학생 학습 분석\n- 실시간 출결 체크\n- 과제 표절 검사 고도화\n- 화상 강의 녹화 자동 저장\n\n사전 교육: 1월 15일 (수) 15:00 온라인 진행', 'NORMAL', false, 53, 9, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '학생 상담 가이드라인 공유', '교수님들께 학생 상담 시 유의사항을 공유드립니다.\n\n1. 개인정보 보호\n- 상담 내용은 철저히 비밀 유지\n- 학생 동의 없이 제3자 공유 금지\n\n2. 위기 학생 대응\n- 자살/자해 위험: 즉시 학생상담센터 (내선 9999) 연락\n- 정신건강 문제: 전문상담사 연계\n\n3. 학업 상담\n- 성적 저조 학생: 학습법 코칭\n- 진로 고민: 취업지원센터 연계\n\n문의: 학생지원팀', 'NORMAL', false, 38, 7, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '2024-2학기 강의평가 결과 분석', '2024학년도 2학기 강의평가 결과가 나왔습니다.\n\n전체 평균: 4.2/5.0 (전년도 대비 +0.1)\n\n우수 강의 사례:\n- 플립러닝 활용 수업: 평균 4.8\n- 프로젝트 기반 학습: 평균 4.6\n- 토론식 수업: 평균 4.5\n\n개선이 필요한 부분:\n- 과제 피드백 속도 향상\n- 수업 자료 사전 배포\n- 실습 시간 확대\n\n우수 강의 교수님들을 대상으로 워크숍 개최 예정입니다.', 'NORMAL', false, 61, 11, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '2025년 상반기 외부 연구비 신청 안내', '2025년 상반기 외부 연구비 지원 사업을 안내드립니다.\n\n1. 한국연구재단 기본연구\n- 신청기간: 2025년 2월 1일 ~ 3월 15일\n- 지원규모: 5천만원 ~ 2억원\n- 연구기간: 3년\n\n2. 산학협력 연구과제\n- 상시 신청\n- 기업 매칭 필요\n- 지원규모: 협의\n\n3. 국제공동연구\n- 신청기간: 2025년 3월 1일 ~ 4월 30일\n- 해외 파트너 필수\n\n연구지원팀에서 신청서 작성 지원해드립니다.', 'NORMAL', false, 45, 8, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '컴퓨터공학과 교육과정 개편 논의', '컴퓨터공학과 교육과정 개편 관련 의견 수렴\n\n현행 문제점:\n- 이론 중심 편성 (실습 부족)\n- 최신 기술 반영 부족 (AI, 클라우드 등)\n- 산업체 요구 미반영\n\n개선 방향:\n1. 실습 비중 확대 (40% → 60%)\n2. 최신 기술 과목 신설\n   - AI/머신러닝\n   - 클라우드 컴퓨팅\n   - 데브옵스\n3. 캡스톤 디자인 강화\n4. 산학 연계 프로젝트\n\n교수님들의 의견 부탁드립니다.', 'NORMAL', false, 54, 10, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '제10회 교내 학술대회 발표자 모집', '제10회 교내 학술대회를 개최합니다.\n\n행사 개요:\n- 일시: 2025년 4월 15일 (화) 10:00~18:00\n- 장소: 학생회관 대강당\n- 주제: AI와 미래 교육\n\n발표 분야:\n- 교육 방법론\n- 학습 효과 분석\n- 교육 기술 혁신\n- 학생 평가 방법\n\n혜택:\n- 우수 발표 시상\n- 논문집 게재\n- 연구실적 인정\n\n신청: 3월 15일까지', 'NORMAL', false, 37, 6, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001);


-- =====================================================
-- 2. 과제 게시판 게시글 + assignments (6개)
-- =====================================================
SET @assignment_category_id = (SELECT id FROM board_categories WHERE board_type = 'ASSIGNMENT' LIMIT 1);

-- 과제 게시글 생성
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@assignment_category_id, 20241001, '[데이터구조] 연결리스트 구현 과제', '단일 연결리스트(Singly Linked List)를 구현하는 과제입니다.\n\n구현 기능:\n1. 노드 추가 (append, prepend, insert)\n2. 노드 삭제 (remove, removeAt)\n3. 노드 검색 (find, indexOf)\n4. 리스트 순회 (traverse)\n\n제출 형식: Java 또는 C++ 소스코드\n테스트 케이스 포함\n\n배점: 100점\n마감일: 2025년 2월 15일 23:59', 'NORMAL', false, 89, 15, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[알고리즘] 정렬 알고리즘 비교 분석', '다양한 정렬 알고리즘의 성능을 비교 분석하는 과제입니다.\n\n분석 대상:\n- 버블 정렬 (Bubble Sort)\n- 선택 정렬 (Selection Sort)\n- 삽입 정렬 (Insertion Sort)\n- 퀵 정렬 (Quick Sort)\n- 병합 정렬 (Merge Sort)\n\n분석 항목:\n1. 시간 복잡도 (최선/평균/최악)\n2. 공간 복잡도\n3. 안정성(Stability)\n4. 실제 실행 시간 측정\n\n제출 형식: 보고서(PDF) + 소스코드\n배점: 100점', 'NORMAL', false, 76, 12, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[데이터베이스] ERD 설계 프로젝트', '도서 대출 관리 시스템의 ERD를 설계하는 과제입니다.\n\n필수 엔티티:\n- 회원 (Member)\n- 도서 (Book)\n- 대출 (Loan)\n- 예약 (Reservation)\n- 카테고리 (Category)\n\n설계 요구사항:\n1. 정규화 3NF 이상\n2. 적절한 관계 설정\n3. 제약조건 명시\n4. 인덱스 계획\n\n제출 형식: ERD 다이어그램 + 설명서\n도구: ERDCloud 또는 Draw.io\n배점: 100점', 'NORMAL', false, 94, 18, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[웹개발] React 포트폴리오 사이트 제작', 'React를 활용한 개인 포트폴리오 웹사이트 제작 과제입니다.\n\n필수 구현 기능:\n1. 소개 페이지 (About)\n2. 프로젝트 목록 (Projects)\n3. 기술 스택 (Skills)\n4. 연락처 (Contact)\n5. 반응형 디자인\n\n기술 스택:\n- React 18+\n- React Router\n- CSS Modules 또는 Styled Components\n- 선택: Redux, TypeScript\n\n제출 방법: GitHub 저장소 링크\n배점: 100점\n마감일: 2025년 3월 1일 23:59', 'NORMAL', false, 112, 21, false, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[운영체제] 프로세스 스케줄링 시뮬레이션', '다양한 스케줄링 알고리즘을 시뮬레이션하는 프로그램 작성 과제입니다.\n\n구현 알고리즘:\n1. FCFS (First Come First Served)\n2. SJF (Shortest Job First)\n3. Priority Scheduling\n4. Round Robin\n5. Multilevel Queue\n\n출력 결과:\n- 각 프로세스의 대기 시간\n- 평균 대기 시간\n- 평균 반환 시간\n- 간트 차트\n\n제출 형식: 소스코드 + 실행 결과 보고서\n배점: 100점', 'NORMAL', false, 87, 16, false, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[네트워크] 패킷 분석 보고서', 'Wireshark를 사용한 네트워크 패킷 분석 과제입니다.\n\n분석 내용:\n1. HTTP/HTTPS 트래픽 분석\n2. DNS 쿼리 분석\n3. TCP 3-way Handshake 관찰\n4. 패킷 손실 및 재전송 분석\n5. 네트워크 지연 측정\n\n보고서 구성:\n- 캡처 환경 설명\n- 각 프로토콜별 상세 분석\n- 스크린샷 및 설명\n- 결론 및 고찰\n\n제출 형식: PDF 보고서 + PCAP 파일\n배점: 100점\n마감일: 2025년 2월 28일 23:59', 'NORMAL', false, 71, 13, false, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 20241001, 20241001);

-- 과제 상세 정보 생성
INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, instructions, created_by, created_at) 
SELECT 
    p.id,
    1, -- course_id
    NOW() + INTERVAL 30 DAY,
    100.00,
    'UPLOAD',
    CONCAT('과제 제출 안내:\n1. 제출 형식을 준수해주세요\n2. 표절 검사가 진행됩니다\n3. 기한 내 제출하세요'),
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @assignment_category_id 
AND p.title LIKE '[데이터구조]%'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, instructions, created_by, created_at) 
SELECT 
    p.id,
    1,
    NOW() + INTERVAL 35 DAY,
    100.00,
    'UPLOAD',
    '과제 제출 안내:\n1. 코드 주석을 충분히 작성하세요\n2. 보고서는 A4 10페이지 이내\n3. 실험 데이터를 포함하세요',
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @assignment_category_id 
AND p.title LIKE '[알고리즘]%'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, instructions, created_by, created_at) 
SELECT 
    p.id,
    2,
    NOW() + INTERVAL 40 DAY,
    100.00,
    'UPLOAD',
    'ERD 설계 가이드:\n1. 엔티티 관계를 명확히 표현\n2. 카디널리티 표시\n3. 제약조건 문서화',
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @assignment_category_id 
AND p.title LIKE '[데이터베이스]%'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, instructions, created_by, created_at) 
SELECT 
    p.id,
    2,
    NOW() + INTERVAL 45 DAY,
    100.00,
    'LINK',
    'React 프로젝트 제출 안내:\n1. README 파일 작성 필수\n2. 실행 방법 명시\n3. 배포 링크 제공 (선택)',
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @assignment_category_id 
AND p.title LIKE '[웹개발]%'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, instructions, created_by, created_at) 
SELECT 
    p.id,
    3,
    NOW() + INTERVAL 50 DAY,
    100.00,
    'UPLOAD',
    '프로세스 스케줄링 과제:\n1. 각 알고리즘 구현\n2. 성능 비교 분석\n3. 간트 차트 출력',
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @assignment_category_id 
AND p.title LIKE '[운영체제]%'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, instructions, created_by, created_at) 
SELECT 
    p.id,
    3,
    NOW() + INTERVAL 55 DAY,
    100.00,
    'UPLOAD',
    '패킷 분석 보고서:\n1. Wireshark 캡처 파일 필수\n2. 각 프로토콜 상세 분석\n3. 스크린샷 포함',
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @assignment_category_id 
AND p.title LIKE '[네트워크]%'
LIMIT 1;

-- 학생 과제 제출 (일부 예시)
INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at)
SELECT 
    a.id,
    20250101001,
    '연결리스트를 Java로 구현했습니다. 모든 기능이 정상 작동합니다.',
    'SUBMITTED',
    NOW() - INTERVAL 5 DAY,
    NOW() - INTERVAL 5 DAY
FROM assignments a
JOIN posts p ON a.post_id = p.id
WHERE p.title LIKE '[데이터구조]%'
LIMIT 1;


-- =====================================================
-- 3. 시험 게시판 게시글 + exams (4개)
-- =====================================================
SET @exam_category_id = (SELECT id FROM board_categories WHERE board_type = 'EXAM' LIMIT 1);

-- 시험 게시글 생성
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@exam_category_id, 20241001, '[데이터구조] 중간고사 안내', '데이터구조 과목 중간고사 안내입니다.\n\n시험 일시: 2025년 3월 15일 (토) 10:00~11:30\n시험 장소: 공학관 301호\n시험 범위: 1주차~7주차 (배열, 리스트, 스택, 큐)\n\n시험 형식:\n- 객관식 20문항 (40점)\n- 주관식 4문항 (40점)\n- 코딩 문제 1문항 (20점)\n\n지참물: 신분증, 필기구\n주의사항: 전자기기 사용 금지', 'NORMAL', false, 156, 28, false, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY, 20241001, 20241001),

(@exam_category_id, 20241001, '[데이터구조] 기말고사 안내', '데이터구조 과목 기말고사 안내입니다.\n\n시험 일시: 2025년 6월 20일 (금) 10:00~11:30\n시험 장소: 공학관 301호\n시험 범위: 8주차~15주차 (트리, 그래프, 정렬, 해싱)\n\n시험 형식:\n- 객관식 15문항 (30점)\n- 주관식 5문항 (40점)\n- 코딩 문제 2문항 (30점)\n\n지참물: 신분증, 필기구, 계산기\n주의사항: 오픈북 시험', 'NORMAL', false, 142, 25, false, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 20241001, 20241001),

(@exam_category_id, 20241001, '[알고리즘] 중간고사 안내', '알고리즘 과목 중간고사 안내입니다.\n\n시험 일시: 2025년 3월 18일 (화) 14:00~15:30\n시험 장소: 공학관 401호\n시험 범위: 시간복잡도, 재귀, 정렬, 탐색\n\n시험 형식:\n- 객관식 15문항 (30점)\n- 알고리즘 분석 3문항 (30점)\n- 코딩 문제 2문항 (40점)\n\n지참물: 신분증, 필기구\n주의사항: 코딩 문제는 의사코드 허용', 'NORMAL', false, 134, 22, false, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 28 DAY, 20241001, 20241001),

(@exam_category_id, 20241001, '[알고리즘] 기말고사 안내', '알고리즘 과목 기말고사 안내입니다.\n\n시험 일시: 2025년 6월 22일 (일) 14:00~15:30\n시험 장소: 공학관 401호\n시험 범위: 동적계획법, 그리디, 그래프, NP 문제\n\n시험 형식:\n- 객관식 10문항 (20점)\n- 알고리즘 분석 4문항 (40점)\n- 코딩 문제 2문항 (40점)\n\n지참물: 신분증, 필기구\n주의사항: 시간 엄수', 'NORMAL', false, 128, 20, false, NOW() - INTERVAL 35 DAY, NOW() - INTERVAL 35 DAY, 20241001, 20241001);

-- 시험 상세 정보 생성
INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    1,
    'MIDTERM',
    '2025-03-15 10:00:00',
    90,
    100.00,
    false,
    60.00,
    25,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @exam_category_id 
AND p.title LIKE '[데이터구조] 중간고사%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    1,
    'FINAL',
    '2025-06-20 10:00:00',
    90,
    100.00,
    false,
    60.00,
    22,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @exam_category_id 
AND p.title LIKE '[데이터구조] 기말고사%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    2,
    'MIDTERM',
    '2025-03-18 14:00:00',
    90,
    100.00,
    false,
    60.00,
    20,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @exam_category_id 
AND p.title LIKE '[알고리즘] 중간고사%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    2,
    'FINAL',
    '2025-06-22 14:00:00',
    90,
    100.00,
    false,
    60.00,
    16,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @exam_category_id 
AND p.title LIKE '[알고리즘] 기말고사%'
LIMIT 1;

-- 학생 시험 결과 (일부 예시)
INSERT INTO exam_results (exam_id, user_id, score, started_at, submitted_at, answer_data, created_at)
SELECT 
    e.id,
    20250101001,
    85.50,
    '2025-03-15 10:00:00',
    '2025-03-15 11:15:00',
    '{"answers": ["A", "B", "C"], "essay": ["답안1", "답안2"]}',
    NOW()
FROM exams e
JOIN posts p ON e.post_id = p.id
WHERE p.title LIKE '[데이터구조] 중간고사%'
LIMIT 1;


-- =====================================================
-- 4. 퀴즈 게시판 게시글 + exams (6개)
-- =====================================================
SET @quiz_category_id = (SELECT id FROM board_categories WHERE board_type = 'QUIZ' LIMIT 1);

-- 퀴즈 게시글 생성
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@quiz_category_id, 20241001, '[데이터구조] 1주차 퀴즈 - 배열과 리스트', '1주차 학습 내용 퀴즈입니다.\n\n범위: 배열과 리스트의 차이점\n문항 수: 10문제\n시간: 15분\n배점: 10점\n\n퀴즈 시작 후 제한시간 내에 제출하세요.\n재시험 불가능합니다.', 'NORMAL', false, 98, 12, false, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[알고리즘] 2주차 퀴즈 - 시간복잡도', '2주차 학습 내용 퀴즈입니다.\n\n범위: Big-O 표기법, 시간복잡도 분석\n문항 수: 15문제\n시간: 20분\n배점: 15점\n\n알고리즘의 효율성을 분석하는 문제입니다.\n계산기 사용 가능합니다.', 'NORMAL', false, 87, 10, false, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[데이터베이스] 3주차 퀴즈 - SQL 기초', '3주차 SQL 기초 퀴즈입니다.\n\n범위: SELECT, WHERE, JOIN 구문\n문항 수: 12문제\n시간: 25분\n배점: 20점\n\nSQL 쿼리 작성 문제가 포함되어 있습니다.\n문법을 정확히 작성하세요.', 'NORMAL', false, 102, 14, false, NOW() - INTERVAL 36 DAY, NOW() - INTERVAL 36 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[웹개발] 4주차 퀴즈 - HTML/CSS', '4주차 HTML과 CSS 기초 퀴즈입니다.\n\n범위: HTML 태그, CSS 선택자, 박스모델\n문항 수: 10문제\n시간: 15분\n배점: 10점\n\n코드 작성 문제가 포함되어 있습니다.\n들여쓰기에 주의하세요.', 'NORMAL', false, 94, 11, false, NOW() - INTERVAL 34 DAY, NOW() - INTERVAL 34 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[운영체제] 5주차 퀴즈 - 프로세스', '5주차 프로세스 관련 퀴즈입니다.\n\n범위: 프로세스 상태, PCB, 문맥교환\n문항 수: 12문제\n시간: 20분\n배점: 15점\n\n프로세스 상태 전이도를 이해하고 있어야 합니다.\n그림 문제가 포함되어 있습니다.', 'NORMAL', false, 89, 9, false, NOW() - INTERVAL 32 DAY, NOW() - INTERVAL 32 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[네트워크] 6주차 퀴즈 - OSI 7계층', '6주차 OSI 7계층 퀴즈입니다.\n\n범위: OSI 7계층 구조, 각 계층의 역할\n문항 수: 14문제\n시간: 20분\n배점: 15점\n\n각 계층의 프로토콜과 장비를 정확히 알고 있어야 합니다.\n약어를 풀어쓰는 문제가 있습니다.', 'NORMAL', false, 96, 13, false, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 20241001, 20241001);

-- 퀴즈 상세 정보 생성 (exam_type='QUIZ')
INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    1,
    'QUIZ',
    NOW() + INTERVAL 5 DAY,
    15,
    10.00,
    true,
    6.00,
    10,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @quiz_category_id 
AND p.title LIKE '[데이터구조]%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    1,
    'QUIZ',
    NOW() + INTERVAL 7 DAY,
    20,
    15.00,
    true,
    9.00,
    15,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @quiz_category_id 
AND p.title LIKE '[알고리즘]%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    2,
    'QUIZ',
    NOW() + INTERVAL 10 DAY,
    25,
    20.00,
    true,
    12.00,
    12,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @quiz_category_id 
AND p.title LIKE '[데이터베이스]%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    2,
    'QUIZ',
    NOW() + INTERVAL 12 DAY,
    15,
    10.00,
    true,
    6.00,
    10,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @quiz_category_id 
AND p.title LIKE '[웹개발]%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    3,
    'QUIZ',
    NOW() + INTERVAL 14 DAY,
    20,
    15.00,
    true,
    9.00,
    12,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @quiz_category_id 
AND p.title LIKE '[운영체제]%'
LIMIT 1;

INSERT INTO exams (post_id, course_id, exam_type, exam_date, duration_minutes, total_score, is_online, passing_score, question_count, created_by, created_at)
SELECT 
    p.id,
    3,
    'QUIZ',
    NOW() + INTERVAL 16 DAY,
    20,
    15.00,
    true,
    9.00,
    14,
    20241001,
    NOW()
FROM posts p 
WHERE p.category_id = @quiz_category_id 
AND p.title LIKE '[네트워크]%'
LIMIT 1;

-- 학생 퀴즈 결과 (일부 예시)
INSERT INTO exam_results (exam_id, user_id, score, started_at, submitted_at, answer_data, created_at)
SELECT 
    e.id,
    20250101001,
    8.00,
    NOW() - INTERVAL 5 DAY,
    NOW() - INTERVAL 5 DAY + INTERVAL 12 MINUTE,
    '{"answers": [1, 2, 1, 3, 2, 1, 4, 2, 3, 1]}',
    NOW()
FROM exams e
JOIN posts p ON e.post_id = p.id
WHERE p.title LIKE '[데이터구조] 1주차 퀴즈%'
LIMIT 1;

-- =====================================================
-- 데이터 생성 완료 확인
-- =====================================================
SELECT 
    '교수 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @professor_category_id AND is_deleted = false

UNION ALL

SELECT 
    '과제 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @assignment_category_id AND is_deleted = false

UNION ALL

SELECT 
    '시험 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @exam_category_id AND is_deleted = false

UNION ALL

SELECT 
    '퀴즈 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @quiz_category_id AND is_deleted = false;

SELECT 
    '과제 정보' as table_name,
    COUNT(*) as record_count 
FROM assignments

UNION ALL

SELECT 
    '과제 제출' as table_name,
    COUNT(*) as record_count 
FROM assignment_submissions

UNION ALL

SELECT 
    '시험/퀴즈 정보' as table_name,
    COUNT(*) as record_count 
FROM exams

UNION ALL

SELECT 
    '시험/퀴즈 결과' as table_name,
    COUNT(*) as record_count 
FROM exam_results;
-- =====================================================
-- 1. 교수 게시판 게시글 (8개)
-- =====================================================
SET @professor_category_id = (SELECT id FROM board_categories WHERE board_type = 'PROFESSOR' LIMIT 1);

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@professor_category_id, 20241001, '신학기 교수회의 안내', '2025학년도 1학기 교수회의를 다음과 같이 개최하오니 참석 부탁드립니다.\n\n일시: 2025년 3월 2일 (화) 14:00\n장소: 본관 3층 대회의실\n\n주요 안건:\n1. 신학기 교육과정 조정\n2. 학생 평가 기준 논의\n3. 연구실적 평가 방안\n\n문의: 기획처 (내선 1111)', 'NORMAL', false, 42, 8, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '[긴급] 연구실적 평가 기준 개정안', '교수님들께 공지드립니다.\n\n연구실적 평가 기준이 다음과 같이 개정되었습니다.\n\n주요 변경사항:\n- SCI급 논문: 10점 → 15점\n- 국내 학술지: 5점 → 7점\n- 저서: 20점 (신설)\n- 특허: 8점 → 10점\n\n시행일: 2025년 1월 1일부터\n문의: 연구처 (내선 1234)', 'URGENT', false, 67, 12, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, 'LMS 플랫폼 업그레이드 안내', '안녕하세요, 교수님들.\n\nLMS 시스템이 다음 주말에 업그레이드 됩니다.\n\n업그레이드 일정:\n- 2025년 1월 18일 (토) 22:00 ~ 1월 19일 (일) 06:00\n- 약 8시간 예상\n\n신규 기능:\n- AI 기반 학생 학습 분석\n- 실시간 출결 체크\n- 과제 표절 검사 고도화\n- 화상 강의 녹화 자동 저장\n\n사전 교육: 1월 15일 (수) 15:00 온라인 진행', 'NORMAL', false, 53, 9, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '학생 상담 가이드라인 공유', '교수님들께 학생 상담 시 유의사항을 공유드립니다.\n\n1. 개인정보 보호\n- 상담 내용은 철저히 비밀 유지\n- 학생 동의 없이 제3자 공유 금지\n\n2. 위기 학생 대응\n- 자살/자해 위험: 즉시 학생상담센터 (내선 9999) 연락\n- 정신건강 문제: 전문상담사 연계\n\n3. 학업 상담\n- 성적 저조 학생: 학습법 코칭\n- 진로 고민: 취업지원센터 연계\n\n문의: 학생지원팀', 'NORMAL', false, 38, 7, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '2024-2학기 강의평가 결과 분석', '2024학년도 2학기 강의평가 결과가 나왔습니다.\n\n전체 평균: 4.2/5.0 (전년도 대비 +0.1)\n\n우수 강의 사례:\n- 플립러닝 활용 수업: 평균 4.8\n- 프로젝트 기반 학습: 평균 4.6\n- 토론식 수업: 평균 4.5\n\n개선이 필요한 부분:\n- 과제 피드백 속도 향상\n- 수업 자료 사전 배포\n- 실습 시간 확대\n\n우수 강의 교수님들을 대상으로 워크숍 개최 예정입니다.', 'NORMAL', false, 61, 11, false, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '2025년 상반기 외부 연구비 신청 안내', '2025년 상반기 외부 연구비 지원 사업을 안내드립니다.\n\n1. 한국연구재단 기본연구\n- 신청기간: 2025년 2월 1일 ~ 3월 15일\n- 지원규모: 5천만원 ~ 2억원\n- 연구기간: 3년\n\n2. 산학협력 연구과제\n- 상시 신청\n- 기업 매칭 필요\n- 지원규모: 협의\n\n3. 국제공동연구\n- 신청기간: 2025년 3월 1일 ~ 4월 30일\n- 해외 파트너 필수\n\n연구지원팀에서 신청서 작성 지원해드립니다.', 'NORMAL', false, 45, 8, false, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '컴퓨터공학과 교육과정 개편 논의', '컴퓨터공학과 교육과정 개편 관련 의견 수렴\n\n현행 문제점:\n- 이론 중심 편성 (실습 부족)\n- 최신 기술 반영 부족 (AI, 클라우드 등)\n- 산업체 요구 미반영\n\n개선 방향:\n1. 실습 비중 확대 (40% → 60%)\n2. 최신 기술 과목 신설\n   - AI/머신러닝\n   - 클라우드 컴퓨팅\n   - 데브옵스\n3. 캡스톤 디자인 강화\n4. 산학 연계 프로젝트\n\n교수님들의 의견 부탁드립니다.', 'NORMAL', false, 54, 10, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001),

(@professor_category_id, 20241001, '제10회 교내 학술대회 발표자 모집', '제10회 교내 학술대회를 개최합니다.\n\n행사 개요:\n- 일시: 2025년 4월 15일 (화) 10:00~18:00\n- 장소: 학생회관 대강당\n- 주제: AI와 미래 교육\n\n발표 분야:\n- 교육 방법론\n- 학습 효과 분석\n- 교육 기술 혁신\n- 학생 평가 방법\n\n혜택:\n- 우수 발표 시상\n- 논문집 게재\n- 연구실적 인정\n\n신청: 3월 15일까지', 'NORMAL', false, 37, 6, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001);


-- =====================================================
-- 2. 과제 게시판 게시글 + assignments (6개)
-- =====================================================
SET @assignment_category_id = (SELECT id FROM board_categories WHERE board_type = 'ASSIGNMENT' LIMIT 1);

-- 과제 게시글 생성
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@assignment_category_id, 20241001, '[데이터구조] 연결리스트 구현 과제', '단일 연결리스트(Singly Linked List)를 구현하는 과제입니다.\n\n구현 기능:\n1. 노드 추가 (append, prepend, insert)\n2. 노드 삭제 (remove, removeAt)\n3. 노드 검색 (find, indexOf)\n4. 리스트 순회 (traverse)\n\n제출 형식: Java 또는 C++ 소스코드\n테스트 케이스 포함\n\n배점: 100점\n마감일: 2025년 2월 15일 23:59', 'NORMAL', false, 89, 15, false, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[알고리즘] 정렬 알고리즘 비교 분석', '다양한 정렬 알고리즘의 성능을 비교 분석하는 과제입니다.\n\n분석 대상:\n- 버블 정렬 (Bubble Sort)\n- 선택 정렬 (Selection Sort)\n- 삽입 정렬 (Insertion Sort)\n- 퀵 정렬 (Quick Sort)\n- 병합 정렬 (Merge Sort)\n\n분석 항목:\n1. 시간 복잡도 (최선/평균/최악)\n2. 공간 복잡도\n3. 안정성(Stability)\n4. 실제 실행 시간 측정\n\n제출 형식: 보고서(PDF) + 소스코드\n배점: 100점', 'NORMAL', false, 76, 12, false, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 12 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[데이터베이스] ERD 설계 프로젝트', '도서 대출 관리 시스템의 ERD를 설계하는 과제입니다.\n\n필수 엔티티:\n- 회원 (Member)\n- 도서 (Book)\n- 대출 (Loan)\n- 예약 (Reservation)\n- 카테고리 (Category)\n\n설계 요구사항:\n1. 정규화 3NF 이상\n2. 적절한 관계 설정\n3. 제약조건 명시\n4. 인덱스 계획\n\n제출 형식: ERD 다이어그램 + 설명서\n도구: ERDCloud 또는 Draw.io\n배점: 100점', 'NORMAL', false, 94, 18, false, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 14 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[웹개발] React 포트폴리오 사이트 제작', 'React를 활용한 개인 포트폴리오 웹사이트 제작 과제입니다.\n\n필수 구현 기능:\n1. 소개 페이지 (About)\n2. 프로젝트 목록 (Projects)\n3. 기술 스택 (Skills)\n4. 연락처 (Contact)\n5. 반응형 디자인\n\n기술 스택:\n- React 18+\n- React Router\n- CSS Modules 또는 Styled Components\n- 선택: Redux, TypeScript\n\n제출 방법: GitHub 저장소 링크\n배점: 100점\n마감일: 2025년 3월 1일 23:59', 'NORMAL', false, 112, 21, false, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 16 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[운영체제] 프로세스 스케줄링 시뮬레이션', '다양한 스케줄링 알고리즘을 시뮬레이션하는 프로그램 작성 과제입니다.\n\n구현 알고리즘:\n1. FCFS (First Come First Served)\n2. SJF (Shortest Job First)\n3. Priority Scheduling\n4. Round Robin\n5. Multilevel Queue\n\n출력 결과:\n- 각 프로세스의 대기 시간\n- 평균 대기 시간\n- 평균 반환 시간\n- 간트 차트\n\n제출 형식: 소스코드 + 실행 결과 보고서\n배점: 100점', 'NORMAL', false, 87, 16, false, NOW() - INTERVAL 18 DAY, NOW() - INTERVAL 18 DAY, 20241001, 20241001),

(@assignment_category_id, 20241001, '[네트워크] 패킷 분석 보고서', 'Wireshark를 사용한 네트워크 패킷 분석 과제입니다.\n\n분석 내용:\n1. HTTP/HTTPS 트래픽 분석\n2. DNS 쿼리 분석\n3. TCP 3-way Handshake 관찰\n4. 패킷 손실 및 재전송 분석\n5. 네트워크 지연 측정\n\n보고서 구성:\n- 캡처 환경 설명\n- 각 프로토콜별 상세 분석\n- 스크린샷 및 설명\n- 결론 및 고찰\n\n제출 형식: PDF 보고서 + PCAP 파일\n배점: 100점\n마감일: 2025년 2월 28일 23:59', 'NORMAL', false, 71, 13, false, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 20 DAY, 20241001, 20241001);

-- 과제 상세 정보 생성












-- 학생 과제 제출 (일부 예시)


-- =====================================================
-- 3. 시험 게시판 게시글 + exams (4개)
-- =====================================================
SET @exam_category_id = (SELECT id FROM board_categories WHERE board_type = 'EXAM' LIMIT 1);

-- 시험 게시글 생성
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@exam_category_id, 20241001, '[데이터구조] 중간고사 안내', '데이터구조 과목 중간고사 안내입니다.\n\n시험 일시: 2025년 3월 15일 (토) 10:00~11:30\n시험 장소: 공학관 301호\n시험 범위: 1주차~7주차 (배열, 리스트, 스택, 큐)\n\n시험 형식:\n- 객관식 20문항 (40점)\n- 주관식 4문항 (40점)\n- 코딩 문제 1문항 (20점)\n\n지참물: 신분증, 필기구\n주의사항: 전자기기 사용 금지', 'NORMAL', false, 156, 28, false, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY, 20241001, 20241001),

(@exam_category_id, 20241001, '[데이터구조] 기말고사 안내', '데이터구조 과목 기말고사 안내입니다.\n\n시험 일시: 2025년 6월 20일 (금) 10:00~11:30\n시험 장소: 공학관 301호\n시험 범위: 8주차~15주차 (트리, 그래프, 정렬, 해싱)\n\n시험 형식:\n- 객관식 15문항 (30점)\n- 주관식 5문항 (40점)\n- 코딩 문제 2문항 (30점)\n\n지참물: 신분증, 필기구, 계산기\n주의사항: 오픈북 시험', 'NORMAL', false, 142, 25, false, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 20241001, 20241001),

(@exam_category_id, 20241001, '[알고리즘] 중간고사 안내', '알고리즘 과목 중간고사 안내입니다.\n\n시험 일시: 2025년 3월 18일 (화) 14:00~15:30\n시험 장소: 공학관 401호\n시험 범위: 시간복잡도, 재귀, 정렬, 탐색\n\n시험 형식:\n- 객관식 15문항 (30점)\n- 알고리즘 분석 3문항 (30점)\n- 코딩 문제 2문항 (40점)\n\n지참물: 신분증, 필기구\n주의사항: 코딩 문제는 의사코드 허용', 'NORMAL', false, 134, 22, false, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 28 DAY, 20241001, 20241001),

(@exam_category_id, 20241001, '[알고리즘] 기말고사 안내', '알고리즘 과목 기말고사 안내입니다.\n\n시험 일시: 2025년 6월 22일 (일) 14:00~15:30\n시험 장소: 공학관 401호\n시험 범위: 동적계획법, 그리디, 그래프, NP 문제\n\n시험 형식:\n- 객관식 10문항 (20점)\n- 알고리즘 분석 4문항 (40점)\n- 코딩 문제 2문항 (40점)\n\n지참물: 신분증, 필기구\n주의사항: 시간 엄수', 'NORMAL', false, 128, 20, false, NOW() - INTERVAL 35 DAY, NOW() - INTERVAL 35 DAY, 20241001, 20241001);

-- 시험 상세 정보 생성




-- 학생 시험 결과 (일부 예시)


-- =====================================================
-- 4. 퀴즈 게시판 게시글 + exams (6개)
-- =====================================================
SET @quiz_category_id = (SELECT id FROM board_categories WHERE board_type = 'QUIZ' LIMIT 1);

-- 퀴즈 게시글 생성
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@quiz_category_id, 20241001, '[데이터구조] 1주차 퀴즈 - 배열과 리스트', '1주차 학습 내용 퀴즈입니다.\n\n범위: 배열과 리스트의 차이점\n문항 수: 10문제\n시간: 15분\n배점: 10점\n\n퀴즈 시작 후 제한시간 내에 제출하세요.\n재시험 불가능합니다.', 'NORMAL', false, 98, 12, false, NOW() - INTERVAL 40 DAY, NOW() - INTERVAL 40 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[알고리즘] 2주차 퀴즈 - 시간복잡도', '2주차 학습 내용 퀴즈입니다.\n\n범위: Big-O 표기법, 시간복잡도 분석\n문항 수: 15문제\n시간: 20분\n배점: 15점\n\n알고리즘의 효율성을 분석하는 문제입니다.\n계산기 사용 가능합니다.', 'NORMAL', false, 87, 10, false, NOW() - INTERVAL 38 DAY, NOW() - INTERVAL 38 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[데이터베이스] 3주차 퀴즈 - SQL 기초', '3주차 SQL 기초 퀴즈입니다.\n\n범위: SELECT, WHERE, JOIN 구문\n문항 수: 12문제\n시간: 25분\n배점: 20점\n\nSQL 쿼리 작성 문제가 포함되어 있습니다.\n문법을 정확히 작성하세요.', 'NORMAL', false, 102, 14, false, NOW() - INTERVAL 36 DAY, NOW() - INTERVAL 36 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[웹개발] 4주차 퀴즈 - HTML/CSS', '4주차 HTML과 CSS 기초 퀴즈입니다.\n\n범위: HTML 태그, CSS 선택자, 박스모델\n문항 수: 10문제\n시간: 15분\n배점: 10점\n\n코드 작성 문제가 포함되어 있습니다.\n들여쓰기에 주의하세요.', 'NORMAL', false, 94, 11, false, NOW() - INTERVAL 34 DAY, NOW() - INTERVAL 34 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[운영체제] 5주차 퀴즈 - 프로세스', '5주차 프로세스 관련 퀴즈입니다.\n\n범위: 프로세스 상태, PCB, 문맥교환\n문항 수: 12문제\n시간: 20분\n배점: 15점\n\n프로세스 상태 전이도를 이해하고 있어야 합니다.\n그림 문제가 포함되어 있습니다.', 'NORMAL', false, 89, 9, false, NOW() - INTERVAL 32 DAY, NOW() - INTERVAL 32 DAY, 20241001, 20241001),

(@quiz_category_id, 20241001, '[네트워크] 6주차 퀴즈 - OSI 7계층', '6주차 OSI 7계층 퀴즈입니다.\n\n범위: OSI 7계층 구조, 각 계층의 역할\n문항 수: 14문제\n시간: 20분\n배점: 15점\n\n각 계층의 프로토콜과 장비를 정확히 알고 있어야 합니다.\n약어를 풀어쓰는 문제가 있습니다.', 'NORMAL', false, 96, 13, false, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY, 20241001, 20241001);

-- 퀴즈 상세 정보 생성 (exam_type='QUIZ')






-- 학생 퀴즈 결과 (일부 예시)

-- =====================================================
-- 데이터 생성 완료 확인
-- =====================================================
SELECT 
    '교수 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @professor_category_id AND is_deleted = false

UNION ALL

SELECT 
    '과제 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @assignment_category_id AND is_deleted = false

UNION ALL

SELECT 
    '시험 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @exam_category_id AND is_deleted = false

UNION ALL

SELECT 
    '퀴즈 게시판' as board_name,
    COUNT(*) as post_count 
FROM posts 
WHERE category_id = @quiz_category_id AND is_deleted = false;

SELECT 
    '과제 정보' as table_name,
    COUNT(*) as record_count 
FROM assignments

UNION ALL

SELECT 
    '과제 제출' as table_name,
    COUNT(*) as record_count 
FROM assignment_submissions

UNION ALL

SELECT 
    '시험/퀴즈 정보' as table_name,
    COUNT(*) as record_count 
FROM exams

UNION ALL

SELECT 
    '시험/퀴즈 결과' as table_name,
    COUNT(*) as record_count 
FROM exam_results;

-- =====================================================
-- 5. V13 데이터 추가: 추가 과제 더미 데이터
-- =====================================================
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '자료구조 과제 1: 연결 리스트 구현',
    '단일 연결 리스트와 이중 연결 리스트를 구현하고 다음 기능을 작성하세요.\n\n1. 노드 삽입 (앞, 뒤, 중간)\n2. 노드 삭제\n3. 노드 검색\n4. 리스트 출력\n\n제출물: 소스코드(.java 또는 .cpp), 실행 결과 캡처',
    'ASSIGNMENT',
    false,
    45,
    3,
    false,
    NOW() - INTERVAL 10 DAY,
    NOW() - INTERVAL 10 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '자료구조 과제 2: 스택과 큐 응용',
    '스택과 큐를 이용하여 다음 문제를 해결하세요.\n\n1. 괄호 검사 프로그램 (스택)\n2. 은행 대기열 시뮬레이션 (큐)\n3. 회문 검사 프로그램 (스택+큐)\n\n제출물: 소스코드, 실행 결과, 보고서(PDF)',
    'ASSIGNMENT',
    false,
    38,
    2,
    false,
    NOW() - INTERVAL 9 DAY,
    NOW() - INTERVAL 9 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '자료구조 과제 3: 이진 탐색 트리 구현',
    '이진 탐색 트리(BST)를 구현하고 다음 기능을 작성하세요.\n\n1. 노드 삽입\n2. 노드 삭제\n3. 노드 검색\n4. 중위/전위/후위 순회\n5. 트리의 높이 계산\n\n제출물: 소스코드, 테스트 케이스, 실행 결과',
    'ASSIGNMENT',
    false,
    32,
    1,
    false,
    NOW() - INTERVAL 8 DAY,
    NOW() - INTERVAL 8 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '알고리즘 과제 1: 정렬 알고리즘 성능 비교',
    '다음 정렬 알고리즘을 구현하고 성능을 비교 분석하세요.\n\n1. 버블 정렬\n2. 선택 정렬\n3. 삽입 정렬\n4. 퀵 정렬\n5. 병합 정렬\n\n데이터 크기별(100, 1000, 10000) 실행 시간을 측정하고 그래프로 표현하세요.\n\n제출물: 소스코드, 성능 분석 보고서(그래프 포함)',
    'ASSIGNMENT',
    false,
    51,
    4,
    false,
    NOW() - INTERVAL 7 DAY,
    NOW() - INTERVAL 7 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '알고리즘 과제 2: 동적 계획법 문제 풀이',
    '동적 계획법을 이용하여 다음 문제를 해결하세요.\n\n1. 피보나치 수열 (Top-down, Bottom-up)\n2. 최장 공통 부분 수열(LCS)\n3. 0-1 배낭 문제\n4. 최단 경로 문제 (Floyd-Warshall)\n\n각 문제에 대한 시간복잡도 분석을 포함하세요.\n\n제출물: 소스코드, 시간복잡도 분석 보고서',
    'ASSIGNMENT',
    false,
    67,
    8,
    false,
    NOW() - INTERVAL 15 DAY,
    NOW() - INTERVAL 15 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

-- 과제 테이블 데이터 (posts와 연결)










-- 과제 제출 더미 데이터
-- 과제 1: 3명 제출 (1명 채점 완료, 2명 미채점)
INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101001,
    '연결 리스트 구현 완료했습니다.\n\n단일 연결 리스트와 이중 연결 리스트 모두 구현했으며,\n요구사항의 모든 기능을 테스트했습니다.\n\n첨부파일:\n- LinkedList.java\n- DoublyLinkedList.java\n- TestResult.png',
    'GRADED',
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 1 DAY
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '자료구조 과제 1: 연결 리스트 구현' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101002,
    '과제 제출합니다.\n\n연결 리스트 구현 및 테스트 완료했습니다.\n코드에 주석을 상세히 작성했습니다.',
    'SUBMITTED',
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 1 DAY
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '자료구조 과제 1: 연결 리스트 구현' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101003,
    '연결 리스트 과제 제출합니다.\n\nC++로 구현했으며 메모리 누수 없이 동작합니다.',
    'SUBMITTED',
    NOW() - INTERVAL 3 HOUR,
    NOW() - INTERVAL 3 HOUR,
    NOW() - INTERVAL 3 HOUR
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '자료구조 과제 1: 연결 리스트 구현' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

-- 첫 번째 제출 채점 (95점)
UPDATE assignment_submissions 
SET score = 95, 
    feedback = '전반적으로 잘 구현하셨습니다.\n\n장점:\n- 코드가 깔끔하고 가독성이 좋습니다.\n- 모든 기능이 정상 동작합니다.\n- 주석이 잘 작성되어 있습니다.\n\n개선점:\n- 예외 처리가 일부 누락되었습니다. (특히 빈 리스트 처리)\n- 시간복잡도 분석이 보고서에 포함되면 더 좋았을 것 같습니다.\n\n수고하셨습니다!',
    status = 'GRADED',
    graded_at = NOW() - INTERVAL 1 DAY,
    graded_by = 20241001
WHERE assignment_id = (
    SELECT a.id FROM assignments a
    INNER JOIN posts p ON a.post_id = p.id
    WHERE p.title = '자료구조 과제 1: 연결 리스트 구현' AND p.post_type = 'ASSIGNMENT'
    LIMIT 1
) AND user_id = 20250101001;

-- 과제 2: 2명 제출 (모두 미채점)
INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101001,
    '스택과 큐 응용 과제 제출합니다.\n\n괄호 검사, 은행 대기열 시뮬레이션, 회문 검사 모두 구현했습니다.\n보고서에 각 프로그램의 동작 원리와 시간복잡도를 분석했습니다.',
    'SUBMITTED',
    NOW() - INTERVAL 5 HOUR,
    NOW() - INTERVAL 5 HOUR,
    NOW() - INTERVAL 5 HOUR
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '자료구조 과제 2: 스택과 큐 응용' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101002,
    '과제 제출합니다.\n\n요구사항의 3가지 프로그램을 모두 작성했습니다.\n실행 결과 스크린샷도 첨부했습니다.',
    'SUBMITTED',
    NOW() - INTERVAL 2 HOUR,
    NOW() - INTERVAL 2 HOUR,
    NOW() - INTERVAL 2 HOUR
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '자료구조 과제 2: 스택과 큐 응용' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

-- 과제 5 (마감 지난 과제): 2명 제출, 1명 지각 제출
INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101001,
    '동적 계획법 과제 제출합니다.\n\n4가지 문제 모두 Top-down과 Bottom-up 방식으로 구현했습니다.\n시간복잡도 분석도 보고서에 포함했습니다.',
    'GRADED',
    NOW() - INTERVAL 5 DAY,
    NOW() - INTERVAL 5 DAY,
    NOW() - INTERVAL 4 DAY
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101002,
    '과제 제출합니다.\n\n모든 문제를 해결했으며 테스트 케이스도 작성했습니다.',
    'GRADED',
    NOW() - INTERVAL 4 DAY,
    NOW() - INTERVAL 4 DAY,
    NOW() - INTERVAL 3 DAY
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignment_submissions (assignment_id, user_id, content, status, submitted_at, created_at, updated_at)
SELECT 
    a.id,
    20250101003,
    '지각 제출 죄송합니다.\n\n개인 사정으로 늦었지만 최선을 다해 작성했습니다.',
    'LATE',
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 1 DAY
FROM assignments a
INNER JOIN posts p ON a.post_id = p.id
WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

-- 과제 5 제출 채점
UPDATE assignment_submissions 
SET score = 98, 
    feedback = '매우 우수한 과제입니다!\n\n장점:\n- Top-down과 Bottom-up 방식 모두 정확히 구현\n- 시간복잡도 분석이 정확하고 상세함\n- 코드가 효율적이고 최적화되어 있음\n- 테스트 케이스가 다양하고 충분함\n\n거의 완벽합니다. 계속 이런 자세로 학습하세요!',
    status = 'GRADED',
    graded_at = NOW() - INTERVAL 4 DAY,
    graded_by = 20241001
WHERE assignment_id = (
    SELECT a.id FROM assignments a
    INNER JOIN posts p ON a.post_id = p.id
    WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
    LIMIT 1
) AND user_id = 20250101001;

UPDATE assignment_submissions 
SET score = 85, 
    feedback = '전반적으로 양호합니다.\n\n장점:\n- 모든 문제를 해결했습니다.\n- 코드가 잘 동작합니다.\n\n개선점:\n- 메모이제이션 최적화가 일부 부족합니다.\n- 시간복잡도 분석이 더 상세했으면 좋겠습니다.\n- 변수명을 더 명확하게 작성하세요.\n\n수고하셨습니다!',
    status = 'GRADED',
    graded_at = NOW() - INTERVAL 3 DAY,
    graded_by = 20241001
WHERE assignment_id = (
    SELECT a.id FROM assignments a
    INNER JOIN posts p ON a.post_id = p.id
    WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
    LIMIT 1
) AND user_id = 20250101002;

-- 과제 관련 해시태그 추가
INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '자료구조 과제 1: 연결 리스트 구현',
    '단일 연결 리스트와 이중 연결 리스트를 구현하고 다음 기능을 작성하세요.\n\n1. 노드 삽입 (앞, 뒤, 중간)\n2. 노드 삭제\n3. 노드 검색\n4. 리스트 출력\n\n제출물: 소스코드(.java 또는 .cpp), 실행 결과 캡처',
    'ASSIGNMENT',
    false,
    45,
    3,
    false,
    NOW() - INTERVAL 10 DAY,
    NOW() - INTERVAL 10 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '자료구조 과제 2: 스택과 큐 응용',
    '스택과 큐를 이용하여 다음 문제를 해결하세요.\n\n1. 괄호 검사 프로그램 (스택)\n2. 은행 대기열 시뮬레이션 (큐)\n3. 회문 검사 프로그램 (스택+큐)\n\n제출물: 소스코드, 실행 결과, 보고서(PDF)',
    'ASSIGNMENT',
    false,
    38,
    2,
    false,
    NOW() - INTERVAL 9 DAY,
    NOW() - INTERVAL 9 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '자료구조 과제 3: 이진 탐색 트리 구현',
    '이진 탐색 트리(BST)를 구현하고 다음 기능을 작성하세요.\n\n1. 노드 삽입\n2. 노드 삭제\n3. 노드 검색\n4. 중위/전위/후위 순회\n5. 트리의 높이 계산\n\n제출물: 소스코드, 테스트 케이스, 실행 결과',
    'ASSIGNMENT',
    false,
    32,
    1,
    false,
    NOW() - INTERVAL 8 DAY,
    NOW() - INTERVAL 8 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '알고리즘 과제 1: 정렬 알고리즘 성능 비교',
    '다음 정렬 알고리즘을 구현하고 성능을 비교 분석하세요.\n\n1. 버블 정렬\n2. 선택 정렬\n3. 삽입 정렬\n4. 퀵 정렬\n5. 병합 정렬\n\n데이터 크기별(100, 1000, 10000) 실행 시간을 측정하고 그래프로 표현하세요.\n\n제출물: 소스코드, 성능 분석 보고서(그래프 포함)',
    'ASSIGNMENT',
    false,
    51,
    4,
    false,
    NOW() - INTERVAL 7 DAY,
    NOW() - INTERVAL 7 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO posts (category_id, author_id, title, content, post_type, is_anonymous, view_count, like_count, is_deleted, created_at, updated_at, created_by, updated_by)
SELECT 
    bc.id,
    20241001,
    '알고리즘 과제 2: 동적 계획법 문제 풀이',
    '동적 계획법을 이용하여 다음 문제를 해결하세요.\n\n1. 피보나치 수열 (Top-down, Bottom-up)\n2. 최장 공통 부분 수열(LCS)\n3. 0-1 배낭 문제\n4. 최단 경로 문제 (Floyd-Warshall)\n\n각 문제에 대한 시간복잡도 분석을 포함하세요.\n\n제출물: 소스코드, 시간복잡도 분석 보고서',
    'ASSIGNMENT',
    false,
    67,
    8,
    false,
    NOW() - INTERVAL 15 DAY,
    NOW() - INTERVAL 15 DAY,
    20241001,
    20241001
FROM board_categories bc
WHERE bc.board_type = 'ASSIGNMENT'
LIMIT 1;

-- 과제 테이블 데이터 (posts와 연결)










-- 과제 제출 더미 데이터
-- 과제 1: 3명 제출 (1명 채점 완료, 2명 미채점)



-- 첫 번째 제출 채점 (95점)
UPDATE assignment_submissions 
SET score = 95, 
    feedback = '전반적으로 잘 구현하셨습니다.\n\n장점:\n- 코드가 깔끔하고 가독성이 좋습니다.\n- 모든 기능이 정상 동작합니다.\n- 주석이 잘 작성되어 있습니다.\n\n개선점:\n- 예외 처리가 일부 누락되었습니다. (특히 빈 리스트 처리)\n- 시간복잡도 분석이 보고서에 포함되면 더 좋았을 것 같습니다.\n\n수고하셨습니다!',
    status = 'GRADED',
    graded_at = NOW() - INTERVAL 1 DAY,
    graded_by = 20241001
WHERE assignment_id = (
    SELECT a.id FROM assignments a
    INNER JOIN posts p ON a.post_id = p.id
    WHERE p.title = '자료구조 과제 1: 연결 리스트 구현' AND p.post_type = 'ASSIGNMENT'
    LIMIT 1
) AND user_id = 20250101001;

-- 과제 2: 2명 제출 (모두 미채점)


-- 과제 5 (마감 지난 과제): 2명 제출, 1명 지각 제출



-- 과제 5 제출 채점
UPDATE assignment_submissions 
SET score = 98, 
    feedback = '매우 우수한 과제입니다!\n\n장점:\n- Top-down과 Bottom-up 방식 모두 정확히 구현\n- 시간복잡도 분석이 정확하고 상세함\n- 코드가 효율적이고 최적화되어 있음\n- 테스트 케이스가 다양하고 충분함\n\n거의 완벽합니다. 계속 이런 자세로 학습하세요!',
    status = 'GRADED',
    graded_at = NOW() - INTERVAL 4 DAY,
    graded_by = 20241001
WHERE assignment_id = (
    SELECT a.id FROM assignments a
    INNER JOIN posts p ON a.post_id = p.id
    WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
    LIMIT 1
) AND user_id = 20250101001;

UPDATE assignment_submissions 
SET score = 85, 
    feedback = '전반적으로 양호합니다.\n\n장점:\n- 모든 문제를 해결했습니다.\n- 코드가 잘 동작합니다.\n\n개선점:\n- 메모이제이션 최적화가 일부 부족합니다.\n- 시간복잡도 분석이 더 상세했으면 좋겠습니다.\n- 변수명을 더 명확하게 작성하세요.\n\n수고하셨습니다!',
    status = 'GRADED',
    graded_at = NOW() - INTERVAL 3 DAY,
    graded_by = 20241001
WHERE assignment_id = (
    SELECT a.id FROM assignments a
    INNER JOIN posts p ON a.post_id = p.id
    WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
    LIMIT 1
) AND user_id = 20250101002;

-- 과제 관련 해시태그 추가
