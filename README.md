# MZC 1st Backend Project - LMS (Learning Management System)

백엔드 리포지토리

## Database Schema
- [DB Diagram](https://dbdiagram.io/d/MZC_TEAM2-691ebf15228c5bbc1aadecbc)

---

## API Specification

> 도메인별 API 문서: [api-spec/README.md](./api-spec/README.md)

| 도메인 | 설명 | API 수 |
|--------|------|--------|
| [Auth](./api-spec/auth.md) | 인증/인가 (회원가입, 로그인, 토큰) | 7개 |
| [Profile](./api-spec/profile.md) | 프로필 관리 (조회, 수정, 이미지) | 4개 |
| [User Search](./api-spec/user-search.md) | 유저 검색 (학생/교수 탐색) | 4개 |
| [Course](./api-spec/course.md) | 강의 관리 (조회, 개설, 주차/콘텐츠) | 16개 |
| [Subject](./api-spec/subject.md) | 과목 관리 (목록, 상세, 검색) | 3개 |
| [Enrollment](./api-spec/enrollment.md) | 수강신청 (장바구니, 신청, 취소) | 9개 |
| [Attendance](./api-spec/attendance.md) | 출석 관리 (학생/교수) | 6개 |
| [Board](./api-spec/board.md) | 게시판 (게시글, 댓글, 첨부파일) | 15개 |
| [Assignment](./api-spec/assignment.md) | 과제 (등록, 제출, 채점) | 10개 |
| [Message](./api-spec/message.md) | 메시지 (대화방, 메시지, SSE) | 12개 |
| [Notification](./api-spec/notification.md) | 알림 (조회, 읽음처리, 삭제) | 8개 |
| [Dashboard](./api-spec/dashboard.md) | 대시보드 (학생용) | 4개 |

---

## Flow Charts

### Auth (인증)
| 플로우 | 설명 |
|--------|------|
| [로그인 플로우](./flow-chart/auth/로그인%20플로우.png) | 사용자 로그인 처리 과정 |
| [토큰 갱신 플로우](./flow-chart/auth/토큰%20갱신%20플로우.png) | JWT 토큰 갱신 프로세스 |
| [회원가입 플로우](./flow-chart/auth/회원가입%20플로우.png) | 신규 사용자 등록 과정 |

### Assignment (과제)

#### Professor (교수)
| 플로우 | 설명 |
|--------|------|
| [과제 등록 플로우](./flow-chart/Assignment/professor/과제%20등록%20플로우.png) | 교수의 과제 생성 및 등록 |
| [과제 채점 플로우](./flow-chart/Assignment/professor/과제%20채점%20플로우.png) | 제출된 과제 채점 과정 |

#### Student (학생)
| 플로우 | 설명 |
|--------|------|
| [과제 제출 플로우](./flow-chart/Assignment/student/과제%20제출%20플로우.png) | 학생의 과제 제출 과정 |

### Attendance (출석)
| 플로우 | 설명 |
|--------|------|
| [교수 출석 관리 플로우](./flow-chart/Attendance/교수%20출석%20관리%20플로우.png) | 교수의 출석 체크 및 관리 |
| [학생 출석 조회 플로우](./flow-chart/Attendance/학생%20출석%20조회%20플로우.png) | 학생의 본인 출석 현황 조회 |

### Board (게시판)
| 플로우 | 설명 |
|--------|------|
| [게시글 목록 조회 플로우](./flow-chart/Board/게시글%20목록%20조회%20플로우.png) | 게시글 리스트 조회 |
| [게시글 생성 플로우](./flow-chart/Board/게시글%20생성%20플로우.png) | 새 게시글 작성 과정 |
| [댓글 생성 플로우](./flow-chart/Board/댓글%20생성%20플로우.png) | 게시글에 댓글 작성 |

### Course (강의)
| 플로우 | 설명 |
|--------|------|
| [강의 검색/상세 조회 플로우](./flow-chart/Course/강의%20검색상세%20조회%20플로우.png) | 강의 검색 및 상세 정보 조회 |

### Dashboard (대시보드)
| 플로우 | 설명 |
|--------|------|
| [학생 대시보드 데이터 조회 플로우](./flow-chart/Dashboard/학생%20대시보드%20데이터%20조회%20플로우.png) | 학생용 대시보드 데이터 로딩 |

### Enrollment (수강신청)
| 플로우 | 설명 |
|--------|------|
| [수강신청 전체 프로세스](./flow-chart/Enrollment/수강신청%20전체%20프로세스.png) | 수강신청 전체 흐름 개요 |
| [일괄 수강신청 상세 플로우](./flow-chart/Enrollment/일괄%20수강신청%20상세%20플로우.png) | 여러 강의 동시 수강신청 |
| [장바구니 일괄 추가 플로우](./flow-chart/Enrollment/장바구니%20일괄%20추가%20플로우.png) | 장바구니에 강의 일괄 추가 |
| [장바구니 조회/삭제 플로우](./flow-chart/Enrollment/장바구니%20조회삭제%20플로우.png) | 장바구니 관리 기능 |

### Message (메시지)
| 플로우 | 설명 |
|--------|------|
| [대화방 생성 및 메시지 발송 플로우](./flow-chart/Message/대화방%20생성%20및%20메시지%20발송%20플로우.png) | 1:1 메시지 대화방 생성 및 발송 |
| [메시지 조회 플로우](./flow-chart/Message/메시지%20조회%20플로우.png) | 메시지 내역 조회 |

### Notification (알림)
| 플로우 | 설명 |
|--------|------|
| [알림 발생 플로우](./flow-chart/Notification/알림%20발생%20플로우.png) | 시스템 알림 생성 과정 |
| [알림 조회/관리 플로우](./flow-chart/Notification/알림%20조회관리%20플로우.png) | 알림 확인 및 관리 |

### Profile (프로필)
| 플로우 | 설명 |
|--------|------|
| [프로필 조회/수정 플로우](./flow-chart/Profile/프로필%20조회수정%20플로우.png) | 사용자 프로필 조회 및 수정 |
| [프로필 이미지 관리 플로우](./flow-chart/Profile/프로필%20이미지%20관리%20플로우.png) | 프로필 이미지 업로드/변경 |
| [프로필 이미지 조회 플로우](./flow-chart/Profile/프로필%20이미지%20조회%20플로우.png) | 프로필 이미지 로딩 |

### Video Streaming (영상 스트리밍)
| 플로우 | 설명 |
|--------|------|
| [시스템 아키텍처 개요](./flow-chart/Video%20Streaming/시스템%20아키텍처%20개요.png) | 영상 스트리밍 시스템 전체 구조 |
| [영상 업로드부터 시청 완료까지 전체 플로우](./flow-chart/Video%20Streaming/영상%20업로드부터%20시청%20완료까지%20전체%20플로우.png) | E2E 영상 처리 흐름 |
| [영상 업로드 플로우 (TUS Protocol)](./flow-chart/Video%20Streaming/영상%20업로드%20플로우%20(TUS%20Protocol).png) | TUS 프로토콜 기반 대용량 업로드 |
| [영상 스트리밍 플로우](./flow-chart/Video%20Streaming/영상%20스트리밍%20플로우.png) | HLS 기반 영상 재생 |
| [시청 세션 관리 플로우](./flow-chart/Video%20Streaming/시청%20세션%20관리%20플로우.png) | 동시 시청 제한 및 세션 관리 |
| [진도 추적 플로우](./flow-chart/Video%20Streaming/진도%20추적%20플로우.png) | 영상 시청 진도 기록 |
| [학습률 계산 플로우](./flow-chart/Video%20Streaming/학습률%20계산%20플로우.png) | 강의 학습 완료율 계산 |
| [부정 시청 감지 플로우](./flow-chart/Video%20Streaming/부정%20시청%20감지%20플로우.png) | 비정상 시청 패턴 탐지 |
