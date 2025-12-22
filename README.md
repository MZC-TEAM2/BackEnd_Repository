# MZC 1st Backend Project - LMS (Learning Management System)

대학교 학습 관리 시스템 백엔드 서버

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Database | MySQL 8.0 |
| Cache | Redis 7 |
| Reverse Proxy | Nginx |
| Authentication | JWT (Access/Refresh Token) |
| DB Migration | Flyway |
| API Docs | Swagger (SpringDoc OpenAPI) |
| Code Quality | CheckStyle, PMD, SpotBugs |
| Container | Docker, Docker Compose |

---
## Documentation

| Document | Description |
|----------|-------------|
| [API Specification](./api-spec/README.md) | 도메인별 API 명세 (12개 도메인, 94개 API) |
| [API Operations](./docs/API_OPERATIONS.md) | API 오퍼레이션 명세서 |
| [Flow Charts](./flow-chart) | 기능별 플로우 차트 (33개) |
| [DB Diagram](./docs/lms_db.png) | 데이터베이스 ERD |
| [Table Specification](./docs/TABLE_SPEC.md) | 테이블 명세서 (50+ 테이블) |


---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          Client                                  │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Nginx (Reverse Proxy)                        │
│  • API 요청 프록시 (/api/*)                                       │
│  • 정적 파일 서빙 (프로필 이미지)                                   │
│  • Gzip 압축                                                      │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Spring Boot Application                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  Security   │  │   Domain    │  │    Util     │              │
│  │  (JWT Auth) │  │  Services   │  │  (Lock/File)│              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└───────────┬───────────────────────────────┬─────────────────────┘
            │                               │
            ▼                               ▼
┌─────────────────────┐         ┌─────────────────────┐
│     MySQL 8.0       │         │      Redis 7        │
│  • 영속 데이터 저장    │         │  • 세션/토큰 캐싱     │
│  • Flyway 마이그레이션 │         │  • 분산 락 (Redisson) │
└─────────────────────┘         │  • 알림 큐           │
                                └─────────────────────┘
```

---

## Domains

| Domain | Description |
|--------|-------------|
| **User** | 사용자 관리 (학생, 교수), 인증/인가, 프로필 |
| **Academy** | 단과대, 학과 정보 관리 |
| **Course** | 강의 개설, 주차별 콘텐츠, 강의 검색 |
| **Enrollment** | 수강신청, 장바구니, 일괄 신청 (분산 락 적용) |
| **Attendance** | 출석 체크, 출석 현황 조회 |
| **Board** | 게시판 (공지, 자유, Q&A), 게시글 CRUD, 댓글 |
| **Message** | 1:1 대화, 메시지 발송, 읽음 처리 |
| **Notification** | 알림 발송 (Redis Queue), SSE 실시간 푸시 |
| **Dashboard** | 학생 대시보드 (미제출 과제, 오늘 강의, 수강 현황) |

---

## Project Structure

```
springboot/
├── src/main/java/com/mzc/backend/lms/
│   ├── common/
│   │   ├── config/          # Security, Redis, JPA, Swagger 설정
│   │   └── exceptions/      # 공통 예외 처리
│   ├── domains/
│   │   ├── user/            # 인증, 프로필, 유저 검색
│   │   ├── academy/         # 단과대, 학과
│   │   ├── course/          # 강의, 주차별 콘텐츠
│   │   ├── enrollment/      # 수강신청
│   │   ├── attendance/      # 출석
│   │   ├── board/           # 게시판, 댓글
│   │   ├── message/         # 메시지
│   │   ├── notification/    # 알림
│   │   └── dashboard/       # 대시보드
│   ├── util/
│   │   ├── file/            # 파일 업로드
│   │   ├── image/           # 이미지 처리 (WebP 변환)
│   │   └── lock/            # 분산 락 (Redisson)
│   └── views/               # SSE, 뷰 관련
└── src/main/resources/
    └── db/migration/        # Flyway 마이그레이션 스크립트
```

---


### Docker Infrastructure

| Container | Image | Port | Description |
|-----------|-------|------|-------------|
| lms-mysql | mysql:8.0 | 3306 | MySQL 8.0 데이터베이스 |
| lms-redis | redis:7-alpine | 6379 | Redis 7 캐시/메시지 큐 |
| lms-phpmyadmin | phpmyadmin:latest | 8081 | MySQL 관리 UI |
| lms-redis-commander | rediscommander/redis-commander | 8082 | Redis 관리 UI |

### Access Points

| Service | URL |
|---------|-----|
| API Server | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| phpMyAdmin | http://localhost:8081 |
| Redis Commander | http://localhost:8082 |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

---
