CREATE TABLE `courses` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '강의 식별자',
  `subject_id` bigint NOT NULL COMMENT '과목 ID',
  `academic_term_id` bigint NOT NULL COMMENT '학기 ID',
  `section_number` varchar(10) NOT NULL COMMENT '분반 번호 (01, 02, A, B 등)',
  `current_students` int NOT NULL COMMENT '현재 수강생 수',
  `professor_id` bigint NOT NULL COMMENT '담당 교수 ID',
  `max_students` int NOT NULL COMMENT '수강 정원',
  `created_at` timestamp DEFAULT (now()) COMMENT '강의 개설 일시'
);

CREATE TABLE `academic_terms` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '학기 식별자',
  `year` int NOT NULL COMMENT '학년도 (예: 2024)',
  `term_type` varchar(10) NOT NULL COMMENT '학기 구분 (1:봄학기/2:가을학기/SUMMER:여름학기/WINTER:겨울학기)',
  `start_date` date NOT NULL COMMENT '학기 시작일',
  `end_date` date NOT NULL COMMENT '학기 종료일'
);


CREATE TABLE `course_types` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '유형 식별자',
  `type_code` int UNIQUE NOT NULL COMMENT '유형 코드 (0: MAJOR_REQ, 1: MAJOR_ELEC, 2: GEN_REQ, 3: GEN_ELEC)',
  `category` int NOT NULL COMMENT '상위 분류 (0: 전공, 1: 교양)'
);

CREATE TABLE `subjects` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '과목 식별자',
  `subject_code` varchar(8) UNIQUE NOT NULL COMMENT '과목 코드 (예: CS101)',
  `subject_name` varchar(20) NOT NULL COMMENT '과목명 (예: 자료구조)',
  `department_id` bigint NOT NULL COMMENT '개설 학과 ID',
  `course_type_id` int NOT NULL COMMENT '이수 구분 ID',
  `credits` int NOT NULL COMMENT '학점 수 (1~4)',
  `theory_hours` int DEFAULT 0 COMMENT '이론 시수',
  `practice_hours` int DEFAULT 0 COMMENT '실습 시수',
  `subject_description` varchar(200) NOT NULL COMMENT '과목 개요',
  `description` text COMMENT '과목 상세 설명',
  `created_at` timestamp DEFAULT (now()) COMMENT '과목 등록 일시'
);

CREATE TABLE `subject_prerequisites` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '관계 식별자',
  `subject_id` bigint NOT NULL COMMENT '과목 ID',
  `prerequisite_id` bigint NOT NULL COMMENT '선수과목 ID',
  `is_mandatory` boolean DEFAULT true COMMENT '필수 이수 여부',
  `created_at` timestamp DEFAULT (now()) COMMENT '생성 일시'
);

CREATE TABLE `course_schedules` (
  `schedule_id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '시간표 식별자',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `day_of_week` int NOT NULL COMMENT '요일 (1:월요일 ~ 5:금요일)',
  `start_time` time NOT NULL COMMENT '시작 시간 (예: 09:00)',
  `end_time` time NOT NULL COMMENT '종료 시간 (예: 10:30)',
  `schedule_room` varchar(50) NOT NULL COMMENT '강의실 (예: 공학관 401호)'
);

CREATE TABLE `course_carts` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '장바구니 항목 식별자',
  `student_id` bigint NOT NULL COMMENT '학생 ID',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `added_at` timestamp DEFAULT (now()) COMMENT '담은 일시'
);

CREATE TABLE `enrollment_periods` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '수강신청 기간 식별자',
  `term_id` bigint NOT NULL COMMENT '학기 ID',
  `period_name` varchar(50) NOT NULL COMMENT '기간명 (예: 1차 수강신청, 정정기간)',
  `start_datetime` timestamp NOT NULL COMMENT '시작 일시',
  `end_datetime` timestamp NOT NULL COMMENT '종료 일시',
  `target_year` int COMMENT '대상 학년 (0이면 전체)',
  `created_at` timestamp DEFAULT (now()) COMMENT '생성 일시'
);


CREATE TABLE `course_weeks` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '주차 식별자',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `week_number` int NOT NULL COMMENT '주차 번호 (1, 2, 3...)',
  `week_title` varchar(200) NOT NULL COMMENT '주차 제목 (예: 1주차: 데이터베이스 개요)',
  `created_at` timestamp DEFAULT (now()) COMMENT '생성 일시'
);

CREATE TABLE `week_contents` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '콘텐츠 식별자',
  `week_id` bigint NOT NULL COMMENT '주차 ID',
  `content_type` varchar(20) NOT NULL COMMENT '콘텐츠 유형 (VIDEO/DOCUMENT/LINK)',
  `title` varchar(200) NOT NULL COMMENT '콘텐츠 제목',
  `content_url` varchar(500) NOT NULL COMMENT '콘텐츠 URL',
  `duration` varchar(10) COMMENT '동영상 길이 표시 (예: 45:23)',
  `display_order` int NOT NULL COMMENT '표시 순서',
  `created_at` timestamp DEFAULT (now()) COMMENT '생성 일시'
);
CREATE TABLE `student_content_progress` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '진행 상황 식별자',
  `content_id` bigint NOT NULL COMMENT '콘텐츠 ID',
  `student_id` bigint NOT NULL COMMENT '학생 ID',
  `is_completed` boolean DEFAULT false COMMENT '완료 여부',
  `progress_percentage` int DEFAULT 0 COMMENT '진행률 (0-100)',
  `last_position_seconds` int COMMENT '마지막 시청 위치 (초) - 동영상인 경우',
  `completed_at` timestamp COMMENT '완료 일시',
  `first_accessed_at` timestamp COMMENT '최초 접근 일시',
  `last_accessed_at` timestamp COMMENT '마지막 접근 일시',
  `access_count` int DEFAULT 0 COMMENT '접근 횟수'
);
CREATE TABLE `course_status_types` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '상태 식별자',
  `status_code` varchar(20) UNIQUE NOT NULL COMMENT '상태 코드 (ONGOING/COMPLETED/PENDING)',
  `status_name` varchar(50) NOT NULL COMMENT '상태명 (진행중/완료/대기)'
);

CREATE TABLE `enrollments` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '수강신청 식별자',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `student_id` bigint NOT NULL COMMENT '학생 ID',
  `enrolled_at` timestamp DEFAULT (now()) COMMENT '수강신청 일시'
);

CREATE TABLE `enrollment_status_types` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '상태 식별자',
  `status_code` varchar(20) UNIQUE NOT NULL COMMENT '상태 코드 (ENROLLED/COMPLETED)',
  `status_name` varchar(50) NOT NULL COMMENT '상태명 (수강중/완강)'
);

CREATE TABLE `attendance_sessions` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT COMMENT '출석 세션 식별자',
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `session_date` date NOT NULL COMMENT '수업 날짜',
  `session_number` int NOT NULL COMMENT '수업 차시 (1차시, 2차시...)'
);
