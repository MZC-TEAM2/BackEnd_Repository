-- =====================================================
-- LMS 스키마 생성 스크립트
-- V0__create_schema.sql
-- =====================================================

-- -----------------------------------------------------
-- 1. 기본 코드 테이블
-- -----------------------------------------------------

-- 사용자 유형 테이블
CREATE TABLE IF NOT EXISTS user_types (
    id INT NOT NULL AUTO_INCREMENT,
    type_code VARCHAR(20) NOT NULL,
    type_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_types_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 상태 유형 테이블
CREATE TABLE IF NOT EXISTS user_status_types (
    id INT NOT NULL AUTO_INCREMENT,
    status_code VARCHAR(20) NOT NULL,
    status_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_status_types_code (status_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 알림 타입 테이블
CREATE TABLE IF NOT EXISTS notification_types (
    id INT NOT NULL AUTO_INCREMENT,
    type_code VARCHAR(50) NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    default_message_template TEXT NOT NULL,
    is_active BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_types_code (type_code),
    KEY idx_notification_types_type_code (type_code),
    KEY idx_notification_types_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 2. 조직 테이블 (단과대학, 학과)
-- -----------------------------------------------------

-- 단과대학 테이블
CREATE TABLE IF NOT EXISTS colleges (
    id BIGINT NOT NULL AUTO_INCREMENT,
    college_code VARCHAR(20) NOT NULL,
    college_number_code VARCHAR(2) NOT NULL,
    college_name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_colleges_code (college_code),
    UNIQUE KEY uk_colleges_number_code (college_number_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 학과 테이블
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    college_id BIGINT NOT NULL,
    department_code VARCHAR(20) NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_departments_code (department_code),
    KEY idx_departments_department_code (department_code),
    KEY idx_departments_college_id (college_id),
    CONSTRAINT fk_departments_college FOREIGN KEY (college_id) REFERENCES colleges (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 3. 사용자 관련 테이블
-- -----------------------------------------------------

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    deleted_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_email (email),
    KEY idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자-유형 매핑 테이블
CREATE TABLE IF NOT EXISTS user_type_mappings (
    user_id BIGINT NOT NULL,
    user_type_id INT NOT NULL,
    assigned_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (user_id),
    KEY idx_user_type_mappings_user_type_id (user_type_id),
    CONSTRAINT fk_user_type_mappings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_type_mappings_type FOREIGN KEY (user_type_id) REFERENCES user_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 프로필 테이블
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 연락처 테이블
CREATE TABLE IF NOT EXISTS user_primary_contacts (
    user_id BIGINT NOT NULL,
    mobile_number VARCHAR(50) DEFAULT NULL,
    mobile_verified BIT(1) DEFAULT NULL,
    home_number VARCHAR(50) DEFAULT NULL,
    office_number VARCHAR(50) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_primary_contact_mobile (mobile_number),
    KEY idx_primary_contact_mobile (mobile_number),
    KEY idx_primary_contact_verified (mobile_verified),
    CONSTRAINT fk_primary_contacts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 프로필 이미지 테이블
CREATE TABLE IF NOT EXISTS user_profile_images (
    user_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (user_id),
    KEY idx_profile_image_created (created_at),
    CONSTRAINT fk_profile_images_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 상태 이력 테이블
CREATE TABLE IF NOT EXISTS user_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status_id INT NOT NULL,
    changed_by BIGINT DEFAULT NULL,
    changed_at DATETIME(6) DEFAULT NULL,
    reason TEXT,
    PRIMARY KEY (id),
    KEY idx_user_status_history_user_id (user_id),
    KEY idx_user_status_history_status_id (status_id),
    KEY idx_user_status_history_changed_at (changed_at),
    CONSTRAINT fk_status_history_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_status_history_status FOREIGN KEY (status_id) REFERENCES user_status_types (id),
    CONSTRAINT fk_status_history_changed_by FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 4. 학생 관련 테이블
-- -----------------------------------------------------

-- 학생 테이블
CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT NOT NULL,
    admission_year INT NOT NULL,
    grade INT NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (student_id),
    KEY idx_students_student_id (student_id),
    KEY idx_students_admission_year (admission_year),
    CONSTRAINT fk_students_user FOREIGN KEY (student_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 학생-학과 매핑 테이블
CREATE TABLE IF NOT EXISTS student_departments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    is_primary BIT(1) DEFAULT NULL,
    enrolled_date DATE NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_department (student_id, department_id),
    UNIQUE KEY uk_student_primary (student_id),
    KEY idx_student_departments_student_id (student_id),
    KEY idx_student_departments_department_id (department_id),
    CONSTRAINT fk_student_dept_student FOREIGN KEY (student_id) REFERENCES students (student_id),
    CONSTRAINT fk_student_dept_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 학번 시퀀스 테이블
CREATE TABLE IF NOT EXISTS student_number_sequences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    year INT NOT NULL,
    college_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    last_sequence INT NOT NULL,
    version BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_seq (year, college_id, department_id),
    KEY idx_student_seq_year_college_dept (year, college_id, department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 5. 교수 관련 테이블
-- -----------------------------------------------------

-- 교수 테이블
CREATE TABLE IF NOT EXISTS professors (
    professor_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (professor_id),
    KEY idx_professors_professor_id (professor_id),
    CONSTRAINT fk_professors_user FOREIGN KEY (professor_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 교수-학과 매핑 테이블
CREATE TABLE IF NOT EXISTS professor_departments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    professor_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    is_primary BIT(1) DEFAULT NULL,
    start_date DATE NOT NULL,
    end_date DATE DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_professor_department (professor_id, department_id, start_date),
    UNIQUE KEY uk_professor_primary (professor_id),
    KEY idx_professor_departments_professor_id (professor_id),
    KEY idx_professor_departments_department_id (department_id),
    CONSTRAINT fk_professor_dept_professor FOREIGN KEY (professor_id) REFERENCES professors (professor_id),
    CONSTRAINT fk_professor_dept_department FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 6. 인증 관련 테이블
-- -----------------------------------------------------

-- 이메일 인증 테이블
CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    verification_code VARCHAR(5) NOT NULL,
    is_verified BIT(1) DEFAULT NULL,
    verification_attempts INT DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    verified_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_email_verifications_email (email),
    KEY idx_email_verifications_code (verification_code),
    KEY idx_email_verifications_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 리프레시 토큰 테이블
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_info VARCHAR(255) DEFAULT NULL,
    ip_address VARCHAR(45) DEFAULT NULL,
    is_revoked BIT(1) DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token (token),
    KEY idx_refresh_tokens_token (token),
    KEY idx_refresh_tokens_user_id (user_id),
    KEY idx_refresh_tokens_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 7. 알림 관련 테이블
-- -----------------------------------------------------

-- 알림 테이블
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    sender_id BIGINT DEFAULT NULL,
    type_id INT NOT NULL,
    title VARCHAR(200) DEFAULT NULL,
    message TEXT NOT NULL,
    is_read BIT(1) NOT NULL DEFAULT b'0',
    read_at DATETIME(6) DEFAULT NULL,
    course_id BIGINT DEFAULT NULL,
    related_entity_type VARCHAR(50) DEFAULT NULL,
    related_entity_id BIGINT DEFAULT NULL,
    action_url VARCHAR(500) DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_recipient_unread (recipient_id, is_read, created_at),
    KEY idx_recipient_created (recipient_id, created_at),
    KEY idx_course_created (course_id, created_at),
    KEY idx_notifications_type_id (type_id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT fk_notifications_type FOREIGN KEY (type_id) REFERENCES notification_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 알림 설정 테이블
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type_id INT NOT NULL,
    is_enabled BIT(1) NOT NULL DEFAULT b'1',
    email_enabled BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY idx_user_type_pref (user_id, type_id),
    KEY idx_notification_preferences_user_id (user_id),
    CONSTRAINT fk_notification_pref_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_notification_pref_type FOREIGN KEY (type_id) REFERENCES notification_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 알림 배치 테이블
CREATE TABLE IF NOT EXISTS notification_batches (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sender_id BIGINT DEFAULT NULL,
    type_id INT NOT NULL,
    title VARCHAR(200) DEFAULT NULL,
    message TEXT NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    total_recipients INT NOT NULL DEFAULT 0,
    course_id BIGINT DEFAULT NULL,
    error_message VARCHAR(500) DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    processed_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_notification_batches_course_id (course_id),
    KEY idx_notification_batches_status (status),
    KEY idx_notification_batches_created_at (created_at),
    CONSTRAINT fk_notification_batch_sender FOREIGN KEY (sender_id) REFERENCES users (id),
    CONSTRAINT fk_notification_batch_type FOREIGN KEY (type_id) REFERENCES notification_types (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;