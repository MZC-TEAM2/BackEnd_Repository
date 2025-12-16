-- =====================================================
-- V12: 교수/과제/시험/퀴즈 게시판 더미데이터
-- V7 이후 실행 (PROFESSOR, ASSIGNMENT, EXAM, QUIZ 게시판 생성 이후)
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
