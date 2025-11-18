# 문서 인덱스

예약 관리 서비스의 전체 문서 구조와 읽기 가이드입니다.

---

## 문서 읽기 가이드

### 처음 시작하는 경우

1. [README.md](../README.md) - 프로젝트 전체 개요 및 핵심 기능
2. [PROJECT_REQUIREMENTS.md](requirements/PROJECT_REQUIREMENTS.md) - 비즈니스 요구사항
3. [ARCHITECTURE_DECISION.md](architecture/ARCHITECTURE_DECISION.md) - 아키텍처 설계 결정

### 개발을 시작하는 경우

1. [PROJECT_SETUP.md](PROJECT_SETUP.md) - 개발 환경 설정
2. [DOMAIN_MODEL_DESIGN.md](architecture/DOMAIN_MODEL_DESIGN.md) - 도메인 모델 설계
3. 기능별 문서 (features/) - 구현할 기능의 상세 명세

### 운영 및 유지보수

1. [TECH_STACK_ANALYSIS.md](architecture/TECH_STACK_ANALYSIS.md) - 기술 스택 분석
2. 기능별 FLOW 문서 - 비즈니스 프로세스 이해

---

## 문서 구조

### 루트 문서

| 문서 | 설명 | 대상 |
|------|------|------|
| [README.md](../README.md) | 프로젝트 전체 개요, 핵심 기능, 아키텍처, API | 모든 이해관계자 |
| [INFO.md](INFO.md) | GitHub Issues/PR 자동화 시스템 | 개발자 |
| [ISSUE_GUIDE.md](ISSUE_GUIDE.md) | 이슈 작성 가이드 (Epic, Story, Task 등) | 개발자, PM |
| [PROJECT_SETUP.md](PROJECT_SETUP.md) | AI 어시스턴트 운영 가이드 | AI, 개발자 |

---

### 요구사항 (requirements/)

프로젝트의 비즈니스 요구사항과 기능 명세를 정의합니다.

| 문서 | 설명 | 작성 시점 |
|------|------|-----------|
| [PROJECT_REQUIREMENTS.md](requirements/PROJECT_REQUIREMENTS.md) | 전체 요구사항 명세 | 프로젝트 시작 시 |

**주요 내용:**
- 비즈니스 목표
- 기능 요구사항 (예약 관리, 블랙리스트, 통계)
- 비기능 요구사항 (성능, 보안, 확장성)
- 제약사항 및 가정

---

### 아키텍처 (architecture/)

시스템 설계 및 기술적 의사결정을 문서화합니다.

| 문서 | 설명 | 독자 |
|------|------|------|
| [ARCHITECTURE_DECISION.md](architecture/ARCHITECTURE_DECISION.md) | 아키텍처 패턴 선택 및 비교 분석 | 아키텍트, 시니어 개발자 |
| [DOMAIN_MODEL_DESIGN.md](architecture/DOMAIN_MODEL_DESIGN.md) | 도메인 모델 및 엔티티 설계 | 백엔드 개발자 |
| [TECH_STACK_ANALYSIS.md](architecture/TECH_STACK_ANALYSIS.md) | 기술 스택 선정 이유 및 비교 | 기술 리드, 개발자 |

**아키텍처 의사결정 프로세스:**
```
1. 문제 정의 및 요구사항 분석
2. 대안 비교 (Layered vs Hexagonal vs DDD)
3. 평가 기준 설정 (개발 속도, 유지보수성, 확장성 등)
4. 최종 선택 및 근거 문서화
5. Trade-off 명시
```

---

### API 명세 (api/)

REST API 상세 명세서를 제공합니다.

| 문서 | 설명 | 대상 |
|------|------|------|
| [RESERVATION_API.md](api/RESERVATION_API.md) | 예약 관리 API 명세 | 백엔드/프론트엔드 개발자 |

**주요 내용:**
- HTTP 요청/응답 명세
- 요청/응답 필드 상세 설명
- 에러 응답 형식 및 코드
- cURL 예제
- 비즈니스 로직 설명

---

### 기능별 문서 (features/)

각 도메인 기능의 상세 명세 및 구현 가이드를 제공합니다.

#### 예약 관리 (features/reservation/)

| 문서 | 설명 | 내용 |
|------|------|------|
| [README.md](features/reservation/README.md) | 예약 관리 기능 개요 | 기능 설명, 도메인 모델, API |
| [FLOW.md](features/reservation/FLOW.md) | 예약 프로세스 흐름 | 상태 전이, 이벤트 흐름 |

**주요 주제:**
- 예약 생성 (이벤트 수신 → 쿠폰 검증 → 저장)
- 예약 확정 (결제 완료 → 블랙리스트 체크)
- 예약 거절 (블랙리스트 → 환불 이벤트 발행)
- 예약 조회 (사용자별, 공간별)

#### 블랙리스트 (features/blacklist/)

| 문서 | 설명 | 내용 |
|------|------|------|
| [README.md](features/blacklist/README.md) | 블랙리스트 기능 개요 | 등록, 조회, 자동 거절 로직 |

**주요 주제:**
- 블랙리스트 등록 (공간 운영자)
- 만료 전략 (영구 차단 vs 기간 제한)
- 자동 거절 프로세스
- 향후: 시스템 자동 등록 (노쇼 패턴 분석)

---

## 문서 작성 원칙

### 1. 명확성 (Clarity)

- 기술 용어는 첫 사용 시 정의
- 코드 예제는 실제 구현 가능한 수준
- 다이어그램은 ASCII 또는 Mermaid 사용

### 2. 일관성 (Consistency)

- 용어 통일 (Reservation vs 예약, Place vs 공간)
- 문서 구조 통일 (개요 → 상세 → 예제)
- 코드 스타일: Google Java Style Guide

### 3. 추적성 (Traceability)

- 요구사항 → 아키텍처 → 구현 연결
- 의사결정 근거 명시
- 변경 이력 관리 (Last Updated)

### 4. 이모지 금지

- 모든 문서에서 이모지 사용 금지
- 체크 표시: [x] 사용
- 점수 표시: 4/5 형식

---

## 문서 업데이트 가이드

### 언제 문서를 업데이트하나요?

| 상황 | 업데이트 대상 문서 |
|------|-------------------|
| 새로운 기능 추가 | README.md, PROJECT_REQUIREMENTS.md, features/ |
| 아키텍처 변경 | architecture/ 전체 |
| 기술 스택 변경 | TECH_STACK_ANALYSIS.md, README.md |
| API 변경 | api/, README.md, features/README.md |
| 버그 수정 (중요) | 해당 기능 문서에 Known Issues 추가 |

### 문서 업데이트 체크리스트

- [ ] 변경 사항 명확히 기술
- [ ] 코드 예제 업데이트
- [ ] 관련 문서 간 링크 확인
- [ ] Last Updated 날짜 갱신
- [ ] 버전 정보 업데이트 (해당 시)

---

## 문서 품질 기준

### 필수 요소

- [ ] 제목이 내용을 명확히 설명
- [ ] 목차 제공 (3개 이상 섹션 시)
- [ ] 코드 예제에 주석 포함
- [ ] 실제 사용 가능한 예제
- [ ] 문서 간 링크 동작 확인

### 권장 요소

- [ ] 다이어그램 (아키텍처, 흐름도)
- [ ] 비교 표 (대안 분석 시)
- [ ] FAQ 섹션 (복잡한 주제)
- [ ] 참고 자료 링크

---

## 문서 버전 관리

### 주요 버전 업데이트

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| 0.0.1 | 2025-01-12 | 초기 문서 작성 |

### 문서 변경 원칙

1. **하위 호환성**: 기존 링크 유지
2. **점진적 개선**: 큰 변경은 새 문서 추가
3. **Deprecation 표시**: 구 문서에 경고 추가

---

## 문서 기여 가이드

### 새 문서 추가 시

1. INDEX.md에 문서 등록
2. 적절한 디렉토리에 배치
3. 관련 문서에 링크 추가
4. README.md 문서 섹션 업데이트

### 문서 리뷰 체크리스트

- [ ] 문법 및 맞춤법 확인
- [ ] 코드 예제 동작 검증
- [ ] 링크 유효성 확인
- [ ] 일관성 검토 (용어, 구조)
- [ ] 이모지 사용 여부 확인

---

## 자주 찾는 문서

### 개발 시작

1. [PROJECT_SETUP.md](PROJECT_SETUP.md) - 환경 설정
2. [DOMAIN_MODEL_DESIGN.md](architecture/DOMAIN_MODEL_DESIGN.md) - 엔티티 구조

### 기능 구현

1. [예약 관리 기능](features/reservation/README.md)
2. [블랙리스트 기능](features/blacklist/README.md)

### 아키텍처 이해

1. [ARCHITECTURE_DECISION.md](architecture/ARCHITECTURE_DECISION.md) - 설계 결정
2. [README.md - 아키텍처 섹션](../README.md#아키텍처)

### API 개발

1. [예약 API 명세](api/RESERVATION_API.md) - 상세 API 명세서
2. [README.md - API 엔드포인트](../README.md#api-엔드포인트)
3. 각 기능별 README.md

---

## 문서 관련 문의

문서 관련 문의사항은 다음을 통해 연락 주세요:

- Develop Team Lead: DDing Joo
- Email: ddingsha9@teambind.co.kr

---

Last Updated: 2025-01-17