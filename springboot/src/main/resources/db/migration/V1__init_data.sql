-- =====================================================
-- LMS 초기 데이터 스크립트
-- V1__init_data.sql
-- =====================================================

-- -----------------------------------------------------
-- 1. 사용자 유형 (user_types)
-- -----------------------------------------------------
INSERT INTO user_types (id, type_code, type_name) VALUES
(1, 'STUDENT', '학생'),
(2, 'PROFESSOR', '교수');

-- -----------------------------------------------------
-- 2. 사용자 상태 유형 (user_status_types)
-- -----------------------------------------------------
INSERT INTO user_status_types (id, status_code, status_name) VALUES
(1, 'ACTIVE', '활성'),
(2, 'INACTIVE', '비활성'),
(3, 'SUSPENDED', '정지');

-- -----------------------------------------------------
-- 3. 단과대학 (colleges)
-- -----------------------------------------------------
INSERT INTO colleges (id, college_code, college_number_code, college_name) VALUES
(1, 'ENG', '01', '공과대학'),
(2, 'BUS', '02', '경영대학'),
(3, 'HUM', '03', '인문대학'),
(4, 'NAT', '04', '자연과학대학'),
(5, 'SOC', '05', '사회과학대학'),
(6, 'ART', '06', '예술대학');

-- -----------------------------------------------------
-- 4. 학과 (departments)
-- -----------------------------------------------------
-- 공과대학 (college_id = 1)
INSERT INTO departments (id, college_id, department_code, department_name) VALUES
(1, 1, 'CS', '컴퓨터공학과'),
(2, 1, 'EE', '전자공학과'),
(3, 1, 'ME', '기계공학과'),
(4, 1, 'CE', '화학공학과'),
(5, 1, 'CV', '건설환경공학과');

-- 경영대학 (college_id = 2)
INSERT INTO departments (id, college_id, department_code, department_name) VALUES
(6, 2, 'BA', '경영학과'),
(7, 2, 'ACC', '회계학과'),
(8, 2, 'FIN', '금융학과'),
(9, 2, 'MKT', '마케팅학과');

-- 인문대학 (college_id = 3)
INSERT INTO departments (id, college_id, department_code, department_name) VALUES
(10, 3, 'KOR', '국어국문학과'),
(11, 3, 'ENG', '영어영문학과'),
(12, 3, 'HIS', '사학과'),
(13, 3, 'PHI', '철학과');

-- 자연과학대학 (college_id = 4)
INSERT INTO departments (id, college_id, department_code, department_name) VALUES
(14, 4, 'MATH', '수학과'),
(15, 4, 'PHY', '물리학과'),
(16, 4, 'CHEM', '화학과'),
(17, 4, 'BIO', '생명과학과');

-- 사회과학대학 (college_id = 5)
INSERT INTO departments (id, college_id, department_code, department_name) VALUES
(18, 5, 'PSY', '심리학과'),
(19, 5, 'SOC', '사회학과'),
(20, 5, 'POL', '정치외교학과'),
(21, 5, 'ECO', '경제학과');

-- 예술대학 (college_id = 6)
INSERT INTO departments (id, college_id, department_code, department_name) VALUES
(22, 6, 'MUS', '음악학과'),
(23, 6, 'ART', '미술학과'),
(24, 6, 'DES', '디자인학과');

-- -----------------------------------------------------
-- 5. 알림 타입 (notification_types)
-- -----------------------------------------------------
INSERT INTO notification_types (id, type_code, type_name, category, default_message_template, is_active) VALUES
-- 시스템 알림
(1, 'SYSTEM_NOTICE', '시스템 공지', 'SYSTEM', '시스템 공지사항이 있습니다: {message}', true),
(2, 'SYSTEM_MAINTENANCE', '시스템 점검', 'SYSTEM', '시스템 점검 안내: {message}', true),

-- 계정 관련 알림
(3, 'ACCOUNT_CREATED', '계정 생성', 'ACCOUNT', '계정이 성공적으로 생성되었습니다.', true),
(4, 'PASSWORD_CHANGED', '비밀번호 변경', 'ACCOUNT', '비밀번호가 성공적으로 변경되었습니다.', true),
(5, 'PROFILE_UPDATED', '프로필 수정', 'ACCOUNT', '프로필 정보가 업데이트되었습니다.', true),

-- 학사 관련 알림
(6, 'COURSE_ENROLLED', '수강 신청', 'ACADEMIC', '{courseName} 과목이 수강 신청되었습니다.', true),
(7, 'COURSE_DROPPED', '수강 취소', 'ACADEMIC', '{courseName} 과목이 수강 취소되었습니다.', true),
(8, 'GRADE_POSTED', '성적 등록', 'ACADEMIC', '{courseName} 과목의 성적이 등록되었습니다.', true),
(9, 'ASSIGNMENT_DUE', '과제 마감', 'ACADEMIC', '{assignmentName} 과제 마감일이 다가옵니다.', true),
(10, 'ATTENDANCE_WARNING', '출석 경고', 'ACADEMIC', '{courseName} 과목의 출석률이 낮습니다.', true);