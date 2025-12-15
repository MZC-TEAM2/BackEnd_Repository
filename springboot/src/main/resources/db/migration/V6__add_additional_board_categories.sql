-- =====================================================
-- V6: 추가 게시판 카테고리 초기화
-- 역할별 제한 게시판 + 특수 목적 게시판 + 학습관리 게시판
-- =====================================================

-- 역할별 제한 게시판 (2개)
INSERT INTO board_categories (name, board_type, allow_comments, allow_attachments, allow_anonymous, display_order, is_active, is_deleted, created_at, updated_at) VALUES
('교수 게시판', 'PROFESSOR', true, true, false, 6, true, false, NOW(), NOW()),
('학생 게시판', 'STUDENT', true, true, true, 7, true, false, NOW(), NOW());

-- 특수 목적 게시판 (2개)
INSERT INTO board_categories (name, board_type, allow_comments, allow_attachments, allow_anonymous, display_order, is_active, is_deleted, created_at, updated_at) VALUES
('공모전 게시판', 'CONTEST', true, true, false, 8, true, false, NOW(), NOW()),
('취업 게시판', 'CAREER', true, true, true, 9, true, false, NOW(), NOW());

-- 학습관리 시스템 게시판 (4개)
INSERT INTO board_categories (name, board_type, allow_comments, allow_attachments, allow_anonymous, display_order, is_active, is_deleted, created_at, updated_at) VALUES
('과제 게시판', 'ASSIGNMENT', false, true, false, 10, true, false, NOW(), NOW()),
('시험 게시판', 'EXAM', false, true, false, 11, true, false, NOW(), NOW()),
('퀴즈 게시판', 'QUIZ', false, false, false, 12, true, false, NOW(), NOW()),
('스터디모집 게시판', 'STUDY_RECRUITMENT', true, false, false, 13, true, false, NOW(), NOW());

-- 각 게시판별 특징:
-- PROFESSOR: 교수만 접근, 댓글 허용, 첨부파일 허용, 익명 불가
-- STUDENT: 학생만 접근, 댓글 허용, 첨부파일 허용, 익명 가능
-- CONTEST: 공모전 정보, 댓글 허용, 첨부파일 허용, 익명 불가
-- CAREER: 취업 정보, 댓글 허용, 첨부파일 허용, 익명 가능 (후기 등)
-- ASSIGNMENT: 과제 관리, 댓글 불가 (제출용), 첨부파일 허용
-- EXAM: 시험 관리, 댓글 불가, 첨부파일 허용 (시험지 등)
-- QUIZ: 퀴즈 관리, 댓글 불가, 첨부파일 불가 (온라인 전용)
-- STUDY_RECRUITMENT: 스터디 모집, 댓글 허용 (문의용), 첨부파일 불가
