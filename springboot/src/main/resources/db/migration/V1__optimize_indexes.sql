-- 성능 최적화를 위한 필수 인덱스 생성

-- 1. students 테이블
CREATE INDEX IF NOT EXISTS idx_students_student_number ON students(student_number);
CREATE INDEX IF NOT EXISTS idx_students_user_id ON students(user_id);

-- 2. professors 테이블
CREATE INDEX IF NOT EXISTS idx_professors_professor_number ON professors(professor_number);
CREATE INDEX IF NOT EXISTS idx_professors_user_id ON professors(user_id);

-- 3. student_departments 테이블 (복합 인덱스)
CREATE INDEX IF NOT EXISTS idx_student_dept_active ON student_departments(student_id, is_active);
CREATE INDEX IF NOT EXISTS idx_student_dept_student ON student_departments(student_id);

-- 4. professor_departments 테이블 (복합 인덱스)
CREATE INDEX IF NOT EXISTS idx_prof_dept_active ON professor_departments(professor_id, is_active);
CREATE INDEX IF NOT EXISTS idx_prof_dept_professor ON professor_departments(professor_id);

-- 5. user_profiles 테이블 (이미 1:1이므로 user_id가 PK)
-- PK 인덱스는 자동 생성됨

-- 6. user_primary_contacts 테이블 (이미 1:1이므로 user_id가 PK)
-- PK 인덱스는 자동 생성됨
-- mobile_number 유니크 인덱스는 엔티티에서 정의됨

-- 7. user_profile_images 테이블 (이미 1:1이므로 user_id가 PK)
-- PK 인덱스는 자동 생성됨

-- 8. departments 테이블
CREATE INDEX IF NOT EXISTS idx_departments_college ON departments(college_id);

-- 통계 업데이트 (MySQL)
ANALYZE TABLE students;
ANALYZE TABLE professors;
ANALYZE TABLE student_departments;
ANALYZE TABLE professor_departments;
ANALYZE TABLE departments;