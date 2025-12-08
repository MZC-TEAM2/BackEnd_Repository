-- 학번/교번을 PK로 변경하는 마이그레이션

-- 1. 학번 시퀀스 관리 테이블 생성
CREATE TABLE IF NOT EXISTS student_number_sequences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year INT NOT NULL,
    college_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    last_sequence INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    UNIQUE KEY unique_year_college_dept (year, college_id, department_id),
    INDEX idx_student_seq_year_college_dept (year, college_id, department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 기존 테이블 백업 (데이터가 있을 경우를 대비)
CREATE TABLE IF NOT EXISTS students_backup AS SELECT * FROM students;
CREATE TABLE IF NOT EXISTS professors_backup AS SELECT * FROM professors;
CREATE TABLE IF NOT EXISTS student_departments_backup AS SELECT * FROM student_departments;
CREATE TABLE IF NOT EXISTS professor_departments_backup AS SELECT * FROM professor_departments;

-- 3. 외래키 제약 조건 임시 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 4. student_departments 테이블 수정
ALTER TABLE student_departments
    DROP FOREIGN KEY IF EXISTS student_departments_ibfk_1;
ALTER TABLE student_departments
    ADD COLUMN student_id_new VARCHAR(20) AFTER id;

-- 5. professor_departments 테이블 수정
ALTER TABLE professor_departments
    DROP FOREIGN KEY IF EXISTS professor_departments_ibfk_1;
ALTER TABLE professor_departments
    ADD COLUMN professor_id_new VARCHAR(20) AFTER id;

-- 6. students 테이블 재생성
DROP TABLE IF EXISTS students;
CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    admission_year INT NOT NULL,
    grade INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_students_user_id (user_id),
    INDEX idx_students_admission_year (admission_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. professors 테이블 재생성
DROP TABLE IF EXISTS professors;
CREATE TABLE professors (
    professor_id VARCHAR(20) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    appointment_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_professors_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. student_departments의 외래키 재설정
ALTER TABLE student_departments
    ADD CONSTRAINT fk_student_dept_student
    FOREIGN KEY (student_id_new) REFERENCES students(student_id) ON DELETE CASCADE;

-- 9. professor_departments의 외래키 재설정
ALTER TABLE professor_departments
    ADD CONSTRAINT fk_professor_dept_professor
    FOREIGN KEY (professor_id_new) REFERENCES professors(professor_id) ON DELETE CASCADE;

-- 10. 기존 컬럼 제거 및 컬럼명 변경
ALTER TABLE student_departments
    DROP COLUMN student_id,
    CHANGE COLUMN student_id_new student_id VARCHAR(20) NOT NULL;

ALTER TABLE professor_departments
    DROP COLUMN professor_id,
    CHANGE COLUMN professor_id_new professor_id VARCHAR(20) NOT NULL;

-- 11. 인덱스 추가
ALTER TABLE student_departments
    ADD INDEX idx_student_dept_active (student_id, is_primary);

ALTER TABLE professor_departments
    ADD INDEX idx_prof_dept_active (professor_id, is_primary);

-- 12. 외래키 제약 조건 재활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 13. 샘플 데이터용 시퀀스 초기화 (필요 시)
INSERT INTO student_number_sequences (year, college_id, department_id, last_sequence) VALUES
    (2024, 1, 1, 0),
    (2024, 1, 2, 0),
    (2024, 2, 3, 0),
    (2024, 2, 4, 0),
    (2024, 3, 5, 0),
    (2024, 3, 6, 0);