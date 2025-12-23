-- MySQL dump 10.13  Distrib 8.0.44, for Linux (aarch64)
--
-- Host: localhost    Database: lms_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `academic_terms`
--

DROP TABLE IF EXISTS `academic_terms`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_terms`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '학기 식별자',
    `year`       int          NOT NULL COMMENT '학년도 (예: 2024)',
    `term_type`  varchar(255) NOT NULL,
    `start_date` date         NOT NULL COMMENT '학기 시작일',
    `end_date`   date         NOT NULL COMMENT '학기 종료일',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `assignment_submissions`
--

DROP TABLE IF EXISTS `assignment_submissions`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignment_submissions`
(
    `id`                    bigint                                 NOT NULL AUTO_INCREMENT,
    `assignment_id`         bigint                                 NOT NULL COMMENT '과제 ID',
    `user_id`               bigint                                 NOT NULL COMMENT '제출자 ID',
    `content`               text COLLATE utf8mb4_unicode_ci COMMENT '텍스트 제출 내용',
    `submitted_at`          datetime(6)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '제출일시',
    `status`                varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUBMITTED' COMMENT '제출 상태',
    `score`                 decimal(5, 2)                                   DEFAULT NULL COMMENT '획득 점수',
    `feedback`              text COLLATE utf8mb4_unicode_ci COMMENT '피드백',
    `graded_at`             datetime(6)                                     DEFAULT NULL COMMENT '채점일시',
    `graded_by`             bigint                                          DEFAULT NULL COMMENT '채점자 ID',
    `created_at`            datetime(6)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`            datetime(6)                                     DEFAULT NULL COMMENT '수정일시',
    `deleted_at`            datetime(6)                                     DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    `created_by`            bigint                                          DEFAULT NULL COMMENT '생성자 ID',
    `updated_by`            bigint                                          DEFAULT NULL COMMENT '수정자 ID',
    `is_deleted`            tinyint(1)                             NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `allow_resubmission`    tinyint(1)                                      DEFAULT '0' COMMENT '재제출 허용 여부',
    `resubmission_deadline` datetime(6)                                     DEFAULT NULL COMMENT '재제출 마감일',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_assignment_user` (`assignment_id`, `user_id`),
    UNIQUE KEY `UKciw6gjdxt6r9cogkbrnh3e77u` (`assignment_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_submitted_at` (`submitted_at`),
    KEY `fk_assignment_submissions_grader` (`graded_by`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_is_deleted` (`is_deleted`),
    KEY `fk_assignment_submissions_updater` (`updated_by`),
    CONSTRAINT `fk_assignment_submissions_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`),
    CONSTRAINT `fk_assignment_submissions_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_assignment_submissions_grader` FOREIGN KEY (`graded_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_assignment_submissions_updater` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_assignment_submissions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='과제 제출';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `assignments`
--

DROP TABLE IF EXISTS `assignments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assignments`
(
    `id`                      bigint                                 NOT NULL AUTO_INCREMENT,
    `post_id`                 bigint                                 NOT NULL COMMENT '게시글 ID (제목/내용은 posts 테이블 참조)',
    `course_id`               bigint                                 NOT NULL COMMENT '강의 ID',
    `due_date`                datetime(6)                            NOT NULL COMMENT '제출 마감일',
    `max_score`               decimal(5, 2)                          NOT NULL COMMENT '만점',
    `submission_method`       varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제출방법 (FILE_UPLOAD/TEXT_INPUT/BOTH)',
    `late_submission_allowed` bit(1)                                 NOT NULL DEFAULT b'0' COMMENT '지각 제출 허용',
    `late_penalty_percent`    decimal(3, 2)                                   DEFAULT NULL COMMENT '지각 제출 감점 비율 (%)',
    `max_file_size_mb`        int                                             DEFAULT '10' COMMENT '최대 파일 크기 (MB)',
    `allowed_file_types`      varchar(255) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '허용 파일 확장자 (쉼표 구분)',
    `instructions`            text COLLATE utf8mb4_unicode_ci COMMENT '제출 지침',
    `created_by`              bigint                                 NOT NULL COMMENT '생성자 ID',
    `created_at`              datetime(6)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`              datetime(6)                                     DEFAULT NULL COMMENT '수정일시',
    `updated_by`              bigint                                          DEFAULT NULL COMMENT '수정자 ID',
    `deleted_at`              datetime(6)                                     DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    `is_deleted`              bit(1)                                 NOT NULL DEFAULT b'0' COMMENT '삭제 여부',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_assignments_post_id` (`post_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_due_date` (`due_date`),
    KEY `idx_created_by` (`created_by`),
    CONSTRAINT `fk_assignments_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_assignments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='과제';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attachment_downloads`
--

DROP TABLE IF EXISTS `attachment_downloads`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attachment_downloads`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT,
    `attachment_id` bigint      NOT NULL COMMENT '첨부파일 ID',
    `user_id`       bigint                                 DEFAULT NULL COMMENT '다운로더 ID (비로그인 시 null)',
    `ip_address`    varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP 주소',
    `downloaded_at` datetime(6) NOT NULL                   DEFAULT CURRENT_TIMESTAMP(6) COMMENT '다운로드일시',
    PRIMARY KEY (`id`),
    KEY `idx_attachment_id` (`attachment_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_downloaded_at` (`downloaded_at`),
    CONSTRAINT `fk_attachment_downloads_attachment` FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`id`),
    CONSTRAINT `fk_attachment_downloads_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='다운로드 추적';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attachments`
--

DROP TABLE IF EXISTS `attachments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attachments`
(
    `id`              bigint                                                                                                              NOT NULL AUTO_INCREMENT,
    `post_id`         bigint                                                                                                                       DEFAULT NULL COMMENT '게시글 ID',
    `comment_id`      bigint                                                                                                                       DEFAULT NULL COMMENT '댓글 ID',
    `attachment_type` enum ('ARCHIVE','AUDIO','DOCUMENT','IMAGE','OTHER','PRESENTATION','SPREADSHEET','VIDEO') COLLATE utf8mb4_unicode_ci NOT NULL,
    `original_name`   varchar(255) COLLATE utf8mb4_unicode_ci                                                                             NOT NULL COMMENT '원본 파일명',
    `stored_name`     varchar(255) COLLATE utf8mb4_unicode_ci                                                                             NOT NULL COMMENT '서버 저장 파일명 (UUID)',
    `file_path`       varchar(255) COLLATE utf8mb4_unicode_ci                                                                             NOT NULL,
    `file_size`       bigint                                                                                                              NOT NULL COMMENT '파일 크기 (bytes)',
    `mime_type`       varchar(100) COLLATE utf8mb4_unicode_ci                                                                                      DEFAULT NULL COMMENT 'MIME 타입',
    `uploader_id`     bigint                                                                                                                       DEFAULT NULL COMMENT '업로더 ID',
    `download_count`  int                                                                                                                          DEFAULT '0' COMMENT '다운로드 횟수',
    `is_deleted`      bit(1)                                                                                                                       DEFAULT b'0' COMMENT '삭제 여부',
    `created_at`      datetime(6)                                                                                                         NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '업로드일시',
    `updated_at`      datetime(6)                                                                                                                  DEFAULT NULL COMMENT '수정일시',
    `deleted_at`      datetime(6)                                                                                                                  DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`id`),
    KEY `idx_post_active_attachments` (`post_id`, `deleted_at`),
    KEY `idx_comment_active_attachments` (`comment_id`, `deleted_at`),
    KEY `idx_uploader_id` (`uploader_id`),
    KEY `idx_attachment_type` (`attachment_type`),
    KEY `idx_mime_type` (`mime_type`),
    CONSTRAINT `fk_attachments_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`),
    CONSTRAINT `fk_attachments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_attachments_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='첨부파일';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `board_categories`
--

DROP TABLE IF EXISTS `board_categories`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `board_categories`
(
    `id`                bigint                                                                                                                                                                         NOT NULL AUTO_INCREMENT,
    `board_type`        enum ('ASSIGNMENT','CAREER','CONTEST','DEPARTMENT','DISCUSSION','EXAM','FREE','NOTICE','PROFESSOR','QUESTION','QUIZ','STUDENT','STUDY_RECRUITMENT') COLLATE utf8mb4_unicode_ci NOT NULL,
    `allow_comments`    bit(1)                                                                                                                                                                                  DEFAULT b'1' COMMENT '댓글 허용 여부',
    `allow_attachments` bit(1)                                                                                                                                                                                  DEFAULT b'1' COMMENT '첨부파일 허용 여부',
    `allow_anonymous`   bit(1)                                                                                                                                                                                  DEFAULT b'0' COMMENT '익명 작성 허용 여부',
    `is_deleted`        bit(1)                                                                                                                                                                                  DEFAULT b'0' COMMENT '삭제 여부',
    `created_at`        datetime(6)                                                                                                                                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`        datetime(6)                                                                                                                                                                             DEFAULT NULL COMMENT '수정일시',
    `deleted_at`        datetime(6)                                                                                                                                                                             DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `board_type` (`board_type`),
    UNIQUE KEY `uk_board_type` (`board_type`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='게시판 카테고리';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `colleges`
--

DROP TABLE IF EXISTS `colleges`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `colleges`
(
    `id`                  bigint                                  NOT NULL AUTO_INCREMENT,
    `college_code`        varchar(20) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `college_number_code` varchar(2) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `college_name`        varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at`          datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_colleges_code` (`college_code`),
    UNIQUE KEY `uk_colleges_number_code` (`college_number_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comments`
(
    `id`                  bigint                          NOT NULL AUTO_INCREMENT,
    `post_id`             bigint                          NOT NULL COMMENT '게시글 ID',
    `author_id`           bigint                          NOT NULL COMMENT '작성자 ID',
    `parent_comment_id`   bigint                                   DEFAULT NULL COMMENT '부모 댓글 ID (대댓글용)',
    `content`             text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '댓글 내용',
    `depth`               int                                      DEFAULT '0' COMMENT '댓글 깊이 (0: 댓글, 1: 대댓글)',
    `is_anonymous`        bit(1)                                   DEFAULT b'0' COMMENT '익명 댓글 여부',
    `is_deleted`          bit(1)                                   DEFAULT b'0' COMMENT '삭제 여부 (성능 최적화용)',
    `is_deleted_by_admin` bit(1)                                   DEFAULT b'0' COMMENT '관리자에 의한 삭제 여부',
    `created_by`          bigint                          NOT NULL COMMENT '생성자 ID',
    `updated_by`          bigint                                   DEFAULT NULL COMMENT '수정자 ID',
    `created_at`          datetime(6)                     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`          datetime(6)                              DEFAULT NULL COMMENT '수정일시',
    `deleted_at`          datetime(6)                              DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`id`),
    KEY `idx_post_active_created` (`post_id`, `is_deleted`, `created_at`),
    KEY `idx_parent_active_created` (`parent_comment_id`, `is_deleted`, `created_at`),
    KEY `idx_author_created` (`author_id`, `created_at`),
    KEY `idx_depth` (`depth`),
    KEY `idx_is_deleted` (`is_deleted`),
    KEY `idx_created_by` (`created_by`),
    KEY `fk_comments_updated_by` (`updated_by`),
    CONSTRAINT `fk_comments_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_comments_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_comment_id`) REFERENCES `comments` (`id`),
    CONSTRAINT `fk_comments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_comments_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='댓글';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversations`
(
    `id`                     bigint    NOT NULL AUTO_INCREMENT,
    `user1_id`               bigint    NOT NULL,
    `user2_id`               bigint    NOT NULL,
    `user1_unread_count`     int       NOT NULL                      DEFAULT '0',
    `user2_unread_count`     int       NOT NULL                      DEFAULT '0',
    `user1_deleted_at`       timestamp NULL                          DEFAULT NULL,
    `user2_deleted_at`       timestamp NULL                          DEFAULT NULL,
    `last_message_content`   varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `last_message_at`        timestamp NULL                          DEFAULT NULL,
    `last_message_sender_id` bigint                                  DEFAULT NULL,
    `created_at`             timestamp NOT NULL                      DEFAULT CURRENT_TIMESTAMP,
    `updated_at`             timestamp NOT NULL                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversations_users` (`user1_id`, `user2_id`),
    KEY `idx_conversations_user1` (`user1_id`),
    KEY `idx_conversations_user2` (`user2_id`),
    KEY `idx_conversations_last_message_at` (`last_message_at` DESC),
    CONSTRAINT `fk_conversations_user1` FOREIGN KEY (`user1_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_conversations_user2` FOREIGN KEY (`user2_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_carts`
--

DROP TABLE IF EXISTS `course_carts`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_carts`
(
    `id`         bigint    NOT NULL AUTO_INCREMENT COMMENT '장바구니 항목 식별자',
    `student_id` bigint    NOT NULL COMMENT '학생 ID',
    `course_id`  bigint    NOT NULL COMMENT '강의 ID',
    `added_at`   timestamp NULL DEFAULT (now()) COMMENT '담은 일시',
    PRIMARY KEY (`id`),
    KEY `FKnmnrffvyhn7remumpagtag0pk` (`course_id`),
    KEY `FKg0yoxhgadbvy0geiumcxk9xxh` (`student_id`),
    CONSTRAINT `FKg0yoxhgadbvy0geiumcxk9xxh` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`),
    CONSTRAINT `FKnmnrffvyhn7remumpagtag0pk` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_grading_policies`
--

DROP TABLE IF EXISTS `course_grading_policies`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_grading_policies`
(
    `course_id`  bigint NOT NULL,
    `assignment` int    NOT NULL,
    `attendance` int    NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `final_exam` int    NOT NULL,
    `midterm`    int    NOT NULL,
    `quiz`       int    NOT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`course_id`),
    CONSTRAINT `FK4u95cm2hquco25qhhcbj0r0c8` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_notice_comments`
--

DROP TABLE IF EXISTS `course_notice_comments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_notice_comments`
(
    `id`         bigint                          NOT NULL AUTO_INCREMENT COMMENT '댓글 ID',
    `notice_id`  bigint                          NOT NULL COMMENT '공지사항 ID',
    `parent_id`  bigint                                   DEFAULT NULL COMMENT '부모 댓글 ID (대댓글용)',
    `content`    text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '댓글 내용',
    `author_id`  bigint                          NOT NULL COMMENT '작성자 ID',
    `created_by` bigint                                   DEFAULT NULL COMMENT '생성자 ID',
    `updated_by` bigint                                   DEFAULT NULL COMMENT '수정자 ID',
    `created_at` timestamp                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` timestamp                       NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    `deleted_at` timestamp                       NULL     DEFAULT NULL COMMENT '삭제일시',
    `is_deleted` tinyint(1)                      NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    PRIMARY KEY (`id`),
    KEY `idx_course_notice_comment_notice_id` (`notice_id`),
    KEY `idx_course_notice_comment_parent_id` (`parent_id`),
    KEY `idx_course_notice_comment_is_deleted` (`is_deleted`),
    KEY `idx_course_notice_comment_author_id` (`author_id`),
    CONSTRAINT `fk_course_notice_comment_notice` FOREIGN KEY (`notice_id`) REFERENCES `course_notices` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_course_notice_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `course_notice_comments` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_notices`
--

DROP TABLE IF EXISTS `course_notices`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_notices`
(
    `id`             bigint                                  NOT NULL AUTO_INCREMENT COMMENT '공지사항 ID',
    `course_id`      bigint                                  NOT NULL COMMENT '강의 ID',
    `title`          varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제목',
    `content`        text COLLATE utf8mb4_unicode_ci         NOT NULL COMMENT '내용',
    `allow_comments` tinyint(1)                              NOT NULL DEFAULT '1' COMMENT '댓글 허용 여부',
    `created_by`     bigint                                           DEFAULT NULL COMMENT '생성자 ID (교수)',
    `updated_by`     bigint                                           DEFAULT NULL COMMENT '수정자 ID',
    `created_at`     timestamp                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at`     timestamp                               NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    `deleted_at`     timestamp                               NULL     DEFAULT NULL COMMENT '삭제일시',
    `is_deleted`     tinyint(1)                              NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    PRIMARY KEY (`id`),
    KEY `idx_course_notice_course_id` (`course_id`),
    KEY `idx_course_notice_is_deleted` (`is_deleted`),
    KEY `idx_course_notice_created_at` (`created_at` DESC),
    CONSTRAINT `fk_course_notice_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_schedules`
--

DROP TABLE IF EXISTS `course_schedules`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_schedules`
(
    `schedule_id`   bigint      NOT NULL AUTO_INCREMENT COMMENT '시간표 식별자',
    `course_id`     bigint      NOT NULL COMMENT '강의 ID',
    `day_of_week`   int         NOT NULL COMMENT '요일 (1:월요일 ~ 5:금요일)',
    `start_time`    time        NOT NULL COMMENT '시작 시간 (예: 09:00)',
    `end_time`      time        NOT NULL COMMENT '종료 시간 (예: 10:30)',
    `schedule_room` varchar(50) NOT NULL COMMENT '강의실 (예: 공학관 401호)',
    PRIMARY KEY (`schedule_id`),
    KEY `FK1n9pcjyrxa70t5w3i11nnuglj` (`course_id`),
    CONSTRAINT `FK1n9pcjyrxa70t5w3i11nnuglj` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_status_types`
--

DROP TABLE IF EXISTS `course_status_types`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_status_types`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT,
    `status_code` varchar(20) NOT NULL COMMENT '상태 코드 (ONGOING/COMPLETED/PENDING)',
    `status_name` varchar(50) NOT NULL COMMENT '상태명 (진행중/완료/대기)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_tas`
--

DROP TABLE IF EXISTS `course_tas`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_tas`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `end_date`   date        NOT NULL,
    `is_active`  bit(1)      NOT NULL,
    `start_date` date        NOT NULL,
    `course_id`  bigint      NOT NULL,
    `ta_id`      bigint      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK836nwq95h6i96npham8ju4wno` (`course_id`),
    KEY `FKjboyfwc0ybtak91hqgn7r301t` (`ta_id`),
    CONSTRAINT `FK836nwq95h6i96npham8ju4wno` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
    CONSTRAINT `FKjboyfwc0ybtak91hqgn7r301t` FOREIGN KEY (`ta_id`) REFERENCES `students` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_types`
--

DROP TABLE IF EXISTS `course_types`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_types`
(
    `id`        bigint NOT NULL AUTO_INCREMENT,
    `type_code` int    NOT NULL COMMENT '유형 코드 (0: MAJOR_REQ, 1: MAJOR_ELEC, 2: GEN_REQ, 3: GEN_ELEC)',
    `category`  int    NOT NULL COMMENT '상위 분류 (0: 전공, 1: 교양)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `course_weeks`
--

DROP TABLE IF EXISTS `course_weeks`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_weeks`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '주차 식별자',
    `course_id`   bigint       NOT NULL COMMENT '강의 ID',
    `week_number` int          NOT NULL COMMENT '주차 번호 (1, 2, 3...)',
    `week_title`  varchar(200) NOT NULL COMMENT '주차 제목 (예: 1주차: 데이터베이스 개요)',
    `created_at`  timestamp    NULL DEFAULT (now()) COMMENT '생성 일시',
    `title`       varchar(200)      DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses`
(
    `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '강의 식별자',
    `subject_id`       bigint       NOT NULL COMMENT '과목 ID',
    `academic_term_id` bigint       NOT NULL COMMENT '학기 ID',
    `section_number`   varchar(255) NOT NULL,
    `current_students` int          NOT NULL COMMENT '현재 수강생 수',
    `professor_id`     bigint       NOT NULL COMMENT '담당 교수 ID',
    `max_students`     int          NOT NULL COMMENT '수강 정원',
    `created_at`       timestamp    NULL DEFAULT (now()) COMMENT '강의 개설 일시',
    `description`      text COMMENT '강의 설명 (분반별)',
    PRIMARY KEY (`id`),
    KEY `FKsj3b8l5dptx5ipcop6asg3sik` (`academic_term_id`),
    KEY `FKsj4okul9jc8m3p4tsnuobqjpb` (`professor_id`),
    KEY `FK5tckdihu5akp5nkxiacx1gfhi` (`subject_id`),
    CONSTRAINT `FK5tckdihu5akp5nkxiacx1gfhi` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
    CONSTRAINT `FKsj3b8l5dptx5ipcop6asg3sik` FOREIGN KEY (`academic_term_id`) REFERENCES `academic_terms` (`id`),
    CONSTRAINT `FKsj4okul9jc8m3p4tsnuobqjpb` FOREIGN KEY (`professor_id`) REFERENCES `professors` (`professor_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments`
(
    `id`              bigint                                  NOT NULL AUTO_INCREMENT,
    `college_id`      bigint                                  NOT NULL,
    `department_code` varchar(20) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `department_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at`      datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_departments_code` (`department_code`),
    KEY `idx_departments_department_code` (`department_code`),
    KEY `idx_departments_college_id` (`college_id`),
    CONSTRAINT `fk_departments_college` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `email_verifications`
--

DROP TABLE IF EXISTS `email_verifications`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_verifications`
(
    `id`                    bigint                                  NOT NULL AUTO_INCREMENT,
    `email`                 varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `verification_code`     varchar(5) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `is_verified`           bit(1)      DEFAULT NULL,
    `verification_attempts` int         DEFAULT NULL,
    `created_at`            datetime(6)                             NOT NULL,
    `expires_at`            datetime(6)                             NOT NULL,
    `verified_at`           datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_email_verifications_email` (`email`),
    KEY `idx_email_verifications_code` (`verification_code`),
    KEY `idx_email_verifications_expires_at` (`expires_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `enrollment_periods`
--

DROP TABLE IF EXISTS `enrollment_periods`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollment_periods`
(
    `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT '수강신청 기간 식별자',
    `term_id`        bigint      NOT NULL COMMENT '학기 ID',
    `period_name`    varchar(50) NOT NULL COMMENT '기간명 (예: 1차 수강신청, 정정기간)',
    `period_type_id` int         NOT NULL COMMENT '기간 타입 ID',
    `start_datetime` timestamp   NOT NULL COMMENT '시작 일시',
    `end_datetime`   timestamp   NOT NULL COMMENT '종료 일시',
    `target_year`    int              DEFAULT NULL COMMENT '대상 학년 (0이면 전체)',
    `created_at`     timestamp   NULL DEFAULT (now()) COMMENT '생성 일시',
    PRIMARY KEY (`id`),
    KEY `FK6uqjtvbd315stvyvgpr36sxr0` (`term_id`),
    KEY `FK7udi83frbrutpwr5derxqgr6s` (`period_type_id`),
    CONSTRAINT `FK6uqjtvbd315stvyvgpr36sxr0` FOREIGN KEY (`term_id`) REFERENCES `academic_terms` (`id`),
    CONSTRAINT `FK7udi83frbrutpwr5derxqgr6s` FOREIGN KEY (`period_type_id`) REFERENCES `period_types` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `enrollment_status_types`
--

DROP TABLE IF EXISTS `enrollment_status_types`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollment_status_types`
(
    `id`          int         NOT NULL AUTO_INCREMENT COMMENT '상태 식별자',
    `status_code` varchar(20) NOT NULL COMMENT '상태 코드 (ENROLLED/COMPLETED)',
    `status_name` varchar(50) NOT NULL COMMENT '상태명 (수강중/완강)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `enrollments`
--

DROP TABLE IF EXISTS `enrollments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollments`
(
    `id`          bigint    NOT NULL AUTO_INCREMENT COMMENT '수강신청 식별자',
    `course_id`   bigint    NOT NULL COMMENT '강의 ID',
    `student_id`  bigint    NOT NULL COMMENT '학생 ID',
    `enrolled_at` timestamp NULL DEFAULT (now()) COMMENT '수강신청 일시',
    PRIMARY KEY (`id`),
    KEY `FKho8mcicp4196ebpltdn9wl6co` (`course_id`),
    KEY `FK8kf1u1857xgo56xbfmnif2c51` (`student_id`),
    CONSTRAINT `FK8kf1u1857xgo56xbfmnif2c51` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`),
    CONSTRAINT `FKho8mcicp4196ebpltdn9wl6co` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exam_results`
--

DROP TABLE IF EXISTS `exam_results`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_results`
(
    `id`                  bigint        NOT NULL AUTO_INCREMENT,
    `exam_id`             bigint        NOT NULL COMMENT '시험 ID',
    `user_id`             bigint        NOT NULL COMMENT '응시자 ID',
    `started_at`          datetime(6)                           DEFAULT NULL COMMENT '시험 시작 시간',
    `submitted_at`        datetime(6)                           DEFAULT NULL COMMENT '제출 시간',
    `score`               decimal(5, 2)                         DEFAULT NULL COMMENT '획득 점수',
    `grade`               varchar(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '등급 (A+, A, B+ 등)',
    `answer_data`         text COLLATE utf8mb4_unicode_ci COMMENT '답안 데이터 (JSON)',
    `feedback`            text COLLATE utf8mb4_unicode_ci COMMENT '피드백',
    `graded_at`           datetime(6)                           DEFAULT NULL COMMENT '채점일시',
    `graded_by`           bigint                                DEFAULT NULL COMMENT '채점자 ID',
    `created_at`          datetime(6)   NOT NULL                DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`          datetime(6)                           DEFAULT NULL COMMENT '수정일시',
    `deleted_at`          datetime(6)                           DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    `is_late`             bit(1)        NOT NULL,
    `late_penalty_points` decimal(5, 2) NOT NULL,
    `late_penalty_rate`   decimal(5, 4) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_exam_user` (`exam_id`, `user_id`),
    UNIQUE KEY `UKjrbvkqe9apgcaey0a4veq5bg9` (`exam_id`, `user_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_submitted_at` (`submitted_at`),
    KEY `idx_score` (`score`),
    KEY `idx_grade` (`grade`),
    KEY `fk_exam_results_grader` (`graded_by`),
    CONSTRAINT `fk_exam_results_exam` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`),
    CONSTRAINT `fk_exam_results_grader` FOREIGN KEY (`graded_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_exam_results_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='시험 결과';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exams`
--

DROP TABLE IF EXISTS `exams`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exams`
(
    `id`               bigint                                 NOT NULL AUTO_INCREMENT,
    `post_id`          bigint                                 NOT NULL COMMENT '게시글 ID (제목/내용은 posts 테이블 참조)',
    `course_id`        bigint                                 NOT NULL COMMENT '강의 ID',
    `exam_type`        varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '시험 유형 (MIDTERM/FINAL/QUIZ/REGULAR)',
    `exam_date`        datetime(6)                            NOT NULL COMMENT '시험 일시',
    `duration_minutes` int                                    NOT NULL COMMENT '시험 시간 (분)',
    `total_score`      decimal(5, 2)                          NOT NULL COMMENT '총점',
    `is_online`        bit(1)                                 NOT NULL DEFAULT b'0' COMMENT '온라인 시험 여부',
    `location`         varchar(100) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '시험 장소',
    `instructions`     text COLLATE utf8mb4_unicode_ci COMMENT '시험 안내사항',
    `question_count`   int                                             DEFAULT NULL COMMENT '문제 수',
    `passing_score`    decimal(5, 2)                                   DEFAULT NULL COMMENT '합격 점수',
    `created_by`       bigint                                 NOT NULL COMMENT '생성자 ID',
    `created_at`       datetime(6)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`       datetime(6)                                     DEFAULT NULL COMMENT '수정일시',
    `deleted_at`       datetime(6)                                     DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    `question_data`    text COLLATE utf8mb4_unicode_ci,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exams_post_id` (`post_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_exam_date` (`exam_date`),
    KEY `idx_exam_type` (`exam_type`),
    KEY `idx_created_by` (`created_by`),
    CONSTRAINT `fk_exams_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_exams_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='시험';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fraud_record`
--

DROP TABLE IF EXISTS `fraud_record`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fraud_record`
(
    `id`            bigint                                                                                             NOT NULL AUTO_INCREMENT,
    `content_id`    bigint                                                                                             NOT NULL,
    `detail`        varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `detected_at`   datetime(6)                                                                                        NOT NULL,
    `end_seconds`   int                                     DEFAULT NULL,
    `fraud_type`    enum ('CONCURRENT_SESSION','FAST_PLAYBACK','SKIP_SEGMENT','TAB_HIDDEN') COLLATE utf8mb4_unicode_ci NOT NULL,
    `session_id`    bigint                                                                                             NOT NULL,
    `start_seconds` int                                     DEFAULT NULL,
    `user_id`       bigint                                                                                             NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_fraud_session` (`session_id`),
    KEY `idx_fraud_user_content` (`user_id`, `content_id`),
    KEY `idx_fraud_type` (`fraud_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `grades`
--

DROP TABLE IF EXISTS `grades`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grades`
(
    `id`               bigint                                                           NOT NULL AUTO_INCREMENT,
    `academic_term_id` bigint                                                           NOT NULL,
    `assignment_score` decimal(5, 2)                         DEFAULT NULL,
    `attendance_score` decimal(5, 2)                         DEFAULT NULL,
    `course_id`        bigint                                                           NOT NULL,
    `created_at`       datetime(6)                           DEFAULT NULL,
    `final_exam_score` decimal(5, 2)                         DEFAULT NULL,
    `final_grade`      varchar(2) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `final_score`      decimal(5, 2)                         DEFAULT NULL,
    `graded_at`        datetime(6)                           DEFAULT NULL,
    `midterm_score`    decimal(5, 2)                         DEFAULT NULL,
    `published_at`     datetime(6)                           DEFAULT NULL,
    `quiz_score`       decimal(5, 2)                         DEFAULT NULL,
    `status`           enum ('GRADED','PENDING','PUBLISHED') COLLATE utf8mb4_unicode_ci NOT NULL,
    `student_id`       bigint                                                           NOT NULL,
    `updated_at`       datetime(6)                           DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_grades_course_student` (`course_id`, `student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hashtags`
--

DROP TABLE IF EXISTS `hashtags`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hashtags`
(
    `id`           bigint                                 NOT NULL AUTO_INCREMENT,
    `name`         varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '태그 이름 (#제외, 소문자)',
    `display_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '화면 표시용 태그명',
    `description`  varchar(255) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '태그 설명',
    `color`        varchar(7) COLLATE utf8mb4_unicode_ci           DEFAULT '#007bff' COMMENT '태그 색상 (HEX)',
    `tag_category` varchar(30) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '태그 카테고리 (SUBJECT/DIFFICULTY/TYPE 등)',
    `is_active`    bit(1)                                          DEFAULT b'1' COMMENT '활성화 상태',
    `created_by`   bigint                                          DEFAULT NULL COMMENT '태그 생성자 ID',
    `created_at`   datetime(6)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`   datetime(6)                                     DEFAULT NULL COMMENT '수정일시',
    `updated_by`   bigint                                          DEFAULT NULL COMMENT '수정자 ID',
    `deleted_at`   datetime(6)                                     DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    `is_deleted`   bit(1)                                          DEFAULT b'0' COMMENT '삭제 여부',
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`),
    KEY `idx_name` (`name`),
    KEY `idx_tag_category` (`tag_category`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_updated_by` (`updated_by`),
    KEY `fk_hashtags_creator` (`created_by`),
    CONSTRAINT `fk_hashtags_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_hashtags_updater` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='해시태그';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages`
(
    `id`                  bigint                          NOT NULL AUTO_INCREMENT,
    `conversation_id`     bigint                          NOT NULL,
    `sender_id`           bigint                          NOT NULL,
    `content`             text COLLATE utf8mb4_unicode_ci NOT NULL,
    `read_at`             timestamp                       NULL     DEFAULT NULL,
    `deleted_by_sender`   tinyint(1)                      NOT NULL DEFAULT '0',
    `deleted_by_receiver` tinyint(1)                      NOT NULL DEFAULT '0',
    `created_at`          timestamp                       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_messages_conversation_created` (`conversation_id`, `created_at` DESC),
    KEY `idx_messages_sender` (`sender_id`),
    CONSTRAINT `fk_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification_batches`
--

DROP TABLE IF EXISTS `notification_batches`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_batches`
(
    `id`               bigint                                                                        NOT NULL AUTO_INCREMENT,
    `sender_id`        bigint                                                                                 DEFAULT NULL,
    `type_id`          int                                                                           NOT NULL,
    `title`            varchar(200) COLLATE utf8mb4_unicode_ci                                                DEFAULT NULL,
    `message`          text COLLATE utf8mb4_unicode_ci                                               NOT NULL,
    `status`           enum ('COMPLETED','FAILED','PENDING','PROCESSING') COLLATE utf8mb4_unicode_ci NOT NULL,
    `total_recipients` int                                                                           NOT NULL DEFAULT '0',
    `course_id`        bigint                                                                                 DEFAULT NULL,
    `error_message`    varchar(500) COLLATE utf8mb4_unicode_ci                                                DEFAULT NULL,
    `created_at`       datetime(6)                                                                   NOT NULL,
    `processed_at`     datetime(6)                                                                            DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_notification_batches_course_id` (`course_id`),
    KEY `idx_notification_batches_status` (`status`),
    KEY `idx_notification_batches_created_at` (`created_at`),
    KEY `fk_notification_batch_sender` (`sender_id`),
    KEY `fk_notification_batch_type` (`type_id`),
    CONSTRAINT `fk_notification_batch_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_notification_batch_type` FOREIGN KEY (`type_id`) REFERENCES `notification_types` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification_preferences`
--

DROP TABLE IF EXISTS `notification_preferences`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_preferences`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT,
    `user_id`       bigint      NOT NULL,
    `type_id`       int         NOT NULL,
    `is_enabled`    bit(1)      NOT NULL DEFAULT b'1',
    `email_enabled` bit(1)      NOT NULL DEFAULT b'0',
    `created_at`    datetime(6) NOT NULL,
    `updated_at`    datetime(6)          DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_type_pref` (`user_id`, `type_id`),
    KEY `idx_notification_preferences_user_id` (`user_id`),
    KEY `fk_notification_pref_type` (`type_id`),
    CONSTRAINT `fk_notification_pref_type` FOREIGN KEY (`type_id`) REFERENCES `notification_types` (`id`),
    CONSTRAINT `fk_notification_pref_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification_types`
--

DROP TABLE IF EXISTS `notification_types`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_types`
(
    `id`                       int                                     NOT NULL AUTO_INCREMENT,
    `type_code`                varchar(50) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `type_name`                varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `category`                 varchar(30) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `default_message_template` text COLLATE utf8mb4_unicode_ci         NOT NULL,
    `is_active`                bit(1)                                  NOT NULL DEFAULT b'1',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notification_types_code` (`type_code`),
    KEY `idx_notification_types_type_code` (`type_code`),
    KEY `idx_notification_types_category` (`category`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications`
(
    `id`                  bigint                          NOT NULL AUTO_INCREMENT,
    `recipient_id`        bigint                          NOT NULL,
    `sender_id`           bigint                                   DEFAULT NULL,
    `type_id`             int                             NOT NULL,
    `title`               varchar(200) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `message`             text COLLATE utf8mb4_unicode_ci NOT NULL,
    `is_read`             bit(1)                          NOT NULL DEFAULT b'0',
    `read_at`             datetime(6)                              DEFAULT NULL,
    `course_id`           bigint                                   DEFAULT NULL,
    `related_entity_type` varchar(50) COLLATE utf8mb4_unicode_ci   DEFAULT NULL,
    `related_entity_id`   bigint                                   DEFAULT NULL,
    `action_url`          varchar(500) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `created_at`          datetime(6)                     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_recipient_unread` (`recipient_id`, `is_read`, `created_at`),
    KEY `idx_recipient_created` (`recipient_id`, `created_at`),
    KEY `idx_course_created` (`course_id`, `created_at`),
    KEY `idx_notifications_type_id` (`type_id`),
    KEY `fk_notifications_sender` (`sender_id`),
    CONSTRAINT `fk_notifications_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_notifications_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_notifications_type` FOREIGN KEY (`type_id`) REFERENCES `notification_types` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `period_types`
--

DROP TABLE IF EXISTS `period_types`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `period_types`
(
    `id`          int         NOT NULL AUTO_INCREMENT COMMENT '기간 타입 식별자',
    `type_code`   varchar(20) NOT NULL COMMENT '타입 코드 (ENROLLMENT, COURSE_REGISTRATION, ADJUSTMENT, CANCELLATION)',
    `type_name`   varchar(50) NOT NULL COMMENT '타입명 (수강신청, 강의등록, 정정, 수강철회)',
    `description` varchar(200) DEFAULT NULL COMMENT '설명',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_admin_settings`
--

DROP TABLE IF EXISTS `post_admin_settings`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_admin_settings`
(
    `id`         bigint                                 NOT NULL AUTO_INCREMENT,
    `post_id`    bigint                                 NOT NULL COMMENT '게시글 ID',
    `status`     varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '상태 (ACTIVE/CLOSED/ARCHIVED)',
    `priority`   varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NORMAL' COMMENT '우선순위 (HIGH/NORMAL/LOW)',
    `is_notice`  bit(1)                                 NOT NULL DEFAULT b'0' COMMENT '공지사항 여부',
    `is_pinned`  bit(1)                                 NOT NULL DEFAULT b'0' COMMENT '상단 고정 여부',
    `managed_by` bigint                                          DEFAULT NULL COMMENT '최종 관리자 ID',
    `managed_at` datetime(6)                                     DEFAULT NULL COMMENT '최종 관리 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `post_id` (`post_id`),
    UNIQUE KEY `uk_post_admin_settings_post_id` (`post_id`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_is_notice` (`is_notice`),
    KEY `idx_is_pinned` (`is_pinned`),
    KEY `idx_managed_by` (`managed_by`),
    CONSTRAINT `fk_post_admin_settings_manager` FOREIGN KEY (`managed_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_post_admin_settings_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='관리자 전용 게시글 설정';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_bookmarks`
--

DROP TABLE IF EXISTS `post_bookmarks`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_bookmarks`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT,
    `user_id`           bigint      NOT NULL COMMENT '사용자 ID',
    `post_id`           bigint      NOT NULL COMMENT '게시글 ID',
    `bookmark_category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '북마크 카테고리',
    `notes`             text COLLATE utf8mb4_unicode_ci COMMENT '개인 메모',
    `created_at`        datetime(6) NOT NULL                   DEFAULT CURRENT_TIMESTAMP(6) COMMENT '북마크 생성일시',
    `updated_at`        datetime(6)                            DEFAULT NULL COMMENT '수정일시',
    `deleted_at`        datetime(6)                            DEFAULT NULL COMMENT '북마크 삭제일시 (Soft Delete)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_post_bookmark` (`user_id`, `post_id`),
    KEY `idx_user_category` (`user_id`, `bookmark_category`),
    KEY `fk_post_bookmarks_post` (`post_id`),
    CONSTRAINT `fk_post_bookmarks_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_post_bookmarks_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='북마크';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_hashtags`
--

DROP TABLE IF EXISTS `post_hashtags`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_hashtags`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT,
    `post_id`    bigint      NOT NULL COMMENT '게시글 ID',
    `hashtag_id` bigint      NOT NULL COMMENT '해시태그 ID',
    `created_by` bigint      NOT NULL COMMENT '태그 추가자 ID',
    `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '연결 생성일시',
    `updated_at` datetime(6)          DEFAULT NULL COMMENT '수정일시',
    `updated_by` bigint               DEFAULT NULL COMMENT '수정자 ID',
    `deleted_at` datetime(6)          DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    `is_deleted` bit(1)               DEFAULT b'0' COMMENT '삭제 여부',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_post_hashtag` (`post_id`, `hashtag_id`),
    KEY `idx_hashtag_id` (`hashtag_id`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_updated_by` (`updated_by`),
    CONSTRAINT `fk_post_hashtags_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_post_hashtags_hashtag` FOREIGN KEY (`hashtag_id`) REFERENCES `hashtags` (`id`),
    CONSTRAINT `fk_post_hashtags_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_post_hashtags_updater` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='게시글-해시태그 연결';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_likes`
--

DROP TABLE IF EXISTS `post_likes`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_likes`
(
    `id`         bigint                                 NOT NULL AUTO_INCREMENT,
    `user_id`    bigint                                 NOT NULL COMMENT '사용자 ID',
    `post_id`    bigint                                          DEFAULT NULL COMMENT '게시글 ID',
    `comment_id` bigint                                          DEFAULT NULL COMMENT '댓글 ID',
    `like_type`  varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '좋아요 유형 (POST/COMMENT)',
    `created_at` datetime(6)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '좋아요 생성일시',
    `deleted_at` datetime(6)                                     DEFAULT NULL COMMENT '좋아요 취소일시 (Soft Delete)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_post_like` (`user_id`, `post_id`),
    UNIQUE KEY `idx_user_comment_like` (`user_id`, `comment_id`),
    UNIQUE KEY `uk_user_post_like` (`user_id`, `post_id`),
    UNIQUE KEY `uk_user_comment_like` (`user_id`, `comment_id`),
    KEY `idx_like_type` (`like_type`),
    KEY `idx_post_likes_user_id` (`user_id`),
    KEY `idx_post_likes_post_id` (`post_id`),
    KEY `idx_post_likes_comment_id` (`comment_id`),
    KEY `idx_post_likes_type` (`like_type`),
    CONSTRAINT `fk_post_likes_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`),
    CONSTRAINT `fk_post_likes_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_post_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='좋아요';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post_views`
--

DROP TABLE IF EXISTS `post_views`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_views`
(
    `id`         bigint      NOT NULL AUTO_INCREMENT,
    `post_id`    bigint      NOT NULL COMMENT '게시글 ID',
    `user_id`    bigint                                 DEFAULT NULL COMMENT '사용자 ID (비로그인 시 null)',
    `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP 주소 (IPv6 지원)',
    `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT '브라우저 정보',
    `viewed_at`  datetime(6) NOT NULL                   DEFAULT CURRENT_TIMESTAMP(6) COMMENT '조회일시',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_viewed_at` (`viewed_at`),
    CONSTRAINT `fk_post_views_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
    CONSTRAINT `fk_post_views_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='조회수 추적';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `posts`
--

DROP TABLE IF EXISTS `posts`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `posts`
(
    `id`            bigint                                  NOT NULL AUTO_INCREMENT,
    `category_id`   bigint                                  NOT NULL COMMENT '카테고리 ID',
    `course_id`     bigint                                           DEFAULT NULL COMMENT '강의 ID (질문 게시판용, 선택사항)',
    `department_id` bigint                                           DEFAULT NULL COMMENT '학과 ID (학과 게시판용, 선택사항)',
    `author_id`     bigint                                  NOT NULL COMMENT '작성자 ID',
    `title`         varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '제목',
    `content`       text COLLATE utf8mb4_unicode_ci         NOT NULL COMMENT '내용',
    `post_type`     varchar(30) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '게시글 유형 (NOTICE/GENERAL/QUESTION/DISCUSSION/PROFESSOR/STUDENT/DEPARTMENT/CONTEST/CAREER/ASSIGNMENT/EXAM/QUIZ/STUDY_RECRUITMENT)',
    `is_anonymous`  bit(1)                                           DEFAULT b'0' COMMENT '익명 게시글 여부',
    `view_count`    int                                              DEFAULT '0' COMMENT '조회수',
    `like_count`    int                                              DEFAULT '0' COMMENT '좋아요 수',
    `is_deleted`    bit(1)                                           DEFAULT b'0' COMMENT '삭제 여부 (성능 최적화용)',
    `created_by`    bigint                                  NOT NULL COMMENT '생성자 ID',
    `updated_by`    bigint                                           DEFAULT NULL COMMENT '수정자 ID',
    `created_at`    datetime(6)                             NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    `updated_at`    datetime(6)                                      DEFAULT NULL COMMENT '수정일시',
    `deleted_at`    datetime(6)                                      DEFAULT NULL COMMENT '삭제일시 (Soft Delete)',
    PRIMARY KEY (`id`),
    KEY `idx_category_active_created` (`category_id`, `is_deleted`, `created_at`),
    KEY `idx_course_active_created` (`course_id`, `is_deleted`, `created_at`),
    KEY `idx_department_active_created` (`department_id`, `is_deleted`, `created_at`),
    KEY `idx_author_created` (`author_id`, `created_at`),
    KEY `idx_post_type` (`post_type`),
    KEY `idx_is_deleted` (`is_deleted`),
    KEY `idx_created_by` (`created_by`),
    KEY `fk_posts_updated_by` (`updated_by`),
    CONSTRAINT `fk_posts_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_posts_category` FOREIGN KEY (`category_id`) REFERENCES `board_categories` (`id`),
    CONSTRAINT `fk_posts_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_posts_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='게시글';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `professor_departments`
--

DROP TABLE IF EXISTS `professor_departments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professor_departments`
(
    `id`            bigint NOT NULL AUTO_INCREMENT,
    `professor_id`  bigint NOT NULL,
    `department_id` bigint NOT NULL,
    `is_primary`    bit(1) DEFAULT NULL,
    `start_date`    date   NOT NULL,
    `end_date`      date   DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_professor_department` (`professor_id`, `department_id`, `start_date`),
    UNIQUE KEY `uk_professor_primary` (`professor_id`),
    UNIQUE KEY `UKoxn5o9nkukj95xikfwijlwqrc` (`professor_id`, `department_id`, `start_date`),
    KEY `idx_professor_departments_professor_id` (`professor_id`),
    KEY `idx_professor_departments_department_id` (`department_id`),
    CONSTRAINT `fk_professor_dept_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
    CONSTRAINT `fk_professor_dept_professor` FOREIGN KEY (`professor_id`) REFERENCES `professors` (`professor_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `professors`
--

DROP TABLE IF EXISTS `professors`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professors`
(
    `professor_id`     bigint NOT NULL,
    `appointment_date` date   NOT NULL,
    `created_at`       datetime(6) DEFAULT NULL,
    PRIMARY KEY (`professor_id`),
    KEY `idx_professors_professor_id` (`professor_id`),
    CONSTRAINT `fk_professors_user` FOREIGN KEY (`professor_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens`
(
    `id`           bigint                                  NOT NULL AUTO_INCREMENT,
    `user_id`      bigint                                  NOT NULL,
    `token`        varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
    `device_info`  varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `ip_address`   varchar(45) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `is_revoked`   bit(1)                                  DEFAULT NULL,
    `created_at`   datetime(6)                             NOT NULL,
    `expires_at`   datetime(6)                             NOT NULL,
    `last_used_at` datetime(6)                             DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_tokens_token` (`token`),
    KEY `idx_refresh_tokens_token` (`token`),
    KEY `idx_refresh_tokens_user_id` (`user_id`),
    KEY `idx_refresh_tokens_expires_at` (`expires_at`),
    CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `student_content_progress`
--

DROP TABLE IF EXISTS `student_content_progress`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_content_progress`
(
    `id`                    bigint NOT NULL AUTO_INCREMENT,
    `access_count`          int    NOT NULL,
    `completed_at`          datetime(6) DEFAULT NULL,
    `first_accessed_at`     datetime(6) DEFAULT NULL,
    `is_completed`          bit(1) NOT NULL,
    `last_accessed_at`      datetime(6) DEFAULT NULL,
    `last_position_seconds` int         DEFAULT NULL,
    `progress_percentage`   int    NOT NULL,
    `content_id`            bigint NOT NULL,
    `student_id`            bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKn15ncg7vcl6jfyddvqp2snkjd` (`content_id`, `student_id`),
    KEY `idx_progress_content` (`content_id`),
    KEY `idx_progress_student` (`student_id`),
    KEY `idx_progress_completed` (`is_completed`),
    CONSTRAINT `FK8ike8vlibqsu38h0ngm5vly91` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`),
    CONSTRAINT `FKsvx0qh2684cdjel23unoayv9e` FOREIGN KEY (`content_id`) REFERENCES `week_contents` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `student_departments`
--

DROP TABLE IF EXISTS `student_departments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_departments`
(
    `id`            bigint NOT NULL AUTO_INCREMENT,
    `student_id`    bigint NOT NULL,
    `department_id` bigint NOT NULL,
    `is_primary`    bit(1) DEFAULT NULL,
    `enrolled_date` date   NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_department` (`student_id`, `department_id`),
    UNIQUE KEY `uk_student_primary` (`student_id`),
    UNIQUE KEY `UKix5cx6or3fdlc16blgo9bfynm` (`student_id`, `department_id`),
    KEY `idx_student_departments_student_id` (`student_id`),
    KEY `idx_student_departments_department_id` (`department_id`),
    CONSTRAINT `fk_student_dept_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
    CONSTRAINT `fk_student_dept_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `student_number_sequences`
--

DROP TABLE IF EXISTS `student_number_sequences`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_number_sequences`
(
    `id`            bigint NOT NULL AUTO_INCREMENT,
    `year`          int    NOT NULL,
    `college_id`    bigint NOT NULL,
    `department_id` bigint NOT NULL,
    `last_sequence` int    NOT NULL,
    `version`       bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_seq` (`year`, `college_id`, `department_id`),
    UNIQUE KEY `UK2jyirwg2350esr1b5o50hxsl7` (`year`, `college_id`, `department_id`),
    KEY `idx_student_seq_year_college_dept` (`year`, `college_id`, `department_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students`
(
    `student_id`     bigint NOT NULL,
    `admission_year` int    NOT NULL,
    `grade`          int    NOT NULL,
    `created_at`     datetime(6) DEFAULT NULL,
    PRIMARY KEY (`student_id`),
    KEY `idx_students_student_id` (`student_id`),
    KEY `idx_students_admission_year` (`admission_year`),
    CONSTRAINT `fk_students_user` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_applications`
--

DROP TABLE IF EXISTS `study_applications`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_applications`
(
    `id`                   bigint                                 NOT NULL AUTO_INCREMENT,
    `study_recruitment_id` bigint                                 NOT NULL COMMENT '스터디모집 ID',
    `applicant_id`         bigint                                 NOT NULL COMMENT '지원자 ID',
    `application_message`  text COLLATE utf8mb4_unicode_ci COMMENT '지원 메시지',
    `status`               varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '지원 상태 (PENDING/APPROVED/REJECTED/WITHDRAWN)',
    `applied_at`           datetime(6)                            NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '지원일시',
    `processed_at`         datetime(6)                                     DEFAULT NULL COMMENT '처리일시 (승인/거절)',
    `process_message`      text COLLATE utf8mb4_unicode_ci COMMENT '처리 메시지 (거절 사유 등)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_study_applicant` (`study_recruitment_id`, `applicant_id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_applied_at` (`applied_at`),
    CONSTRAINT `fk_study_applications_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_study_applications_recruitment` FOREIGN KEY (`study_recruitment_id`) REFERENCES `study_recruitments` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='스터디 지원';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_recruitments`
--

DROP TABLE IF EXISTS `study_recruitments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_recruitments`
(
    `id`                   bigint                                 NOT NULL AUTO_INCREMENT,
    `post_id`              bigint                                 NOT NULL COMMENT '게시글 ID (제목/내용은 posts 테이블 참조)',
    `study_type`           varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '스터디 유형 (EXAM_PREP/PROJECT/LANGUAGE/CERTIFICATION/READING/CODING/OTHER)',
    `max_participants`     int                                    NOT NULL COMMENT '최대 참여 인원',
    `recruitment_status`   varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECRUITING' COMMENT '모집 상태 (RECRUITING/COMPLETED/CANCELLED/IN_PROGRESS/FINISHED)',
    `recruitment_deadline` datetime(6)                                     DEFAULT NULL COMMENT '모집 마감일',
    `requirements`         text COLLATE utf8mb4_unicode_ci COMMENT '지원 자격/조건',
    PRIMARY KEY (`id`),
    UNIQUE KEY `post_id` (`post_id`),
    UNIQUE KEY `uk_study_recruitments_post_id` (`post_id`),
    KEY `idx_study_type` (`study_type`),
    KEY `idx_recruitment_status` (`recruitment_status`),
    KEY `idx_recruitment_deadline` (`recruitment_deadline`),
    CONSTRAINT `fk_study_recruitments_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='스터디모집';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subject_prerequisites`
--

DROP TABLE IF EXISTS `subject_prerequisites`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject_prerequisites`
(
    `id`              bigint    NOT NULL AUTO_INCREMENT COMMENT '관계 식별자',
    `subject_id`      bigint    NOT NULL COMMENT '과목 ID',
    `prerequisite_id` bigint    NOT NULL COMMENT '선수과목 ID',
    `is_mandatory`    tinyint(1)     DEFAULT '1' COMMENT '필수 이수 여부',
    `created_at`      timestamp NULL DEFAULT (now()) COMMENT '생성 일시',
    PRIMARY KEY (`id`),
    KEY `FK8vcc3x4jk5nyevql4tbg04rlx` (`prerequisite_id`),
    KEY `FKi2pk0tltinduehdgkvotwwa2b` (`subject_id`),
    CONSTRAINT `FK8vcc3x4jk5nyevql4tbg04rlx` FOREIGN KEY (`prerequisite_id`) REFERENCES `subjects` (`id`),
    CONSTRAINT `FKi2pk0tltinduehdgkvotwwa2b` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subjects`
(
    `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT '과목 식별자',
    `subject_code`        varchar(8)   NOT NULL COMMENT '과목 코드 (예: CS101)',
    `subject_name`        varchar(20)  NOT NULL,
    `department_id`       bigint       NOT NULL COMMENT '개설 학과 ID',
    `course_type_id`      bigint       NOT NULL,
    `credits`             int          NOT NULL COMMENT '학점 수 (1~4)',
    `theory_hours`        int               DEFAULT '0' COMMENT '이론 시수',
    `practice_hours`      int               DEFAULT '0' COMMENT '실습 시수',
    `subject_description` varchar(200) NOT NULL COMMENT '과목 개요',
    `description`         text COMMENT '과목 상세 설명',
    `created_at`          timestamp    NULL DEFAULT (now()) COMMENT '과목 등록 일시',
    PRIMARY KEY (`id`),
    KEY `FKdygtibnr0axa8tp264fbgl61p` (`course_type_id`),
    KEY `FKgh0j5ejuox2kr2av0l8158c0a` (`department_id`),
    CONSTRAINT `FKdygtibnr0axa8tp264fbgl61p` FOREIGN KEY (`course_type_id`) REFERENCES `course_types` (`id`),
    CONSTRAINT `FKgh0j5ejuox2kr2av0l8158c0a` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `submission_attachments`
--

DROP TABLE IF EXISTS `submission_attachments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submission_attachments`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT,
    `submission_id` bigint      NOT NULL COMMENT '과제 제출 ID',
    `attachment_id` bigint      NOT NULL COMMENT '첨부파일 ID',
    `created_at`    datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_submission_attachment` (`submission_id`, `attachment_id`),
    KEY `idx_submission_id` (`submission_id`),
    KEY `idx_attachment_id` (`attachment_id`),
    CONSTRAINT `fk_submission_attachments_attachment` FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_submission_attachments_submission` FOREIGN KEY (`submission_id`) REFERENCES `assignment_submissions` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='과제 제출 첨부파일';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_primary_contacts`
--

DROP TABLE IF EXISTS `user_primary_contacts`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_primary_contacts`
(
    `user_id`         bigint NOT NULL,
    `mobile_number`   varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `mobile_verified` bit(1)                                 DEFAULT NULL,
    `home_number`     varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `office_number`   varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `created_at`      datetime(6)                            DEFAULT NULL,
    `updated_at`      datetime(6)                            DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_primary_contact_mobile` (`mobile_number`),
    UNIQUE KEY `UKs95n9je6vatqq50v1wt5jxx8o` (`mobile_number`),
    KEY `idx_primary_contact_mobile` (`mobile_number`),
    KEY `idx_primary_contact_verified` (`mobile_verified`),
    CONSTRAINT `fk_primary_contacts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_profile_images`
--

DROP TABLE IF EXISTS `user_profile_images`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profile_images`
(
    `user_id`       bigint                                  NOT NULL,
    `image_url`     varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `thumbnail_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `created_at`    datetime(6)                             DEFAULT NULL,
    `updated_at`    datetime(6)                             DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    KEY `idx_profile_image_created` (`created_at`),
    CONSTRAINT `fk_profile_images_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_profiles`
--

DROP TABLE IF EXISTS `user_profiles`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profiles`
(
    `user_id`    bigint                                 NOT NULL,
    `name`       varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    KEY `idx_user_profiles_name_user_id` (`name`, `user_id`),
    CONSTRAINT `fk_user_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_status_history`
--

DROP TABLE IF EXISTS `user_status_history`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_status_history`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `user_id`    bigint NOT NULL,
    `status_id`  int    NOT NULL,
    `changed_by` bigint      DEFAULT NULL,
    `changed_at` datetime(6) DEFAULT NULL,
    `reason`     text COLLATE utf8mb4_unicode_ci,
    PRIMARY KEY (`id`),
    KEY `idx_user_status_history_user_id` (`user_id`),
    KEY `idx_user_status_history_status_id` (`status_id`),
    KEY `idx_user_status_history_changed_at` (`changed_at`),
    KEY `fk_status_history_changed_by` (`changed_by`),
    CONSTRAINT `fk_status_history_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_status_history_status` FOREIGN KEY (`status_id`) REFERENCES `user_status_types` (`id`),
    CONSTRAINT `fk_status_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_status_types`
--

DROP TABLE IF EXISTS `user_status_types`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_status_types`
(
    `id`          int                                    NOT NULL AUTO_INCREMENT,
    `status_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    `status_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_status_types_code` (`status_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_type_mappings`
--

DROP TABLE IF EXISTS `user_type_mappings`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_type_mappings`
(
    `user_id`      bigint NOT NULL,
    `user_type_id` int    NOT NULL,
    `assigned_at`  datetime(6) DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    KEY `idx_user_type_mappings_user_type_id` (`user_type_id`),
    CONSTRAINT `fk_user_type_mappings_type` FOREIGN KEY (`user_type_id`) REFERENCES `user_types` (`id`),
    CONSTRAINT `fk_user_type_mappings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_types`
--

DROP TABLE IF EXISTS `user_types`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_types`
(
    `id`        int                                    NOT NULL AUTO_INCREMENT,
    `type_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    `type_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_types_code` (`type_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users`
(
    `id`         bigint                                  NOT NULL,
    `email`      varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
    `password`   varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at` datetime(6)                             NOT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `deleted_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`),
    KEY `idx_users_email` (`email`),
    KEY `idx_users_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `video_upload`
--

DROP TABLE IF EXISTS `video_upload`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video_upload`
(
    `id`                bigint                                                                                     NOT NULL AUTO_INCREMENT,
    `completed_at`      datetime(6)                             DEFAULT NULL,
    `content_id`        bigint                                  DEFAULT NULL,
    `content_type`      varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `created_at`        datetime(6)                                                                                NOT NULL,
    `duration`          varchar(10) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `file_size`         bigint                                                                                     NOT NULL,
    `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci                                                    NOT NULL,
    `status`            enum ('CANCELLED','COMPLETED','EXPIRED','FAILED','IN_PROGRESS') COLLATE utf8mb4_unicode_ci NOT NULL,
    `storage_path`      varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `title`             varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `tus_upload_id`     varchar(255) COLLATE utf8mb4_unicode_ci                                                    NOT NULL,
    `updated_at`        datetime(6)                             DEFAULT NULL,
    `uploaded_bytes`    bigint                                                                                     NOT NULL,
    `user_id`           bigint                                                                                     NOT NULL,
    `week_id`           bigint                                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKdd2ovfrur63mlso6hqtf79vp5` (`tus_upload_id`),
    KEY `idx_upload_user` (`user_id`),
    KEY `idx_upload_status` (`status`),
    KEY `idx_upload_tus_id` (`tus_upload_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watch_event`
--

DROP TABLE IF EXISTS `watch_event`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `watch_event`
(
    `id`                    bigint                                                                                                         NOT NULL AUTO_INCREMENT,
    `content_id`            bigint                                                                                                         NOT NULL,
    `created_at`            datetime(6)                                                                                                    NOT NULL,
    `event_timestamp`       datetime(6)                                                                                                    NOT NULL,
    `event_type`            enum ('PAUSE','PLAY','RATE_CHANGE','SEEK','VISIBILITY_HIDDEN','VISIBILITY_VISIBLE') COLLATE utf8mb4_unicode_ci NOT NULL,
    `from_position_seconds` int    DEFAULT NULL,
    `playback_rate`         double DEFAULT NULL,
    `position_seconds`      int    DEFAULT NULL,
    `session_id`            bigint                                                                                                         NOT NULL,
    `to_position_seconds`   int    DEFAULT NULL,
    `user_id`               bigint                                                                                                         NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_watch_event_session` (`session_id`),
    KEY `idx_watch_event_user` (`user_id`),
    KEY `idx_watch_event_content` (`content_id`),
    KEY `idx_watch_event_type` (`event_type`),
    KEY `idx_watch_event_timestamp` (`event_timestamp`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `watched_segment`
--

DROP TABLE IF EXISTS `watched_segment`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `watched_segment`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT,
    `content_id`    bigint      NOT NULL,
    `created_at`    datetime(6) NOT NULL,
    `end_seconds`   int         NOT NULL,
    `is_valid`      bit(1)      NOT NULL,
    `playback_rate` double DEFAULT NULL,
    `session_id`    bigint      NOT NULL,
    `start_seconds` int         NOT NULL,
    `user_id`       bigint      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_segment_session` (`session_id`),
    KEY `idx_segment_user_content` (`user_id`, `content_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `week_attendance`
--

DROP TABLE IF EXISTS `week_attendance`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `week_attendance`
(
    `id`                    bigint     NOT NULL AUTO_INCREMENT COMMENT '출석 식별자',
    `student_id`            bigint     NOT NULL COMMENT '학생 ID',
    `week_id`               bigint     NOT NULL COMMENT '주차 ID',
    `course_id`             bigint     NOT NULL COMMENT '강의 ID',
    `is_completed`          tinyint(1) NOT NULL DEFAULT '0' COMMENT '출석 완료 여부',
    `completed_video_count` int        NOT NULL DEFAULT '0' COMMENT '완료한 VIDEO 수',
    `total_video_count`     int        NOT NULL COMMENT '전체 VIDEO 수 (완료 시점 잠금용)',
    `first_accessed_at`     timestamp  NULL     DEFAULT NULL COMMENT '최초 접근 일시',
    `completed_at`          timestamp  NULL     DEFAULT NULL COMMENT '출석 완료 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_week_attendance_student_week` (`student_id`, `week_id`),
    UNIQUE KEY `UKav0120anqfx7u7xyks13sf9s2` (`student_id`, `week_id`),
    KEY `idx_week_attendance_student` (`student_id`),
    KEY `idx_week_attendance_course` (`course_id`),
    KEY `idx_week_attendance_week` (`week_id`),
    KEY `idx_week_attendance_completed` (`is_completed`),
    CONSTRAINT `FKg4dasydkip0qqctdcbsgbe8b4` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
    CONSTRAINT `FKk4hh4eo46rk14l3oal46uif3q` FOREIGN KEY (`week_id`) REFERENCES `course_weeks` (`id`),
    CONSTRAINT `FKmxmetgc06cpfw1fqm9ebyut5c` FOREIGN KEY (`student_id`) REFERENCES `students` (`student_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `week_contents`
--

DROP TABLE IF EXISTS `week_contents`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `week_contents`
(
    `id`            bigint                                  NOT NULL AUTO_INCREMENT,
    `content_type`  varchar(20) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `content_url`   varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
    `created_at`    datetime(6)                             NOT NULL,
    `display_order` int                                     NOT NULL,
    `duration`      varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `title`         varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
    `week_id`       bigint                                  NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_week_contents_week` (`week_id`),
    KEY `idx_week_contents_order` (`display_order`),
    CONSTRAINT `FKca3p784b2hsp33tsvopvpx5io` FOREIGN KEY (`week_id`) REFERENCES `course_weeks` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'lms_db'
--
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2025-12-23  3:07:16
