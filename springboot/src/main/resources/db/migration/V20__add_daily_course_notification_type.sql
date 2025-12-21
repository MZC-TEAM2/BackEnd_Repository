-- -----------------------------------------------------
-- V20: 오늘의 수업 알림 타입 추가
-- -----------------------------------------------------

INSERT INTO notification_types (type_code, type_name, category, default_message_template, is_active) VALUES
('DAILY_COURSE_REMINDER', '오늘의 수업 알림', '수업', '오늘 수강해야 할 수업이 있습니다.', true);
