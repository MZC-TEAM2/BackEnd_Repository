# LMS 데이터베이스 테이블 명세서

## 목차

- [1. 기본 코드 테이블](#1-기본-코드-테이블)
- [2. 조직 테이블](#2-조직-테이블)
- [3. 사용자 테이블](#3-사용자-테이블)
- [4. 학생 테이블](#4-학생-테이블)
- [5. 교수 테이블](#5-교수-테이블)
- [6. 인증 테이블](#6-인증-테이블)
- [7. 알림 테이블](#7-알림-테이블)
- [8. 학사 테이블](#8-학사-테이블)
- [9. 강의 테이블](#9-강의-테이블)
- [10. 수강 테이블](#10-수강-테이블)
- [11. 게시판 테이블](#11-게시판-테이블)
- [12. 과제/시험 테이블](#12-과제시험-테이블)
- [13. 스터디 테이블](#13-스터디-테이블)
- [14. 메시지 테이블](#14-메시지-테이블)
- [15. 출석 테이블](#15-출석-테이블)

---

## 1. 기본 코드 테이블

### 1.1 user_types (사용자 유형)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | INT | NO | PK | AUTO_INCREMENT | 유형 식별자 |
| type_code | VARCHAR(20) | NO | UK | - | 유형 코드 (STUDENT, PROFESSOR) |
| type_name | VARCHAR(50) | NO | - | - | 유형명 |

### 1.2 user_status_types (사용자 상태 유형)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | INT | NO | PK | AUTO_INCREMENT | 상태 식별자 |
| status_code | VARCHAR(20) | NO | UK | - | 상태 코드 (ACTIVE, INACTIVE, SUSPENDED) |
| status_name | VARCHAR(50) | NO | - | - | 상태명 |

### 1.3 notification_types (알림 타입)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | INT | NO | PK | AUTO_INCREMENT | 알림 타입 식별자 |
| type_code | VARCHAR(50) | NO | UK | - | 알림 타입 코드 |
| type_name | VARCHAR(100) | NO | - | - | 알림 타입명 |
| category | VARCHAR(30) | NO | IDX | - | 알림 카테고리 |
| default_message_template | TEXT | NO | - | - | 기본 메시지 템플릿 |
| is_active | BIT(1) | NO | - | 1 | 활성화 여부 |

---

## 2. 조직 테이블

### 2.1 colleges (단과대학)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 단과대 식별자 |
| college_code | VARCHAR(20) | NO | UK | - | 단과대 코드 |
| college_number_code | VARCHAR(2) | NO | UK | - | 단과대 번호 코드 (학번 생성용) |
| college_name | VARCHAR(100) | NO | - | - | 단과대명 |
| created_at | DATETIME(6) | YES | - | NULL | 생성일시 |

### 2.2 departments (학과)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 학과 식별자 |
| college_id | BIGINT | NO | FK, IDX | - | 단과대 ID |
| department_code | VARCHAR(20) | NO | UK, IDX | - | 학과 코드 |
| department_name | VARCHAR(100) | NO | - | - | 학과명 |
| created_at | DATETIME(6) | YES | - | NULL | 생성일시 |

**Foreign Keys:**
- `college_id` -> `colleges(id)`

---

## 3. 사용자 테이블

### 3.1 users (사용자)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | - | 사용자 식별자 (학번/교번) |
| email | VARCHAR(100) | NO | UK, IDX | - | 이메일 (암호화 저장) |
| password | VARCHAR(255) | NO | - | - | 비밀번호 (BCrypt 해시) |
| created_at | DATETIME(6) | NO | IDX | - | 가입일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 탈퇴일시 (Soft Delete) |

### 3.2 user_type_mappings (사용자-유형 매핑)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| user_id | BIGINT | NO | PK, FK | - | 사용자 ID |
| user_type_id | INT | NO | FK, IDX | - | 사용자 유형 ID |
| assigned_at | DATETIME(6) | YES | - | NULL | 유형 할당일시 |

**Foreign Keys:**
- `user_id` -> `users(id)`
- `user_type_id` -> `user_types(id)`

### 3.3 user_profiles (사용자 프로필)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| user_id | BIGINT | NO | PK, FK | - | 사용자 ID |
| name | VARCHAR(50) | NO | - | - | 이름 |
| created_at | DATETIME(6) | YES | - | NULL | 생성일시 |

**Foreign Keys:**
- `user_id` -> `users(id)`

### 3.4 user_primary_contacts (사용자 연락처)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| user_id | BIGINT | NO | PK, FK | - | 사용자 ID |
| mobile_number | VARCHAR(50) | YES | UK, IDX | NULL | 휴대폰 번호 |
| mobile_verified | BIT(1) | YES | IDX | NULL | 휴대폰 인증 여부 |
| home_number | VARCHAR(50) | YES | - | NULL | 자택 전화번호 |
| office_number | VARCHAR(50) | YES | - | NULL | 사무실 전화번호 |
| created_at | DATETIME(6) | YES | - | NULL | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |

**Foreign Keys:**
- `user_id` -> `users(id)`

### 3.5 user_profile_images (프로필 이미지)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| user_id | BIGINT | NO | PK, FK | - | 사용자 ID |
| image_url | VARCHAR(255) | NO | - | - | 원본 이미지 URL |
| thumbnail_url | VARCHAR(255) | YES | - | NULL | 썸네일 URL |
| created_at | DATETIME(6) | YES | IDX | NULL | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |

**Foreign Keys:**
- `user_id` -> `users(id)`

### 3.6 user_status_history (사용자 상태 이력)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 이력 식별자 |
| user_id | BIGINT | NO | FK, IDX | - | 사용자 ID |
| status_id | INT | NO | FK, IDX | - | 상태 ID |
| changed_by | BIGINT | YES | FK | NULL | 변경자 ID |
| changed_at | DATETIME(6) | YES | IDX | NULL | 변경일시 |
| reason | TEXT | YES | - | NULL | 변경 사유 |

**Foreign Keys:**
- `user_id` -> `users(id)`
- `status_id` -> `user_status_types(id)`
- `changed_by` -> `users(id)`

---

## 4. 학생 테이블

### 4.1 students (학생)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| student_id | BIGINT | NO | PK, FK, IDX | - | 학번 (users.id 참조) |
| admission_year | INT | NO | IDX | - | 입학년도 |
| grade | INT | NO | - | - | 학년 |
| created_at | DATETIME(6) | YES | - | NULL | 생성일시 |

**Foreign Keys:**
- `student_id` -> `users(id)`

### 4.2 student_departments (학생-학과 매핑)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 매핑 식별자 |
| student_id | BIGINT | NO | FK, UK, IDX | - | 학생 ID |
| department_id | BIGINT | NO | FK, UK, IDX | - | 학과 ID |
| is_primary | BIT(1) | YES | UK | NULL | 주전공 여부 |
| enrolled_date | DATE | NO | - | - | 등록일 |

**Foreign Keys:**
- `student_id` -> `students(student_id)`
- `department_id` -> `departments(id)`

**Unique Keys:**
- `uk_student_department (student_id, department_id)`
- `uk_student_primary (student_id)` - 학생당 하나의 주전공

### 4.3 student_number_sequences (학번 시퀀스)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 시퀀스 식별자 |
| year | INT | NO | UK, IDX | - | 학년도 |
| college_id | BIGINT | NO | UK, IDX | - | 단과대 ID |
| department_id | BIGINT | NO | UK, IDX | - | 학과 ID |
| last_sequence | INT | NO | - | - | 마지막 시퀀스 번호 |
| version | BIGINT | YES | - | NULL | 낙관적 락 버전 |

**Unique Keys:**
- `uk_student_seq (year, college_id, department_id)`

---

## 5. 교수 테이블

### 5.1 professors (교수)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| professor_id | BIGINT | NO | PK, FK, IDX | - | 교번 (users.id 참조) |
| appointment_date | DATE | NO | - | - | 임용일 |
| created_at | DATETIME(6) | YES | - | NULL | 생성일시 |

**Foreign Keys:**
- `professor_id` -> `users(id)`

### 5.2 professor_departments (교수-학과 매핑)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 매핑 식별자 |
| professor_id | BIGINT | NO | FK, UK, IDX | - | 교수 ID |
| department_id | BIGINT | NO | FK, UK, IDX | - | 학과 ID |
| is_primary | BIT(1) | YES | UK | NULL | 주 소속 여부 |
| start_date | DATE | NO | UK | - | 소속 시작일 |
| end_date | DATE | YES | - | NULL | 소속 종료일 |

**Foreign Keys:**
- `professor_id` -> `professors(professor_id)`
- `department_id` -> `departments(id)`

---

## 6. 인증 테이블

### 6.1 email_verifications (이메일 인증)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 인증 식별자 |
| email | VARCHAR(100) | NO | IDX | - | 이메일 |
| verification_code | VARCHAR(5) | NO | IDX | - | 인증 코드 (5자리) |
| is_verified | BIT(1) | YES | - | NULL | 인증 완료 여부 |
| verification_attempts | INT | YES | - | NULL | 인증 시도 횟수 |
| created_at | DATETIME(6) | NO | - | - | 생성일시 |
| expires_at | DATETIME(6) | NO | IDX | - | 만료일시 |
| verified_at | DATETIME(6) | YES | - | NULL | 인증완료일시 |

### 6.2 refresh_tokens (리프레시 토큰)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 토큰 식별자 |
| user_id | BIGINT | NO | FK, IDX | - | 사용자 ID |
| token | VARCHAR(500) | NO | UK, IDX | - | 리프레시 토큰 |
| device_info | VARCHAR(255) | YES | - | NULL | 디바이스 정보 |
| ip_address | VARCHAR(45) | YES | - | NULL | IP 주소 (IPv6 지원) |
| is_revoked | BIT(1) | YES | - | NULL | 폐기 여부 |
| created_at | DATETIME(6) | NO | - | - | 발급일시 |
| expires_at | DATETIME(6) | NO | IDX | - | 만료일시 |
| last_used_at | DATETIME(6) | YES | - | NULL | 최근 사용일시 |

**Foreign Keys:**
- `user_id` -> `users(id)`

---

## 7. 알림 테이블

### 7.1 notifications (알림)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 알림 식별자 |
| recipient_id | BIGINT | NO | FK, IDX | - | 수신자 ID |
| sender_id | BIGINT | YES | FK | NULL | 발신자 ID |
| type_id | INT | NO | FK, IDX | - | 알림 타입 ID |
| title | VARCHAR(200) | YES | - | NULL | 알림 제목 |
| message | TEXT | NO | - | - | 알림 내용 |
| is_read | BIT(1) | NO | IDX | 0 | 읽음 여부 |
| read_at | DATETIME(6) | YES | - | NULL | 읽은 일시 |
| course_id | BIGINT | YES | IDX | NULL | 관련 강의 ID |
| related_entity_type | VARCHAR(50) | YES | - | NULL | 관련 엔티티 타입 |
| related_entity_id | BIGINT | YES | - | NULL | 관련 엔티티 ID |
| action_url | VARCHAR(500) | YES | - | NULL | 액션 URL |
| created_at | DATETIME(6) | NO | IDX | - | 생성일시 |

**Foreign Keys:**
- `recipient_id` -> `users(id)`
- `sender_id` -> `users(id)`
- `type_id` -> `notification_types(id)`

**Indexes:**
- `idx_recipient_unread (recipient_id, is_read, created_at)`
- `idx_recipient_created (recipient_id, created_at)`
- `idx_course_created (course_id, created_at)`

### 7.2 notification_preferences (알림 설정)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 설정 식별자 |
| user_id | BIGINT | NO | FK, UK, IDX | - | 사용자 ID |
| type_id | INT | NO | FK, UK | - | 알림 타입 ID |
| is_enabled | BIT(1) | NO | - | 1 | 알림 활성화 여부 |
| email_enabled | BIT(1) | NO | - | 0 | 이메일 알림 여부 |
| created_at | DATETIME(6) | NO | - | - | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |

**Foreign Keys:**
- `user_id` -> `users(id)`
- `type_id` -> `notification_types(id)`

### 7.3 notification_batches (알림 배치)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 배치 식별자 |
| sender_id | BIGINT | YES | FK | NULL | 발신자 ID |
| type_id | INT | NO | FK | - | 알림 타입 ID |
| title | VARCHAR(200) | YES | - | NULL | 알림 제목 |
| message | TEXT | NO | - | - | 알림 내용 |
| status | ENUM | NO | IDX | PENDING | 상태 (PENDING/PROCESSING/COMPLETED/FAILED) |
| total_recipients | INT | NO | - | 0 | 총 수신자 수 |
| course_id | BIGINT | YES | IDX | NULL | 관련 강의 ID |
| error_message | VARCHAR(500) | YES | - | NULL | 오류 메시지 |
| created_at | DATETIME(6) | NO | IDX | - | 생성일시 |
| processed_at | DATETIME(6) | YES | - | NULL | 처리완료일시 |

**Foreign Keys:**
- `sender_id` -> `users(id)`
- `type_id` -> `notification_types(id)`

---

## 8. 학사 테이블

### 8.1 academic_terms (학기)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 학기 식별자 |
| year | INT | NO | - | - | 학년도 (예: 2024) |
| term_type | VARCHAR(10) | NO | - | - | 학기 구분 (1/2/SUMMER/WINTER) |
| start_date | DATE | NO | - | - | 학기 시작일 |
| end_date | DATE | NO | - | - | 학기 종료일 |

### 8.2 period_types (기간 타입)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | INT | NO | PK | AUTO_INCREMENT | 타입 식별자 |
| type_code | VARCHAR(20) | NO | UK | - | 타입 코드 |
| type_name | VARCHAR(50) | NO | - | - | 타입명 |
| description | VARCHAR(200) | YES | - | NULL | 설명 |

### 8.3 enrollment_periods (수강신청 기간)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 기간 식별자 |
| term_id | BIGINT | NO | - | - | 학기 ID |
| period_name | VARCHAR(50) | NO | - | - | 기간명 |
| period_type_id | INT | NO | FK, IDX | - | 기간 타입 ID |
| start_datetime | TIMESTAMP | NO | IDX | - | 시작 일시 |
| end_datetime | TIMESTAMP | NO | IDX | - | 종료 일시 |
| target_year | INT | YES | - | NULL | 대상 학년 (0: 전체) |
| created_at | TIMESTAMP | YES | - | NOW() | 생성일시 |

**Foreign Keys:**
- `period_type_id` -> `period_types(id)`

---

## 9. 강의 테이블

### 9.1 subjects (과목)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 과목 식별자 |
| subject_code | VARCHAR(8) | NO | - | - | 과목 코드 (예: CS101) |
| subject_name | VARCHAR(64) | NO | - | - | 과목명 |
| department_id | BIGINT | NO | - | - | 개설 학과 ID |
| course_type_id | INT | NO | - | - | 이수 구분 ID |
| credits | INT | NO | - | - | 학점 수 (1~4) |
| theory_hours | INT | YES | - | 0 | 이론 시수 |
| practice_hours | INT | YES | - | 0 | 실습 시수 |
| subject_description | VARCHAR(200) | NO | - | - | 과목 개요 |
| description | TEXT | YES | - | NULL | 과목 상세 설명 |
| created_at | TIMESTAMP | YES | - | NOW() | 등록일시 |

### 9.2 subject_prerequisites (선수과목)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 관계 식별자 |
| subject_id | BIGINT | NO | - | - | 과목 ID |
| prerequisite_id | BIGINT | NO | - | - | 선수과목 ID |
| is_mandatory | TINYINT(1) | YES | - | 1 | 필수 이수 여부 |
| created_at | TIMESTAMP | YES | - | NOW() | 생성일시 |

### 9.3 course_types (강의 유형)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | INT | NO | PK | AUTO_INCREMENT | 유형 식별자 |
| type_code | INT | NO | UK | - | 유형 코드 (0~3) |
| category | INT | NO | - | - | 상위 분류 (0: 전공, 1: 교양) |

**유형 코드:**
- 0: MAJOR_REQ (전공필수)
- 1: MAJOR_ELEC (전공선택)
- 2: GEN_REQ (교양필수)
- 3: GEN_ELEC (교양선택)

### 9.4 courses (강의)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 강의 식별자 |
| subject_id | BIGINT | NO | - | - | 과목 ID |
| academic_term_id | BIGINT | NO | - | - | 학기 ID |
| section_number | VARCHAR(10) | NO | - | - | 분반 번호 (01, 02, A, B) |
| current_students | INT | NO | - | - | 현재 수강생 수 |
| professor_id | BIGINT | NO | - | - | 담당 교수 ID |
| max_students | INT | NO | - | - | 수강 정원 |
| created_at | TIMESTAMP | YES | - | NOW() | 개설일시 |
| description | TEXT | YES | - | NULL | 강의 설명 (분반별) |

### 9.5 course_schedules (강의 시간표)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| schedule_id | BIGINT | NO | PK | AUTO_INCREMENT | 시간표 식별자 |
| course_id | BIGINT | NO | - | - | 강의 ID |
| day_of_week | INT | NO | - | - | 요일 (1: 월 ~ 5: 금) |
| start_time | TIME | NO | - | - | 시작 시간 |
| end_time | TIME | NO | - | - | 종료 시간 |
| schedule_room | VARCHAR(50) | NO | - | - | 강의실 |

### 9.6 course_weeks (강의 주차)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 주차 식별자 |
| course_id | BIGINT | NO | - | - | 강의 ID |
| week_number | INT | NO | - | - | 주차 번호 |
| week_title | VARCHAR(200) | NO | - | - | 주차 제목 |
| created_at | TIMESTAMP | YES | - | NOW() | 생성일시 |

### 9.7 week_contents (주차별 콘텐츠)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 콘텐츠 식별자 |
| week_id | BIGINT | NO | - | - | 주차 ID |
| content_type | VARCHAR(20) | NO | - | - | 콘텐츠 유형 (VIDEO/DOCUMENT/LINK) |
| title | VARCHAR(200) | NO | - | - | 콘텐츠 제목 |
| content_url | VARCHAR(500) | NO | - | - | 콘텐츠 URL |
| duration | VARCHAR(10) | YES | - | NULL | 동영상 길이 (예: 45:23) |
| display_order | INT | NO | - | - | 표시 순서 |
| created_at | TIMESTAMP | YES | - | NOW() | 생성일시 |

### 9.8 course_status_types (강의 상태 유형)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | INT | NO | PK | AUTO_INCREMENT | 상태 식별자 |
| status_code | VARCHAR(20) | NO | UK | - | 상태 코드 (ONGOING/COMPLETED/PENDING) |
| status_name | VARCHAR(50) | NO | - | - | 상태명 |

---

## 10. 수강 테이블

### 10.1 course_carts (수강 장바구니)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 장바구니 식별자 |
| student_id | BIGINT | NO | - | - | 학생 ID |
| course_id | BIGINT | NO | - | - | 강의 ID |
| added_at | TIMESTAMP | YES | - | NOW() | 담은 일시 |

### 10.2 enrollments (수강신청)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 수강신청 식별자 |
| course_id | BIGINT | NO | - | - | 강의 ID |
| student_id | BIGINT | NO | - | - | 학생 ID |
| enrolled_at | TIMESTAMP | YES | - | NOW() | 수강신청 일시 |

### 10.3 enrollment_status_types (수강 상태 유형)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | INT | NO | PK | AUTO_INCREMENT | 상태 식별자 |
| status_code | VARCHAR(20) | NO | UK | - | 상태 코드 (ENROLLED/COMPLETED) |
| status_name | VARCHAR(50) | NO | - | - | 상태명 |

---

## 11. 게시판 테이블

### 11.1 board_categories (게시판 카테고리)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 카테고리 식별자 |
| board_type | ENUM | NO | UK | - | 게시판 유형 |
| allow_comments | BIT(1) | YES | - | 1 | 댓글 허용 여부 |
| allow_attachments | BIT(1) | YES | - | 1 | 첨부파일 허용 여부 |
| allow_anonymous | BIT(1) | YES | - | 0 | 익명 작성 허용 여부 |
| is_deleted | BIT(1) | YES | IDX | 0 | 삭제 여부 |
| created_at | DATETIME(6) | NO | - | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |

**board_type ENUM 값:**
NOTICE, FREE, QUESTION, DISCUSSION, PROFESSOR, STUDENT, DEPARTMENT, CONTEST, CAREER, ASSIGNMENT, EXAM, QUIZ, STUDY_RECRUITMENT

### 11.2 posts (게시글)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 게시글 식별자 |
| category_id | BIGINT | NO | FK, IDX | - | 카테고리 ID |
| course_id | BIGINT | YES | IDX | NULL | 강의 ID (강의 게시판용) |
| department_id | BIGINT | YES | IDX | NULL | 학과 ID (학과 게시판용) |
| author_id | BIGINT | NO | FK, IDX | - | 작성자 ID |
| title | VARCHAR(255) | NO | - | - | 제목 |
| content | TEXT | NO | - | - | 내용 |
| post_type | VARCHAR(30) | NO | IDX | - | 게시글 유형 |
| is_anonymous | BIT(1) | YES | - | 0 | 익명 여부 |
| view_count | INT | YES | - | 0 | 조회수 |
| like_count | INT | YES | - | 0 | 좋아요 수 |
| is_deleted | BIT(1) | YES | IDX | 0 | 삭제 여부 |
| created_by | BIGINT | NO | FK, IDX | - | 생성자 ID |
| updated_by | BIGINT | YES | FK | NULL | 수정자 ID |
| created_at | DATETIME(6) | NO | IDX | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |

**Foreign Keys:**
- `category_id` -> `board_categories(id)`
- `author_id` -> `users(id)`
- `created_by` -> `users(id)`
- `updated_by` -> `users(id)`

### 11.3 post_admin_settings (게시글 관리자 설정)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 설정 식별자 |
| post_id | BIGINT | NO | FK, UK | - | 게시글 ID |
| status | VARCHAR(20) | NO | IDX | ACTIVE | 상태 (ACTIVE/CLOSED/ARCHIVED) |
| priority | VARCHAR(20) | NO | IDX | NORMAL | 우선순위 (HIGH/NORMAL/LOW) |
| is_notice | BIT(1) | NO | IDX | 0 | 공지사항 여부 |
| is_pinned | BIT(1) | NO | IDX | 0 | 상단 고정 여부 |
| managed_by | BIGINT | YES | FK, IDX | NULL | 최종 관리자 ID |
| managed_at | DATETIME(6) | YES | - | NULL | 최종 관리 일시 |

**Foreign Keys:**
- `post_id` -> `posts(id)`
- `managed_by` -> `users(id)`

### 11.4 comments (댓글)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 댓글 식별자 |
| post_id | BIGINT | NO | FK, IDX | - | 게시글 ID |
| author_id | BIGINT | NO | FK, IDX | - | 작성자 ID |
| parent_comment_id | BIGINT | YES | FK, IDX | NULL | 부모 댓글 ID (대댓글용) |
| content | TEXT | NO | - | - | 댓글 내용 |
| depth | INT | YES | IDX | 0 | 댓글 깊이 (0: 댓글, 1: 대댓글) |
| is_anonymous | BIT(1) | YES | - | 0 | 익명 여부 |
| is_deleted | BIT(1) | YES | IDX | 0 | 삭제 여부 |
| is_deleted_by_admin | BIT(1) | YES | - | 0 | 관리자 삭제 여부 |
| created_by | BIGINT | NO | FK, IDX | - | 생성자 ID |
| updated_by | BIGINT | YES | FK | NULL | 수정자 ID |
| created_at | DATETIME(6) | NO | IDX | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |

**Foreign Keys:**
- `post_id` -> `posts(id)`
- `author_id` -> `users(id)`
- `parent_comment_id` -> `comments(id)`
- `created_by` -> `users(id)`
- `updated_by` -> `users(id)`

### 11.5 attachments (첨부파일)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 첨부파일 식별자 |
| post_id | BIGINT | YES | FK, IDX | NULL | 게시글 ID |
| comment_id | BIGINT | YES | FK, IDX | NULL | 댓글 ID |
| attachment_type | VARCHAR(30) | NO | IDX | - | 첨부 유형 |
| original_name | VARCHAR(255) | NO | - | - | 원본 파일명 |
| stored_name | VARCHAR(255) | NO | - | - | 서버 저장 파일명 (UUID) |
| file_path | VARCHAR(500) | NO | - | - | 파일 저장 경로 |
| file_size | BIGINT | NO | - | - | 파일 크기 (bytes) |
| mime_type | VARCHAR(100) | YES | IDX | NULL | MIME 타입 |
| uploader_id | BIGINT | YES | FK, IDX | NULL | 업로더 ID |
| download_count | INT | YES | - | 0 | 다운로드 횟수 |
| is_deleted | BIT(1) | YES | - | 0 | 삭제 여부 |
| created_at | DATETIME(6) | NO | - | NOW() | 업로드일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | IDX | NULL | 삭제일시 |

**attachment_type 값:** POST_CONTENT, POST_BOTTOM, COMMENT

### 11.6 hashtags (해시태그)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 해시태그 식별자 |
| name | VARCHAR(50) | NO | UK, IDX | - | 태그 이름 (#제외, 소문자) |
| display_name | VARCHAR(50) | NO | - | - | 화면 표시용 태그명 |
| description | VARCHAR(255) | YES | - | NULL | 태그 설명 |
| color | VARCHAR(7) | YES | - | #007bff | 태그 색상 (HEX) |
| tag_category | VARCHAR(30) | YES | IDX | NULL | 태그 카테고리 |
| is_active | BIT(1) | YES | IDX | 1 | 활성화 상태 |
| created_by | BIGINT | YES | FK | NULL | 태그 생성자 ID |
| created_at | DATETIME(6) | NO | - | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| updated_by | BIGINT | YES | FK, IDX | NULL | 수정자 ID |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |
| is_deleted | BIT(1) | YES | - | 0 | 삭제 여부 |

### 11.7 post_hashtags (게시글-해시태그 연결)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 연결 식별자 |
| post_id | BIGINT | NO | FK, UK | - | 게시글 ID |
| hashtag_id | BIGINT | NO | FK, UK, IDX | - | 해시태그 ID |
| created_by | BIGINT | NO | FK, IDX | - | 태그 추가자 ID |
| created_at | DATETIME(6) | NO | - | NOW() | 연결 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| updated_by | BIGINT | YES | FK, IDX | NULL | 수정자 ID |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |
| is_deleted | BIT(1) | YES | - | 0 | 삭제 여부 |

**Unique Keys:**
- `idx_post_hashtag (post_id, hashtag_id)`

### 11.8 post_likes (좋아요)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 좋아요 식별자 |
| user_id | BIGINT | NO | FK, UK, IDX | - | 사용자 ID |
| post_id | BIGINT | YES | FK, UK, IDX | NULL | 게시글 ID |
| comment_id | BIGINT | YES | FK, UK, IDX | NULL | 댓글 ID |
| like_type | VARCHAR(20) | NO | IDX | - | 좋아요 유형 (POST/COMMENT) |
| created_at | DATETIME(6) | NO | - | NOW() | 좋아요 생성일시 |
| deleted_at | DATETIME(6) | YES | IDX | NULL | 좋아요 취소일시 |

**Unique Keys:**
- `idx_user_post_like (user_id, post_id)`
- `idx_user_comment_like (user_id, comment_id)`

### 11.9 post_bookmarks (북마크)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 북마크 식별자 |
| user_id | BIGINT | NO | FK, UK, IDX | - | 사용자 ID |
| post_id | BIGINT | NO | FK, UK | - | 게시글 ID |
| bookmark_category | VARCHAR(50) | YES | IDX | NULL | 북마크 카테고리 |
| notes | TEXT | YES | - | NULL | 개인 메모 |
| created_at | DATETIME(6) | NO | - | NOW() | 북마크 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |

### 11.10 post_views (조회수 추적)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 조회 식별자 |
| post_id | BIGINT | NO | FK, IDX | - | 게시글 ID |
| user_id | BIGINT | YES | FK, IDX | NULL | 사용자 ID (비로그인 시 NULL) |
| ip_address | VARCHAR(45) | YES | - | NULL | IP 주소 (IPv6 지원) |
| user_agent | TEXT | YES | - | NULL | 브라우저 정보 |
| viewed_at | DATETIME(6) | NO | IDX | NOW() | 조회일시 |

### 11.11 attachment_downloads (다운로드 추적)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 다운로드 식별자 |
| attachment_id | BIGINT | NO | FK, IDX | - | 첨부파일 ID |
| user_id | BIGINT | YES | FK, IDX | NULL | 다운로더 ID |
| ip_address | VARCHAR(45) | YES | - | NULL | IP 주소 |
| downloaded_at | DATETIME(6) | NO | IDX | NOW() | 다운로드일시 |

---

## 12. 과제/시험 테이블

### 12.1 assignments (과제)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 과제 식별자 |
| post_id | BIGINT | NO | FK, UK | - | 게시글 ID (제목/내용 참조) |
| course_id | BIGINT | NO | IDX | - | 강의 ID |
| due_date | DATETIME(6) | NO | IDX | - | 제출 마감일 |
| max_score | DECIMAL(5,2) | NO | - | - | 만점 |
| submission_method | VARCHAR(20) | NO | - | - | 제출방법 |
| late_submission_allowed | BIT(1) | NO | - | 0 | 지각 제출 허용 |
| late_penalty_percent | DECIMAL(3,2) | YES | - | NULL | 지각 감점 비율 (%) |
| max_file_size_mb | INT | YES | - | 10 | 최대 파일 크기 (MB) |
| allowed_file_types | VARCHAR(255) | YES | - | NULL | 허용 파일 확장자 |
| instructions | TEXT | YES | - | NULL | 제출 지침 |
| created_by | BIGINT | NO | FK, IDX | - | 생성자 ID |
| created_at | DATETIME(6) | NO | - | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| updated_by | BIGINT | YES | FK | NULL | 수정자 ID |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |
| is_deleted | BIT(1) | NO | - | 0 | 삭제 여부 |

**submission_method 값:** FILE_UPLOAD, TEXT_INPUT, BOTH

### 12.2 assignment_submissions (과제 제출)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 제출 식별자 |
| assignment_id | BIGINT | NO | FK, UK | - | 과제 ID |
| user_id | BIGINT | NO | FK, UK, IDX | - | 제출자 ID |
| content | TEXT | YES | - | NULL | 텍스트 제출 내용 |
| submitted_at | DATETIME(6) | NO | IDX | NOW() | 제출일시 |
| status | VARCHAR(20) | NO | IDX | SUBMITTED | 제출 상태 |
| score | DECIMAL(5,2) | YES | - | NULL | 획득 점수 |
| feedback | TEXT | YES | - | NULL | 피드백 |
| graded_at | DATETIME(6) | YES | - | NULL | 채점일시 |
| graded_by | BIGINT | YES | FK | NULL | 채점자 ID |
| created_at | DATETIME(6) | NO | - | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |
| created_by | BIGINT | YES | FK, IDX | NULL | 생성자 ID |
| updated_by | BIGINT | YES | FK | NULL | 수정자 ID |
| is_deleted | TINYINT(1) | NO | IDX | 0 | 삭제 여부 |
| allow_resubmission | BOOLEAN | YES | - | FALSE | 재제출 허용 여부 |
| resubmission_deadline | DATETIME(6) | YES | - | NULL | 재제출 마감일 |

**Unique Keys:**
- `idx_assignment_user (assignment_id, user_id)`

### 12.3 submission_attachments (과제 제출 첨부파일)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 연결 식별자 |
| submission_id | BIGINT | NO | FK, UK, IDX | - | 과제 제출 ID |
| attachment_id | BIGINT | NO | FK, UK, IDX | - | 첨부파일 ID |
| created_at | DATETIME(6) | NO | - | NOW() | 생성일시 |

### 12.4 exams (시험)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 시험 식별자 |
| post_id | BIGINT | NO | FK, UK | - | 게시글 ID |
| course_id | BIGINT | NO | IDX | - | 강의 ID |
| exam_type | VARCHAR(20) | NO | IDX | - | 시험 유형 (MIDTERM/FINAL/QUIZ/REGULAR) |
| exam_date | DATETIME(6) | NO | IDX | - | 시험 일시 |
| duration_minutes | INT | NO | - | - | 시험 시간 (분) |
| total_score | DECIMAL(5,2) | NO | - | - | 총점 |
| is_online | BIT(1) | NO | - | 0 | 온라인 시험 여부 |
| location | VARCHAR(100) | YES | - | NULL | 시험 장소 |
| instructions | TEXT | YES | - | NULL | 시험 안내사항 |
| question_count | INT | YES | - | NULL | 문제 수 |
| passing_score | DECIMAL(5,2) | YES | - | NULL | 합격 점수 |
| created_by | BIGINT | NO | FK, IDX | - | 생성자 ID |
| created_at | DATETIME(6) | NO | - | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |

### 12.5 exam_results (시험 결과)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 결과 식별자 |
| exam_id | BIGINT | NO | FK, UK | - | 시험 ID |
| user_id | BIGINT | NO | FK, UK, IDX | - | 응시자 ID |
| started_at | DATETIME(6) | YES | - | NULL | 시험 시작 시간 |
| submitted_at | DATETIME(6) | YES | IDX | NULL | 제출 시간 |
| score | DECIMAL(5,2) | YES | IDX | NULL | 획득 점수 |
| grade | VARCHAR(2) | YES | IDX | NULL | 등급 (A+, A, B+ 등) |
| answer_data | TEXT | YES | - | NULL | 답안 데이터 (JSON) |
| feedback | TEXT | YES | - | NULL | 피드백 |
| graded_at | DATETIME(6) | YES | - | NULL | 채점일시 |
| graded_by | BIGINT | YES | FK | NULL | 채점자 ID |
| created_at | DATETIME(6) | NO | - | NOW() | 생성일시 |
| updated_at | DATETIME(6) | YES | - | NULL | 수정일시 |
| deleted_at | DATETIME(6) | YES | - | NULL | 삭제일시 |

---

## 13. 스터디 테이블

### 13.1 study_recruitments (스터디 모집)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 모집 식별자 |
| post_id | BIGINT | NO | FK, UK | - | 게시글 ID |
| study_type | VARCHAR(20) | NO | IDX | - | 스터디 유형 |
| max_participants | INT | NO | - | - | 최대 참여 인원 |
| recruitment_status | VARCHAR(20) | NO | IDX | RECRUITING | 모집 상태 |
| recruitment_deadline | DATETIME(6) | YES | IDX | NULL | 모집 마감일 |
| requirements | TEXT | YES | - | NULL | 지원 자격/조건 |

**study_type 값:** EXAM_PREP, PROJECT, LANGUAGE, CERTIFICATION, READING, CODING, OTHER

**recruitment_status 값:** RECRUITING, COMPLETED, CANCELLED, IN_PROGRESS, FINISHED

### 13.2 study_applications (스터디 지원)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 지원 식별자 |
| study_recruitment_id | BIGINT | NO | FK, UK | - | 스터디 모집 ID |
| applicant_id | BIGINT | NO | FK, UK, IDX | - | 지원자 ID |
| application_message | TEXT | YES | - | NULL | 지원 메시지 |
| status | VARCHAR(20) | NO | IDX | PENDING | 지원 상태 |
| applied_at | DATETIME(6) | NO | IDX | NOW() | 지원일시 |
| processed_at | DATETIME(6) | YES | - | NULL | 처리일시 |
| process_message | TEXT | YES | - | NULL | 처리 메시지 (거절 사유 등) |

**status 값:** PENDING, APPROVED, REJECTED, WITHDRAWN

---

## 14. 메시지 테이블

### 14.1 conversations (대화방)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 대화방 식별자 |
| user1_id | BIGINT | NO | FK, UK, IDX | - | 사용자1 ID |
| user2_id | BIGINT | NO | FK, UK, IDX | - | 사용자2 ID |
| user1_unread_count | INT | NO | - | 0 | 사용자1 안읽음 수 |
| user2_unread_count | INT | NO | - | 0 | 사용자2 안읽음 수 |
| user1_deleted_at | TIMESTAMP | YES | - | NULL | 사용자1 삭제일시 |
| user2_deleted_at | TIMESTAMP | YES | - | NULL | 사용자2 삭제일시 |
| last_message_content | VARCHAR(500) | YES | - | NULL | 마지막 메시지 내용 |
| last_message_at | TIMESTAMP | YES | IDX | NULL | 마지막 메시지 일시 |
| last_message_sender_id | BIGINT | YES | - | NULL | 마지막 메시지 발신자 |
| created_at | TIMESTAMP | NO | - | NOW() | 생성일시 |
| updated_at | TIMESTAMP | NO | - | NOW() | 수정일시 (자동 갱신) |

**Foreign Keys:**
- `user1_id` -> `users(id)`
- `user2_id` -> `users(id)`

**Unique Keys:**
- `uk_conversations_users (user1_id, user2_id)`

### 14.2 messages (메시지)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 메시지 식별자 |
| conversation_id | BIGINT | NO | FK, IDX | - | 대화방 ID |
| sender_id | BIGINT | NO | FK, IDX | - | 발신자 ID |
| content | TEXT | NO | - | - | 메시지 내용 |
| read_at | TIMESTAMP | YES | - | NULL | 읽은 일시 |
| deleted_by_sender | BOOLEAN | NO | - | FALSE | 발신자 삭제 여부 |
| deleted_by_receiver | BOOLEAN | NO | - | FALSE | 수신자 삭제 여부 |
| created_at | TIMESTAMP | NO | IDX | NOW() | 생성일시 |

**Foreign Keys:**
- `conversation_id` -> `conversations(id)` (ON DELETE CASCADE)
- `sender_id` -> `users(id)`

**Indexes:**
- `idx_messages_conversation_created (conversation_id, created_at DESC)`

---

## 15. 출석 테이블

### 15.1 week_attendance (주차별 출석)

| 컬럼명 | 타입 | NULL | Key | 기본값 | 설명 |
|--------|------|------|-----|--------|------|
| id | BIGINT | NO | PK | AUTO_INCREMENT | 출석 식별자 |
| student_id | BIGINT | NO | UK, IDX | - | 학생 ID |
| week_id | BIGINT | NO | UK, IDX | - | 주차 ID |
| course_id | BIGINT | NO | IDX | - | 강의 ID |
| is_completed | BOOLEAN | NO | IDX | FALSE | 출석 완료 여부 |
| completed_video_count | INT | NO | - | 0 | 완료한 VIDEO 수 |
| total_video_count | INT | NO | - | - | 전체 VIDEO 수 |
| first_accessed_at | TIMESTAMP | YES | - | NULL | 최초 접근 일시 |
| completed_at | TIMESTAMP | YES | - | NULL | 출석 완료 일시 |

**Unique Keys:**
- `uk_week_attendance_student_week (student_id, week_id)`

**비즈니스 규칙:** 학생이 해당 주차의 모든 VIDEO 콘텐츠를 완료하면 출석으로 인정

---

## 부록: ERD

![LMS ERD](./lms_db.png)



---

> **작성자** : 송명주 (최종 수정일 : 2025-12-22)
