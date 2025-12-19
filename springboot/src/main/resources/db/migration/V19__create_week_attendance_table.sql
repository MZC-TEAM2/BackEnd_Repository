-- Week Attendance Table
-- 주차별 출석 관리 테이블
-- 학생이 해당 주차의 모든 VIDEO 콘텐츠를 완료하면 출석으로 인정

CREATE TABLE IF NOT EXISTS week_attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '출석 식별자',
    student_id BIGINT NOT NULL COMMENT '학생 ID',
    week_id BIGINT NOT NULL COMMENT '주차 ID',
    course_id BIGINT NOT NULL COMMENT '강의 ID',
    is_completed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '출석 완료 여부',
    completed_video_count INT NOT NULL DEFAULT 0 COMMENT '완료한 VIDEO 수',
    total_video_count INT NOT NULL COMMENT '전체 VIDEO 수 (완료 시점 잠금용)',
    first_accessed_at TIMESTAMP NULL COMMENT '최초 접근 일시',
    completed_at TIMESTAMP NULL COMMENT '출석 완료 일시',

    CONSTRAINT fk_week_attendance_student
        FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_week_attendance_week
        FOREIGN KEY (week_id) REFERENCES course_weeks(id) ON DELETE CASCADE,
    CONSTRAINT fk_week_attendance_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,

    CONSTRAINT uk_week_attendance_student_week UNIQUE (student_id, week_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for performance
CREATE INDEX idx_week_attendance_student ON week_attendance(student_id);
CREATE INDEX idx_week_attendance_course ON week_attendance(course_id);
CREATE INDEX idx_week_attendance_week ON week_attendance(week_id);
CREATE INDEX idx_week_attendance_completed ON week_attendance(is_completed);
