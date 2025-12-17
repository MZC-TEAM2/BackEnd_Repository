-- =====================================================
-- V15: 테스트 사용자 데이터 생성
-- =====================================================

-- 교수 사용자

-- User 3: 강교수 (20250102001)
INSERT INTO users (id, email, password, created_at, updated_at) VALUES
(20250102001, 'professor3@university.ac.kr', '$2a$10$DUMMY_HASH_FOR_PASSWORD', NOW(), NOW());

INSERT INTO user_profiles (user_id, name, created_at) VALUES
(20250102001, '강교수', NOW());

INSERT INTO user_type_mappings (user_id, user_type_id, assigned_at) VALUES
(20250102001, 2, NOW());

-- 학생 사용자
-- Student 4: 장학생 (20250102002)
INSERT INTO users (id, email, password, created_at, updated_at) VALUES
(20250102002, 'student4@university.ac.kr', '$2a$10$DUMMY_HASH_FOR_PASSWORD', NOW(), NOW());

INSERT INTO user_profiles (user_id, name, created_at) VALUES
(20250102002, '장학생', NOW());

INSERT INTO user_type_mappings (user_id, user_type_id, assigned_at) VALUES
(20250102002, 1, NOW());

INSERT INTO students (student_id, admission_year, grade, created_at) VALUES
(20250102002, 2025, 1, NOW());
