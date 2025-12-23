-- V22__seed_30_dummy_students_course_52.sql
-- 더미 학생 30명 생성 + course_id=52 수강 등록
-- 로그인: username=학번(숫자), password=password123

-- 먼저 course_id=52 생성 (subject_id=52 마이크로서비스, professor_id=20250101002)
INSERT INTO `courses` (`id`, `subject_id`, `academic_term_id`, `section_number`, `current_students`, `professor_id`, `max_students`, `created_at`, `description`) VALUES
(52, 52, 1, '01', 0, 20250101002, 60, NOW(), '마이크로서비스 아키텍처 강의');

INSERT INTO `users` (`id`, `email`, `password`, `created_at`, `updated_at`, `deleted_at`) VALUES
(20250102100, 'c2nTlYsK/IzDAZLBk11JH+jC+atAJ0iLLRtZnLAhuYE=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102101, 'EIQtDvppu+D4Qw/teL4F0FNATrpnZ304LSLAqTlt5Tw=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102102, 'RqvC9+UgaU3fYCyjh1mUDz2iGfdV5IvGYlrHSFYzzoU=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102103, 'FQkrkZBFzEOyIdkVYmd6P52CILL6KeFCHaBAg371C/U=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102104, 'R1/NmnjINiiqBjS71wThsr4sdPYARcGhBcBMV64LlYg=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102105, 'HwZ47GYOnbZeMnMbMLtGl4o7yVWsgrnl80avr+C4SV8=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102106, '08BemDIhzrFEaS5HMIoQdRr8Uqo9b/4bo/yhApmimBQ=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102107, 'CN6ACS/tQLQexQ7/YmQJ3BKPbEO2P3fAtUXn4BKkh/c=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102108, 'Ga2tuGPhHXsR0jX/mZ0oEqActlxnBywYQ5inB2zFyTo=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102109, 'ihWwdyaHYFUG8aP732agDKsgUsunaDm2y9RdNCQnpyY=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102110, 'Biei2ZgCYfU1ssKVn8m5YzCxtfD+fHljQiglom40U80=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102111, 'o0lD6vlOGWL/n5Mu40MrhNCdgWzSlh0yYr2iDxamsZY=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102112, 'yfiHZN9CPNLByLt6sIzz9ZF6YpbSeHRNjz2QnmzxsPE=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102113, 'lrhEbx6HFh7157YcPM98VK1qJ4B9ZzriVnNIvMcKjes=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102114, '4IuH1D3Ous7GdarW+ViraereGI5LonUre6wy031pSHQ=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102115, 'yw89FEpVOz3jwbXccTsEJ9VeLNG/PHyYT7VPwe3vHdY=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102116, 'OI65EEtUg/8QR3nRhHsHa0a6rbczkxfUiw7ysggt5MU=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102117, '3SZdqNCrwSdtsm0HtbQ/6IlhrSFKPYuKiBg6jGPaaq8=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102118, 'u4g/H97axczcd8hgN4VMJj33Qt2+rsLKycwRu9O8XI4=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102119, '6tdcWPkzThyWvwKUxauSNhJf6qVWLYknLLiFcVzZGhs=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102120, 'xAOpCtY4vF+YXbsMLiwFAktdms1eAtNG4Df+S3R8jeU=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102121, 'YZErwG5iIuYSOBKOtGyfQxfJzV3+7DaWbXk4yXOeP9s=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102122, '8ImgBims48KpALjuByo6HlNqaVH5fMS4XGaJ2Z/dR1Y=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102123, 'h5Dr/cBwbIoYl4P+FRVtydBQPgQVlKbkkKHX7Z9VzUQ=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102124, 'cURM6DcibYr8HFlAOVauCb6LvV1YFUBXGXZgqtTQt1M=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102125, 'tFFsrA6G4MUI9xnKXIpeKHK67o0pjejOFJqCm6GaLSk=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102126, 'm0jKjXaPHvdCGewKRkz4l/kzKphy3pVI/NPKC2NOyaU=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102127, '9/WrQR3V2qHoPvwtK1cPo6iTgbDuvNDVM7DHWXsO0Wc=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102128, '0yLM+G4pGXkkmTm+MM2ZL662hJZ1AJgDpJRiIZKUxtQ=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL),
(20250102129, 'gGHshnbSt1k0SDZaZ3sAuFZArPAbHgmY4HRFSSq3VIM=', '$2a$12$ZcgzTwf5HH/w3/qxh/y1pe9bzo1x6tr8YlZGxXENnZ14Pl2BtI34m', '2025-12-22 10:00:00.000000', NULL, NULL);

INSERT INTO `user_profiles` (`user_id`, `name`, `created_at`) VALUES
(20250102100, '김민준', '2025-12-22 10:00:00.000000'),
(20250102101, '이서연', '2025-12-22 10:00:00.000000'),
(20250102102, '박지훈', '2025-12-22 10:00:00.000000'),
(20250102103, '최유진', '2025-12-22 10:00:00.000000'),
(20250102104, '정지우', '2025-12-22 10:00:00.000000'),
(20250102105, '강하준', '2025-12-22 10:00:00.000000'),
(20250102106, '조수빈', '2025-12-22 10:00:00.000000'),
(20250102107, '윤도현', '2025-12-22 10:00:00.000000'),
(20250102108, '임채원', '2025-12-22 10:00:00.000000'),
(20250102109, '한지민', '2025-12-22 10:00:00.000000'),
(20250102110, '오세훈', '2025-12-22 10:00:00.000000'),
(20250102111, '신예린', '2025-12-22 10:00:00.000000'),
(20250102112, '홍서준', '2025-12-22 10:00:00.000000'),
(20250102113, '유나연', '2025-12-22 10:00:00.000000'),
(20250102114, '서강준', '2025-12-22 10:00:00.000000'),
(20250102115, '김하늘', '2025-12-22 10:00:00.000000'),
(20250102116, '이도윤', '2025-12-22 10:00:00.000000'),
(20250102117, '박서아', '2025-12-22 10:00:00.000000'),
(20250102118, '최현우', '2025-12-22 10:00:00.000000'),
(20250102119, '정수아', '2025-12-22 10:00:00.000000'),
(20250102120, '강민재', '2025-12-22 10:00:00.000000'),
(20250102121, '조아린', '2025-12-22 10:00:00.000000'),
(20250102122, '윤지호', '2025-12-22 10:00:00.000000'),
(20250102123, '임서현', '2025-12-22 10:00:00.000000'),
(20250102124, '한예준', '2025-12-22 10:00:00.000000'),
(20250102125, '오지민', '2025-12-22 10:00:00.000000'),
(20250102126, '신도윤', '2025-12-22 10:00:00.000000'),
(20250102127, '홍수아', '2025-12-22 10:00:00.000000'),
(20250102128, '유현준', '2025-12-22 10:00:00.000000'),
(20250102129, '서지우', '2025-12-22 10:00:00.000000');

INSERT INTO `students` (`student_id`, `admission_year`, `grade`, `created_at`) VALUES
(20250102100, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102101, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102102, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102103, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102104, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102105, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102106, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102107, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102108, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102109, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102110, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102111, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102112, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102113, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102114, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102115, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102116, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102117, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102118, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102119, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102120, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102121, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102122, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102123, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102124, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102125, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102126, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102127, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102128, 2025, 1, '2025-12-22 10:00:00.000000'),
(20250102129, 2025, 1, '2025-12-22 10:00:00.000000');

INSERT INTO `enrollments` (`course_id`, `student_id`, `enrolled_at`) VALUES
(52, 20250102100, '2025-12-22 10:00:00.000000'),
(52, 20250102101, '2025-12-22 10:00:00.000000'),
(52, 20250102102, '2025-12-22 10:00:00.000000'),
(52, 20250102103, '2025-12-22 10:00:00.000000'),
(52, 20250102104, '2025-12-22 10:00:00.000000'),
(52, 20250102105, '2025-12-22 10:00:00.000000'),
(52, 20250102106, '2025-12-22 10:00:00.000000'),
(52, 20250102107, '2025-12-22 10:00:00.000000'),
(52, 20250102108, '2025-12-22 10:00:00.000000'),
(52, 20250102109, '2025-12-22 10:00:00.000000'),
(52, 20250102110, '2025-12-22 10:00:00.000000'),
(52, 20250102111, '2025-12-22 10:00:00.000000'),
(52, 20250102112, '2025-12-22 10:00:00.000000'),
(52, 20250102113, '2025-12-22 10:00:00.000000'),
(52, 20250102114, '2025-12-22 10:00:00.000000'),
(52, 20250102115, '2025-12-22 10:00:00.000000'),
(52, 20250102116, '2025-12-22 10:00:00.000000'),
(52, 20250102117, '2025-12-22 10:00:00.000000'),
(52, 20250102118, '2025-12-22 10:00:00.000000'),
(52, 20250102119, '2025-12-22 10:00:00.000000'),
(52, 20250102120, '2025-12-22 10:00:00.000000'),
(52, 20250102121, '2025-12-22 10:00:00.000000'),
(52, 20250102122, '2025-12-22 10:00:00.000000'),
(52, 20250102123, '2025-12-22 10:00:00.000000'),
(52, 20250102124, '2025-12-22 10:00:00.000000'),
(52, 20250102125, '2025-12-22 10:00:00.000000'),
(52, 20250102126, '2025-12-22 10:00:00.000000'),
(52, 20250102127, '2025-12-22 10:00:00.000000'),
(52, 20250102128, '2025-12-22 10:00:00.000000'),
(52, 20250102129, '2025-12-22 10:00:00.000000');

-- current_students 보정(선택)
UPDATE `courses` SET `current_students` = `current_students` + 30 WHERE `id` = 52;

-- =====================================================
-- course_id=52 더미 응시 데이터 (exams.id = 17,18,19)
-- - 17: 퀴즈 (객관식 3문항, 4지선다)
-- - 18: 중간고사 (객관식 1 + 주관식 1)
-- - 19: 기말고사 (객관식 1 + 주관식 1)
-- =====================================================

SET @course52_professor_id := (SELECT professor_id FROM courses WHERE id = 52);
SET @quiz_total := (SELECT total_score FROM exams WHERE id = 17);
SET @mid_total := (SELECT total_score FROM exams WHERE id = 18);
SET @final_total := (SELECT total_score FROM exams WHERE id = 19);

DROP TEMPORARY TABLE IF EXISTS tmp_course52_students;
CREATE TEMPORARY TABLE tmp_course52_students (
  user_id BIGINT NOT NULL PRIMARY KEY
);

INSERT INTO tmp_course52_students (user_id) VALUES
(20250102100),(20250102101),(20250102102),(20250102103),(20250102104),
(20250102105),(20250102106),(20250102107),(20250102108),(20250102109),
(20250102110),(20250102111),(20250102112),(20250102113),(20250102114),
(20250102115),(20250102116),(20250102117),(20250102118),(20250102119),
(20250102120),(20250102121),(20250102122),(20250102123),(20250102124),
(20250102125),(20250102126),(20250102127),(20250102128),(20250102129);

-- 17: 퀴즈 제출 (자동채점된 점수 저장)
INSERT INTO exam_results (
  exam_id, user_id,
  started_at, submitted_at,
  is_late, late_penalty_points, late_penalty_rate,
  score, answer_data,
  graded_at, graded_by,
  created_at
)
SELECT
  17 AS exam_id,
  s.user_id,
  '2025-12-22 10:10:00.000000' AS started_at,
  (CASE WHEN MOD(s.user_id, 10) = 0 THEN DATE_ADD('2025-12-22 10:22:00.000000', INTERVAL 6 MINUTE)
        ELSE '2025-12-22 10:22:00.000000' END) AS submitted_at,
  (CASE WHEN MOD(s.user_id, 10) = 0 THEN b'1' ELSE b'0' END) AS is_late,
  0 AS late_penalty_points,
  (CASE WHEN MOD(s.user_id, 10) = 0 THEN 0.10 ELSE 0 END) AS late_penalty_rate,
  ROUND(
    @quiz_total * (0.60 + (MOD(s.user_id, 5) * 0.08)) *
    (CASE WHEN MOD(s.user_id, 10) = 0 THEN 0.90 ELSE 1 END)
  , 2) AS score,
  CAST(JSON_OBJECT(
    'q1', MOD(s.user_id, 4),
    'q2', MOD(s.user_id + 1, 4),
    'q3', MOD(s.user_id + 2, 4)
  ) AS CHAR) AS answer_data,
  '2025-12-22 10:22:00.000000' AS graded_at,
  NULL AS graded_by,
  '2025-12-22 10:22:00.000000' AS created_at
FROM tmp_course52_students s;

-- 18: 중간고사 제출 + 교수 채점 완료(점수 입력됨)
INSERT INTO exam_results (
  exam_id, user_id,
  started_at, submitted_at,
  is_late, late_penalty_points, late_penalty_rate,
  score, answer_data,
  graded_at, graded_by,
  created_at
)
SELECT
  18 AS exam_id,
  s.user_id,
  '2025-12-22 11:00:00.000000' AS started_at,
  (CASE WHEN MOD(s.user_id, 10) = 1 THEN DATE_ADD('2025-12-22 12:05:00.000000', INTERVAL 7 MINUTE)
        ELSE '2025-12-22 12:05:00.000000' END) AS submitted_at,
  (CASE WHEN MOD(s.user_id, 10) = 1 THEN b'1' ELSE b'0' END) AS is_late,
  0 AS late_penalty_points,
  (CASE WHEN MOD(s.user_id, 10) = 1 THEN 0.10 ELSE 0 END) AS late_penalty_rate,
  ROUND(
    @mid_total * (0.55 + (MOD(s.user_id, 6) * 0.07)) *
    (CASE WHEN MOD(s.user_id, 10) = 1 THEN 0.90 ELSE 1 END)
  , 2) AS score,
  CAST(JSON_OBJECT(
    'q1', MOD(s.user_id, 4),
    'q2', CONCAT('주관식 답안(중간) - ', s.user_id)
  ) AS CHAR) AS answer_data,
  '2025-12-23 09:00:00.000000' AS graded_at,
  @course52_professor_id AS graded_by,
  '2025-12-22 12:05:00.000000' AS created_at
FROM tmp_course52_students s;

-- 19: 기말고사 제출 + 교수 채점 완료(점수 입력됨)
INSERT INTO exam_results (
  exam_id, user_id,
  started_at, submitted_at,
  is_late, late_penalty_points, late_penalty_rate,
  score, answer_data,
  graded_at, graded_by,
  created_at
)
SELECT
  19 AS exam_id,
  s.user_id,
  '2025-12-22 14:00:00.000000' AS started_at,
  (CASE WHEN MOD(s.user_id, 10) = 2 THEN DATE_ADD('2025-12-22 15:20:00.000000', INTERVAL 8 MINUTE)
        ELSE '2025-12-22 15:20:00.000000' END) AS submitted_at,
  (CASE WHEN MOD(s.user_id, 10) = 2 THEN b'1' ELSE b'0' END) AS is_late,
  0 AS late_penalty_points,
  (CASE WHEN MOD(s.user_id, 10) = 2 THEN 0.10 ELSE 0 END) AS late_penalty_rate,
  ROUND(
    @final_total * (0.50 + (MOD(s.user_id, 7) * 0.07)) *
    (CASE WHEN MOD(s.user_id, 10) = 2 THEN 0.90 ELSE 1 END)
  , 2) AS score,
  CAST(JSON_OBJECT(
    'q1', MOD(s.user_id + 2, 4),
    'q2', CONCAT('주관식 답안(기말) - ', s.user_id)
  ) AS CHAR) AS answer_data,
  '2025-12-23 10:30:00.000000' AS graded_at,
  @course52_professor_id AS graded_by,
  '2025-12-22 15:20:00.000000' AS created_at
FROM tmp_course52_students s;

-- =====================================================
-- course_id=52 과제 더미 데이터 (assignments.id = 12, post_id = 146)
-- - 성적 산출 테스트를 위해 모든 제출은 GRADED 처리
-- =====================================================

SET @assignment_category_id := (SELECT id FROM board_categories WHERE board_type = 'ASSIGNMENT' LIMIT 1);

-- posts.id=146 이 없을 수도/있을 수도 있으니, 있으면 건드리지 않고 없으면 생성
INSERT IGNORE INTO posts
(`id`, `category_id`, `course_id`, `department_id`, `author_id`, `title`, `content`, `post_type`,
 `is_anonymous`, `view_count`, `like_count`, `is_deleted`,
 `created_by`, `updated_by`, `created_at`, `updated_at`, `deleted_at`)
VALUES
(146, @assignment_category_id, 52, NULL, @course52_professor_id,
 '[과제] 더미 과제 #12', '성적 산출용 더미 과제입니다. (course_id=52)', 'ASSIGNMENT',
 b'0', 0, 0, b'0',
 @course52_professor_id, @course52_professor_id,
 '2025-12-20 09:00:00.000000', NULL, NULL);

INSERT INTO assignments
(`id`, `post_id`, `course_id`, `due_date`, `max_score`, `submission_method`,
 `late_submission_allowed`, `late_penalty_percent`, `max_file_size_mb`, `allowed_file_types`, `instructions`,
 `created_by`, `created_at`, `updated_at`, `updated_by`, `deleted_at`, `is_deleted`)
VALUES
(12, 146, 52, '2025-12-25 23:59:59.000000', 100.00, 'TEXT_INPUT',
 b'1', 0.10, 10, NULL, '텍스트로 제출하세요.',
 @course52_professor_id, '2025-12-20 09:00:00.000000', NULL, NULL, NULL, b'0')
ON DUPLICATE KEY UPDATE
  `post_id` = VALUES(`post_id`),
  `course_id` = VALUES(`course_id`),
  `due_date` = VALUES(`due_date`),
  `max_score` = VALUES(`max_score`),
  `submission_method` = VALUES(`submission_method`),
  `late_submission_allowed` = VALUES(`late_submission_allowed`),
  `late_penalty_percent` = VALUES(`late_penalty_percent`),
  `max_file_size_mb` = VALUES(`max_file_size_mb`),
  `allowed_file_types` = VALUES(`allowed_file_types`),
  `instructions` = VALUES(`instructions`),
  `updated_at` = VALUES(`updated_at`),
  `updated_by` = VALUES(`updated_by`),
  `deleted_at` = VALUES(`deleted_at`),
  `is_deleted` = VALUES(`is_deleted`);

-- assignment_submissions: 30명 모두 제출 + 채점 완료
INSERT INTO assignment_submissions
(`assignment_id`, `user_id`, `content`, `submitted_at`, `status`, `score`, `feedback`,
 `graded_at`, `graded_by`, `created_at`, `updated_at`, `deleted_at`,
 `created_by`, `updated_by`, `is_deleted`, `allow_resubmission`, `resubmission_deadline`)
SELECT
  12 AS assignment_id,
  s.user_id,
  CONCAT('과제 제출 - ', s.user_id) AS content,
  (CASE WHEN MOD(s.user_id, 10) = 0 THEN '2025-12-26 00:10:00.000000' ELSE '2025-12-24 20:00:00.000000' END) AS submitted_at,
  'GRADED' AS status,
  ROUND(100.00 * (0.60 + (MOD(s.user_id, 5) * 0.08)), 2) AS score,
  'OK' AS feedback,
  '2025-12-27 09:00:00.000000' AS graded_at,
  (SELECT professor_id FROM courses WHERE id = 52) AS graded_by,
  '2025-12-24 20:00:00.000000' AS created_at,
  NULL AS updated_at,
  NULL AS deleted_at,
  s.user_id AS created_by,
  s.user_id AS updated_by,
  0 AS is_deleted,
  FALSE AS allow_resubmission,
  NULL AS resubmission_deadline
FROM (
  SELECT 20250102100 AS user_id UNION ALL SELECT 20250102101 UNION ALL SELECT 20250102102 UNION ALL SELECT 20250102103 UNION ALL SELECT 20250102104
  UNION ALL SELECT 20250102105 UNION ALL SELECT 20250102106 UNION ALL SELECT 20250102107 UNION ALL SELECT 20250102108 UNION ALL SELECT 20250102109
  UNION ALL SELECT 20250102110 UNION ALL SELECT 20250102111 UNION ALL SELECT 20250102112 UNION ALL SELECT 20250102113 UNION ALL SELECT 20250102114
  UNION ALL SELECT 20250102115 UNION ALL SELECT 20250102116 UNION ALL SELECT 20250102117 UNION ALL SELECT 20250102118 UNION ALL SELECT 20250102119
  UNION ALL SELECT 20250102120 UNION ALL SELECT 20250102121 UNION ALL SELECT 20250102122 UNION ALL SELECT 20250102123 UNION ALL SELECT 20250102124
  UNION ALL SELECT 20250102125 UNION ALL SELECT 20250102126 UNION ALL SELECT 20250102127 UNION ALL SELECT 20250102128 UNION ALL SELECT 20250102129
) s
ON DUPLICATE KEY UPDATE
  `content` = VALUES(`content`),
  `submitted_at` = VALUES(`submitted_at`),
  `status` = VALUES(`status`),
  `score` = VALUES(`score`),
  `feedback` = VALUES(`feedback`),
  `graded_at` = VALUES(`graded_at`),
  `graded_by` = VALUES(`graded_by`),
  `updated_at` = VALUES(`updated_at`),
  `updated_by` = VALUES(`updated_by`),
  `deleted_at` = VALUES(`deleted_at`),
  `is_deleted` = VALUES(`is_deleted`),
  `allow_resubmission` = VALUES(`allow_resubmission`),
  `resubmission_deadline` = VALUES(`resubmission_deadline`);

DROP TEMPORARY TABLE tmp_course52_students;


