-- V23__seed_week_attendance_course_52.sql
-- course_id=52, week_id=38~53 출석 더미 데이터 생성
-- - 전체 VIDEO 갯수(total_video_count): 1
-- - 학번(student_id) 오름차순 앞 3명은 각각 3주 결석
--
-- NOTE:
-- - `week_attendance`는 (student_id, week_id) 유니크 제약이 있으므로, 해당 조합이 이미 존재하면 실행 시 에러가 납니다.
--   (Flyway는 기본적으로 1회만 실행되므로 일반적으로 문제 없습니다.)

INSERT INTO week_attendance (
  student_id,
  week_id,
  course_id,
  is_completed,
  completed_video_count,
  total_video_count,
  first_accessed_at,
  completed_at
)
SELECT
  s.student_id,
  w.week_id,
  52 AS course_id,
  CASE
    WHEN (s.rn = 1 AND w.week_id IN (38, 39, 40))
      OR (s.rn = 2 AND w.week_id IN (41, 42, 43))
      OR (s.rn = 3 AND w.week_id IN (44, 45, 46))
    THEN FALSE
    ELSE TRUE
  END AS is_completed,
  CASE
    WHEN (s.rn = 1 AND w.week_id IN (38, 39, 40))
      OR (s.rn = 2 AND w.week_id IN (41, 42, 43))
      OR (s.rn = 3 AND w.week_id IN (44, 45, 46))
    THEN 0
    ELSE 1
  END AS completed_video_count,
  1 AS total_video_count,
  CASE
    WHEN (s.rn = 1 AND w.week_id IN (38, 39, 40))
      OR (s.rn = 2 AND w.week_id IN (41, 42, 43))
      OR (s.rn = 3 AND w.week_id IN (44, 45, 46))
    THEN NULL
    ELSE '2025-12-22 10:00:00'
  END AS first_accessed_at,
  CASE
    WHEN (s.rn = 1 AND w.week_id IN (38, 39, 40))
      OR (s.rn = 2 AND w.week_id IN (41, 42, 43))
      OR (s.rn = 3 AND w.week_id IN (44, 45, 46))
    THEN NULL
    ELSE '2025-12-22 12:00:00'
  END AS completed_at
FROM (
  SELECT
    e.student_id,
    ROW_NUMBER() OVER (ORDER BY e.student_id) AS rn
  FROM enrollments e
  WHERE e.course_id = 52
) s
CROSS JOIN (
  SELECT 38 AS week_id UNION ALL
  SELECT 39 UNION ALL
  SELECT 40 UNION ALL
  SELECT 41 UNION ALL
  SELECT 42 UNION ALL
  SELECT 43 UNION ALL
  SELECT 44 UNION ALL
  SELECT 45 UNION ALL
  SELECT 46 UNION ALL
  SELECT 47 UNION ALL
  SELECT 48 UNION ALL
  SELECT 49 UNION ALL
  SELECT 50 UNION ALL
  SELECT 51 UNION ALL
  SELECT 52 UNION ALL
  SELECT 53
) w;


