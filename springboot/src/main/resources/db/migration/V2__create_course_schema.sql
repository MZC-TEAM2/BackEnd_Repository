--
-- Table structure for table `academic_terms`
--

CREATE TABLE IF NOT EXISTS    `academic_terms` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '학기 식별자',
  `year` int NOT NULL COMMENT '학년도 (예: 2024)',
  `term_type` varchar(10) NOT NULL COMMENT '학기 구분 (1:봄학기/2:가을학기/SUMMER:여름학기/WINTER:겨울학기)',
  `start_date` date NOT NULL COMMENT '학기 시작일',
  `end_date` date NOT NULL COMMENT '학기 종료일',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- --------------------------------------------------------
-- Table structure for table `courses`
--

CREATE TABLE IF NOT EXISTS   `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '강의 식별자',
  `subject_id` bigint NOT NULL COMMENT '과목 ID',
  `academic_term_id` bigint NOT NULL COMMENT '학기 ID',
  `section_number` varchar(10) NOT NULL COMMENT '분반 번호 (01, 02, A, B 등)',
  `current_students` int NOT NULL COMMENT '현재 수강생 수',
  `professor_id` bigint NOT NULL COMMENT '담당 교수 ID',
  `max_students` int NOT NULL COMMENT '수강 정원',
  `created_at` timestamp NULL DEFAULT (now()) COMMENT '강의 개설 일시',
  `description` text COMMENT '강의 설명 (분반별)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `courses`
--

--
-- Table structure for table `course_carts`
--

CREATE TABLE IF NOT EXISTS   `course_carts` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '장바구니 항목 식별자',
  `student_id` bigint NOT NULL COMMENT '학생 ID',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `added_at` timestamp NULL DEFAULT (now()) COMMENT '담은 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `course_schedules`
--

CREATE TABLE IF NOT EXISTS   `course_schedules` (
  `schedule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '시간표 식별자',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `day_of_week` int NOT NULL COMMENT '요일 (1:월요일 ~ 5:금요일)',
  `start_time` time NOT NULL COMMENT '시작 시간 (예: 09:00)',
  `end_time` time NOT NULL COMMENT '종료 시간 (예: 10:30)',
  `schedule_room` varchar(50) NOT NULL COMMENT '강의실 (예: 공학관 401호)',
  PRIMARY KEY (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `course_status_types`
--

CREATE TABLE IF NOT EXISTS   `course_status_types` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '상태 식별자',
  `status_code` varchar(20) NOT NULL COMMENT '상태 코드 (ONGOING/COMPLETED/PENDING)',
  `status_name` varchar(50) NOT NULL COMMENT '상태명 (진행중/완료/대기)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `course_types`
--

CREATE TABLE IF NOT EXISTS   `course_types` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '유형 식별자',
  `type_code` int NOT NULL COMMENT '유형 코드 (0: MAJOR_REQ, 1: MAJOR_ELEC, 2: GEN_REQ, 3: GEN_ELEC)',
  `category` int NOT NULL COMMENT '상위 분류 (0: 전공, 1: 교양)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- --------------------------------------------------------

--
-- Table structure for table `course_weeks`
--

CREATE TABLE IF NOT EXISTS   `course_weeks` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '주차 식별자',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `week_number` int NOT NULL COMMENT '주차 번호 (1, 2, 3...)',
  `week_title` varchar(200) NOT NULL COMMENT '주차 제목 (예: 1주차: 데이터베이스 개요)',
  `created_at` timestamp NULL DEFAULT (now()) COMMENT '생성 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `enrollments`
--

CREATE TABLE IF NOT EXISTS   `enrollments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '수강신청 식별자',
  `course_id` bigint NOT NULL COMMENT '강의 ID',
  `student_id` bigint NOT NULL COMMENT '학생 ID',
  `enrolled_at` timestamp NULL DEFAULT (now()) COMMENT '수강신청 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `enrollment_periods`
--

CREATE TABLE IF NOT EXISTS   `enrollment_periods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '수강신청 기간 식별자',
  `term_id` bigint NOT NULL COMMENT '학기 ID',
  `period_name` varchar(50) NOT NULL COMMENT '기간명 (예: 1차 수강신청, 정정기간)',
  `period_type_id` int NOT NULL COMMENT '기간 타입 ID',
  `start_datetime` timestamp NOT NULL COMMENT '시작 일시',
  `end_datetime` timestamp NOT NULL COMMENT '종료 일시',
  `target_year` int DEFAULT NULL COMMENT '대상 학년 (0이면 전체)',
  `created_at` timestamp NULL DEFAULT (now()) COMMENT '생성 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



-- --------------------------------------------------------

--
-- Table structure for table `enrollment_status_types`
--

CREATE TABLE IF NOT EXISTS   `enrollment_status_types` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '상태 식별자',
  `status_code` varchar(20) NOT NULL COMMENT '상태 코드 (ENROLLED/COMPLETED)',
  `status_name` varchar(50) NOT NULL COMMENT '상태명 (수강중/완강)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------
--
-- Table structure for table `period_types`
--

CREATE TABLE IF NOT EXISTS   `period_types` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '기간 타입 식별자',
  `type_code` varchar(20) NOT NULL COMMENT '타입 코드 (ENROLLMENT, COURSE_REGISTRATION, ADJUSTMENT, CANCELLATION)',
  `type_name` varchar(50) NOT NULL COMMENT '타입명 (수강신청, 강의등록, 정정, 수강철회)',
  `description` varchar(200) DEFAULT NULL COMMENT '설명',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `period_types`
--

--
-- Table structure for table `subjects`
--

CREATE TABLE IF NOT EXISTS   `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '과목 식별자',
  `subject_code` varchar(8) NOT NULL COMMENT '과목 코드 (예: CS101)',
  `subject_name` varchar(64) NOT NULL COMMENT '과목명 (예: 자료구조)',
  `department_id` bigint NOT NULL COMMENT '개설 학과 ID',
  `course_type_id` int NOT NULL COMMENT '이수 구분 ID',
  `credits` int NOT NULL COMMENT '학점 수 (1~4)',
  `theory_hours` int DEFAULT '0' COMMENT '이론 시수',
  `practice_hours` int DEFAULT '0' COMMENT '실습 시수',
  `subject_description` varchar(200) NOT NULL COMMENT '과목 개요',
  `description` text COMMENT '과목 상세 설명',
  `created_at` timestamp NULL DEFAULT (now()) COMMENT '과목 등록 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `subject_prerequisites`
--

CREATE TABLE IF NOT EXISTS   `subject_prerequisites` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '관계 식별자',
  `subject_id` bigint NOT NULL COMMENT '과목 ID',
  `prerequisite_id` bigint NOT NULL COMMENT '선수과목 ID',
  `is_mandatory` tinyint(1) DEFAULT '1' COMMENT '필수 이수 여부',
  `created_at` timestamp NULL DEFAULT (now()) COMMENT '생성 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `week_contents`
--

CREATE TABLE IF NOT EXISTS `week_contents` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '콘텐츠 식별자',
  `week_id` bigint NOT NULL COMMENT '주차 ID',
  `content_type` varchar(20) NOT NULL COMMENT '콘텐츠 유형 (VIDEO/DOCUMENT/LINK)',
  `title` varchar(200) NOT NULL COMMENT '콘텐츠 제목',
  `content_url` varchar(500) NOT NULL COMMENT '콘텐츠 URL',
  `duration` varchar(10) DEFAULT NULL COMMENT '동영상 길이 표시 (예: 45:23)',
  `display_order` int NOT NULL COMMENT '표시 순서',
  `created_at` timestamp NULL DEFAULT (now()) COMMENT '생성 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- -- Indexes for dumped tables
-- --

-- --
-- -- Indexes for table `academic_terms`
-- --
-- ALTER TABLE `academic_terms`
--   ADD PRIMARY KEY (`id`);


-- -- Indexes for table `courses`
-- --
-- ALTER TABLE `courses`
--   ADD PRIMARY KEY (`id`);

-- --
-- -- Indexes for table `course_carts`
-- --
-- ALTER TABLE `course_carts`
--   ADD PRIMARY KEY (`id`);

-- --
-- -- Indexes for table `course_schedules`
-- --
-- ALTER TABLE `course_schedules`
--   ADD PRIMARY KEY (`schedule_id`);

-- --
-- -- Indexes for table `course_status_types`
-- --
-- ALTER TABLE `course_status_types`
--   ADD PRIMARY KEY (`id`),
--   ADD UNIQUE KEY `status_code` (`status_code`);

-- --
-- -- Indexes for table `course_types`
-- --
-- ALTER TABLE `course_types`
--   ADD PRIMARY KEY (`id`),
--   ADD UNIQUE KEY `type_code` (`type_code`);

-- --
-- -- Indexes for table `course_weeks`
-- --
-- ALTER TABLE `course_weeks`
--   ADD PRIMARY KEY (`id`);

-- --
-- -- Indexes for table `enrollments`
-- --
-- ALTER TABLE `enrollments`
--   ADD PRIMARY KEY (`id`);

-- --
-- -- Indexes for table `enrollment_periods`
-- --
-- ALTER TABLE `enrollment_periods`
--   ADD PRIMARY KEY (`id`),
--   ADD KEY `idx_enrollment_periods_period_type_id` (`period_type_id`),
--   ADD KEY `idx_enrollment_periods_type_datetime` (`period_type_id`,`start_datetime`,`end_datetime`);

-- --
-- -- Indexes for table `enrollment_status_types`
-- --
-- ALTER TABLE `enrollment_status_types`
--   ADD PRIMARY KEY (`id`),
--   ADD UNIQUE KEY `status_code` (`status_code`);

-- -- Indexes for table `period_types`
-- --
-- ALTER TABLE `period_types`
--   ADD PRIMARY KEY (`id`),
--   ADD UNIQUE KEY `type_code` (`type_code`);




-- --
-- -- Indexes for table `subject_prerequisites`
-- --
-- ALTER TABLE `subject_prerequisites`
--   ADD PRIMARY KEY (`id`);

-- --
-- -- Indexes for table `week_contents`
-- --
-- ALTER TABLE `week_contents`
--   ADD PRIMARY KEY (`id`);

-- --
-- -- AUTO_INCREMENT for dumped tables
-- --

-- --
-- -- AUTO_INCREMENT for table `academic_terms`
-- --
-- ALTER TABLE `academic_terms`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '학기 식별자', AUTO_INCREMENT=2;
-- --
-- -- AUTO_INCREMENT for table `courses`
-- --
-- ALTER TABLE `courses`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '강의 식별자', AUTO_INCREMENT=49;

-- --
-- -- AUTO_INCREMENT for table `course_carts`
-- --
-- ALTER TABLE `course_carts`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '장바구니 항목 식별자', AUTO_INCREMENT=134;

-- --
-- -- AUTO_INCREMENT for table `course_schedules`
-- --
-- ALTER TABLE `course_schedules`
--   MODIFY `schedule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '시간표 식별자', AUTO_INCREMENT=71;

-- --
-- -- AUTO_INCREMENT for table `course_status_types`
-- --
-- ALTER TABLE `course_status_types`
--   MODIFY `id` int NOT NULL AUTO_INCREMENT COMMENT '상태 식별자';

-- --
-- -- AUTO_INCREMENT for table `course_types`
-- --
-- ALTER TABLE `course_types`
--   MODIFY `id` int NOT NULL AUTO_INCREMENT COMMENT '유형 식별자', AUTO_INCREMENT=5;

-- --
-- -- AUTO_INCREMENT for table `course_weeks`
-- --
-- ALTER TABLE `course_weeks`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '주차 식별자', AUTO_INCREMENT=19;

-- --
-- -- AUTO_INCREMENT for table `departments`
-- --
-- ALTER TABLE `departments`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

-- --
-- -- AUTO_INCREMENT for table `email_verifications`
-- --
-- ALTER TABLE `email_verifications`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

-- --
-- -- AUTO_INCREMENT for table `enrollments`
-- --
-- ALTER TABLE `enrollments`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '수강신청 식별자', AUTO_INCREMENT=33;

-- --
-- -- AUTO_INCREMENT for table `enrollment_periods`
-- --
-- ALTER TABLE `enrollment_periods`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '수강신청 기간 식별자', AUTO_INCREMENT=3;

-- --
-- -- AUTO_INCREMENT for table `enrollment_status_types`
-- --
-- ALTER TABLE `enrollment_status_types`
--   MODIFY `id` int NOT NULL AUTO_INCREMENT COMMENT '상태 식별자';


-- --
-- -- AUTO_INCREMENT for table `subjects`
-- --
-- ALTER TABLE `subjects`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '과목 식별자', AUTO_INCREMENT=62;

-- --
-- -- AUTO_INCREMENT for table `subject_prerequisites`
-- --
-- ALTER TABLE `subject_prerequisites`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '관계 식별자', AUTO_INCREMENT=21;

-- ALTER TABLE `week_contents`
--   MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '콘텐츠 식별자', AUTO_INCREMENT=2;

-- ALTER TABLE `enrollment_periods`
--   ADD CONSTRAINT `fk_enrollment_periods_period_type` FOREIGN KEY (`period_type_id`) REFERENCES `period_types` (`id`);
