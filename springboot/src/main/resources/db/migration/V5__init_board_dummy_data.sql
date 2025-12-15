-- 더미 관리자 사용자 생성 (게시글 작성자용)
INSERT INTO users (id, email, password, created_at) VALUES
(20241001, 'admin@lms.ac.kr', '$2a$10$dummyHashedPassword123456789012345678901234567890', NOW());

INSERT INTO user_type_mappings (user_id, user_type_id, assigned_at) VALUES
(20241001, 2, NOW());  -- 교수 타입

INSERT INTO user_profiles (user_id, name, created_at) VALUES
(20241001, '관리자', NOW());

-- 게시판 카테고리 초기화 데이터
INSERT INTO board_categories (name, board_type, allow_comments, allow_attachments, allow_anonymous, is_deleted, created_at, updated_at) VALUES
('공지사항', 'NOTICE', true, true, false, false, NOW(), NOW()),
('자유게시판', 'FREE', true, true, true, false, NOW(), NOW()),
('질문게시판', 'QUESTION', true, true, true, false, NOW(), NOW()),
('토론게시판', 'DISCUSSION', true, true, false, false, NOW(), NOW()),
('학과게시판', 'DEPARTMENT', true, true, false, false, NOW(), NOW());

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
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('대학생활', '대학생활', '#1976d2', 'FREE', 1, 20241001, NOW()),
('맛집추천', '맛집추천', '#d32f2f', 'FREE', 1, 20241001, NOW()),
('동아리', '동아리', '#0288d1', 'FREE', 1, 20241001, NOW()),
('취미', '취미', '#9c27b0', 'FREE', 1, 20241001, NOW()),
('일상', '일상', '#f57c00', 'FREE', 1, 20241001, NOW()),
('고민상담', '고민상담', '#388e3c', 'FREE', 1, 20241001, NOW());

-- 질문 게시판 해시태그
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('자료구조', '자료구조', '#1976d2', 'QUESTION', 1, 20241001, NOW()),
('알고리즘', '알고리즘', '#9c27b0', 'QUESTION', 1, 20241001, NOW()),
('데이터베이스', '데이터베이스', '#0288d1', 'QUESTION', 1, 20241001, NOW()),
('웹개발', '웹개발', '#388e3c', 'QUESTION', 1, 20241001, NOW()),
('운영체제', '운영체제', '#f57c00', 'QUESTION', 1, 20241001, NOW()),
('네트워크', '네트워크', '#d32f2f', 'QUESTION', 1, 20241001, NOW()),
('ai', 'AI', '#1976d2', 'QUESTION', 1, 20241001, NOW());

-- 토론 게시판 해시태그
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('ai윤리토론', 'AI윤리토론', '#1976d2', 'DISCUSSION', 1, 20241001, NOW()),
('기후변화대응', '기후변화대응', '#388e3c', 'DISCUSSION', 1, 20241001, NOW()),
('교육개혁', '교육개혁', '#f57c00', 'DISCUSSION', 1, 20241001, NOW()),
('메타버스미래', '메타버스미래', '#0288d1', 'DISCUSSION', 1, 20241001, NOW()),
('청년정책', '청년정책', '#9c27b0', 'DISCUSSION', 1, 20241001, NOW()),
('esg경영', 'ESG경영', '#388e3c', 'DISCUSSION', 1, 20241001, NOW()),
('디지털전환', '디지털전환', '#1976d2', 'DISCUSSION', 1, 20241001, NOW()),
('사회적가치', '사회적가치', '#d32f2f', 'DISCUSSION', 1, 20241001, NOW());

-- 학생 게시판 해시태그
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('학업고민', '학업고민', '#1976d2', 'STUDENT', 1, 20241001, NOW()),
('진로상담', '진로상담', '#9c27b0', 'STUDENT', 1, 20241001, NOW()),
('대외활동', '대외활동', '#0288d1', 'STUDENT', 1, 20241001, NOW()),
('학생회', '학생회', '#f57c00', 'STUDENT', 1, 20241001, NOW()),
('동아리모집', '동아리모집', '#388e3c', 'STUDENT', 1, 20241001, NOW());

-- 공모전 게시판 해시태그
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('it/소프트웨어', 'IT/소프트웨어', '#1976d2', 'CONTEST', 1, 20241001, NOW()),
('디자인', '디자인', '#9c27b0', 'CONTEST', 1, 20241001, NOW()),
('마케팅', '마케팅', '#0288d1', 'CONTEST', 1, 20241001, NOW()),
('아이디어', '아이디어', '#f57c00', 'CONTEST', 1, 20241001, NOW()),
('창업', '창업', '#d32f2f', 'CONTEST', 1, 20241001, NOW()),
('사회혁신', '사회혁신', '#388e3c', 'CONTEST', 1, 20241001, NOW());

-- 취업정보 게시판 해시태그
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('채용공고', '채용공고', '#1976d2', 'CAREER', 1, 20241001, NOW()),
('면접후기', '면접후기', '#9c27b0', 'CAREER', 1, 20241001, NOW()),
('인턴', '인턴', '#0288d1', 'CAREER', 1, 20241001, NOW()),
('자소서첨삭', '자소서첨삭', '#f57c00', 'CAREER', 1, 20241001, NOW()),
('포트폴리오', '포트폴리오', '#388e3c', 'CAREER', 1, 20241001, NOW()),
('이력서', '이력서', '#d32f2f', 'CAREER', 1, 20241001, NOW());

-- 스터디모집 게시판 해시태그
INSERT INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('코딩테스트', '코딩테스트', '#1976d2', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('자격증', '자격증', '#9c27b0', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('프로젝트', '프로젝트', '#0288d1', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('토익토스', '토익토스', '#f57c00', 'STUDY_RECRUITMENT', 1, 20241001, NOW()),
('전공공부', '전공공부', '#388e3c', 'STUDY_RECRUITMENT', 1, 20241001, NOW());

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
