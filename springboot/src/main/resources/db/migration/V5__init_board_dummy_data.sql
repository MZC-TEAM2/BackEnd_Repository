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

-- 공지사항 더미 데이터
-- 카테고리 ID 조회 (NOTICE)
SET @notice_category_id = (SELECT id FROM board_categories WHERE board_type = 'NOTICE' LIMIT 1);

-- 공지사항 게시글 20개
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

-- 댓글 더미 데이터 (첫 번째 공지사항에 대한 댓글)
SET @first_post_id = (SELECT id FROM posts WHERE title = '[중요] 2025학년도 1학기 수강신청 일정 안내' LIMIT 1);
SET @second_post_id = (SELECT id FROM posts WHERE title = '[공지] 2024학년도 2학기 기말고사 시험 일정' LIMIT 1);
SET @third_post_id = (SELECT id FROM posts WHERE title = '[중요] 학생 건강검진 실시 안내' LIMIT 1);

-- 첫 번째 게시글의 댓글 (5개)
INSERT INTO comments (post_id, author_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@first_post_id, 20241001, '수강신청 일정 공지 감사합니다. 꼭 확인하겠습니다!', 0, false, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '1차 수강신청에서 정정이 가능한가요?', 0, false, false, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '유의사항도 자세히 안내해주셔서 감사합니다.', 0, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '시간표 짜는 것부터 어려운데 잘 준비해야겠어요.', 0, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001),
(@first_post_id, 20241001, '2차 수강신청 일정도 놓치지 않도록 주의해야겠네요!', 0, false, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001);

-- 대댓글 추가 (첫 번째 게시글의 두 번째 댓글에 대한 답변)
SET @parent_comment_id = (SELECT id FROM comments WHERE content = '1차 수강신청에서 정정이 가능한가요?' LIMIT 1);
INSERT INTO comments (post_id, author_id, parent_comment_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by, is_deleted_by_admin) VALUES
(@first_post_id, 20241001, @parent_comment_id, '1차 수강신청 기간 내에는 자유롭게 정정 가능합니다.', 1, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001, false),
(@first_post_id, 20241001, @parent_comment_id, '다만 정정 마감 시간을 꼭 확인하시기 바랍니다.', 1, false, false, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 20241001, 20241001, false);

-- 두 번째 게시글의 댓글 (3개)
INSERT INTO comments (post_id, author_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@second_post_id, 20241001, '기말고사 일정 확인했습니다. 열심히 준비하겠습니다!', 0, false, false, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY, 20241001, 20241001),
(@second_post_id, 20241001, '성적 공개는 12월 27일이군요. 기다려집니다.', 0, false, false, NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY, 20241001, 20241001),
(@second_post_id, 20241001, '시험 기간 동안 도서관 운영시간도 연장되나요?', 0, false, false, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 20241001, 20241001);

-- 세 번째 게시글의 댓글 (4개)
INSERT INTO comments (post_id, author_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by) VALUES
(@third_post_id, 20241001, '건강검진 꼭 받아야 하나요?', 0, false, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@third_post_id, 20241001, '대상 학년이 아니라 안타깝네요. 건강 챙기세요!', 0, false, false, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 20241001, 20241001),
(@third_post_id, 20241001, '보건소 위치가 어디인가요?', 0, false, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001),
(@third_post_id, 20241001, '건강검진 결과는 언제 나오나요?', 0, false, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001);

-- 대댓글 추가 (세 번째 게시글의 첫 번째 댓글에 대한 답변)
SET @health_comment_id = (SELECT id FROM comments WHERE content = '건강검진 꼭 받아야 하나요?' LIMIT 1);
INSERT INTO comments (post_id, author_id, parent_comment_id, content, depth, is_anonymous, is_deleted, created_at, updated_at, created_by, updated_by, is_deleted_by_admin) VALUES
(@third_post_id, 20241001, @health_comment_id, '네, 필수로 받으셔야 합니다. 미이수 시 졸업에 영향을 줄 수 있습니다.', 1, false, false, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 20241001, 20241001, false);
