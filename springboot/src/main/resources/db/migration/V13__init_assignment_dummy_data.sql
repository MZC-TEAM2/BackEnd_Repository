-- =====================================================
-- V13: 과제 게시판 더미 데이터 생성
-- =====================================================

-- 과제 게시글 더미 데이터 (5개)
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
INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, late_submission_allowed, late_penalty_percent, max_file_size_mb, allowed_file_types, instructions, created_by, created_at, updated_at)
SELECT 
    p.id,
    1,
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    100.00,
    'BOTH',
    1,
    5.00,
    10,
    'java,cpp,c,py,zip',
    '소스코드와 실행 결과를 모두 제출하세요.',
    20241001,
    NOW() - INTERVAL 10 DAY,
    NOW() - INTERVAL 10 DAY
FROM posts p
WHERE p.title = '자료구조 과제 1: 연결 리스트 구현' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, late_submission_allowed, late_penalty_percent, max_file_size_mb, allowed_file_types, instructions, created_by, created_at, updated_at)
SELECT 
    p.id,
    1,
    DATE_ADD(NOW(), INTERVAL 14 DAY),
    100.00,
    'BOTH',
    1,
    5.00,
    10,
    'java,cpp,c,py,pdf,zip',
    '소스코드와 보고서를 제출하세요.',
    20241001,
    NOW() - INTERVAL 9 DAY,
    NOW() - INTERVAL 9 DAY
FROM posts p
WHERE p.title = '자료구조 과제 2: 스택과 큐 응용' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, late_submission_allowed, late_penalty_percent, max_file_size_mb, allowed_file_types, instructions, created_by, created_at, updated_at)
SELECT 
    p.id,
    1,
    DATE_ADD(NOW(), INTERVAL 21 DAY),
    100.00,
    'BOTH',
    1,
    5.00,
    10,
    'java,cpp,c,py,zip',
    '소스코드와 테스트 케이스를 제출하세요.',
    20241001,
    NOW() - INTERVAL 8 DAY,
    NOW() - INTERVAL 8 DAY
FROM posts p
WHERE p.title = '자료구조 과제 3: 이진 탐색 트리 구현' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, late_submission_allowed, late_penalty_percent, max_file_size_mb, allowed_file_types, instructions, created_by, created_at, updated_at)
SELECT 
    p.id,
    2,
    DATE_ADD(NOW(), INTERVAL 10 DAY),
    100.00,
    'BOTH',
    1,
    5.00,
    20,
    'java,cpp,c,py,pdf,xlsx,zip',
    '소스코드와 성능 분석 보고서를 제출하세요.',
    20241001,
    NOW() - INTERVAL 7 DAY,
    NOW() - INTERVAL 7 DAY
FROM posts p
WHERE p.title = '알고리즘 과제 1: 정렬 알고리즘 성능 비교' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

INSERT INTO assignments (post_id, course_id, due_date, max_score, submission_method, late_submission_allowed, late_penalty_percent, max_file_size_mb, allowed_file_types, instructions, created_by, created_at, updated_at)
SELECT 
    p.id,
    2,
    DATE_ADD(NOW(), INTERVAL -3 DAY),
    100.00,
    'BOTH',
    0,
    NULL,
    10,
    'java,cpp,c,py,pdf,zip',
    '소스코드와 시간복잡도 분석 보고서를 제출하세요.',
    20241001,
    NOW() - INTERVAL 15 DAY,
    NOW() - INTERVAL 15 DAY
FROM posts p
WHERE p.title = '알고리즘 과제 2: 동적 계획법 문제 풀이' AND p.post_type = 'ASSIGNMENT'
LIMIT 1;

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
INSERT IGNORE INTO hashtags (name, display_name, color, tag_category, is_active, created_by, created_at) VALUES
('자료구조', '자료구조', '#1976d2', 'ASSIGNMENT', 1, 20241001, NOW()),
('알고리즘', '알고리즘', '#9c27b0', 'ASSIGNMENT', 1, 20241001, NOW()),
('코딩과제', '코딩과제', '#0288d1', 'ASSIGNMENT', 1, 20241001, NOW()),
('프로그래밍', '프로그래밍', '#f57c00', 'ASSIGNMENT', 1, 20241001, NOW()),
('동적계획법', '동적계획법', '#388e3c', 'ASSIGNMENT', 1, 20241001, NOW());
