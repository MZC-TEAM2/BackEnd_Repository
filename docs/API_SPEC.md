# LMS 백엔드 API 명세서

## 개요
- 기본 URL: `/api`
- 인증 방식: JWT (Bearer Token)
- Content-Type: `application/json`

## 인증 API `/api/auth`

### 1. 이메일 인증 코드 발송
**POST** `/api/auth/signup/email-verification`

이메일 중복 확인 후 인증 코드를 발송합니다.

#### Request
```json
{
  "email": "user@example.com"
}
```

#### Response
**Success (200)**
```json
{
  "success": true,
  "message": "인증 코드가 발송되었습니다."
}
```

**Error (400)**
```json
{
  "success": false,
  "message": "이미 가입된 이메일입니다."
}
```

---

### 2. 인증 코드 확인
**POST** `/api/auth/signup/verify-code`

발송된 이메일 인증 코드를 확인합니다.

#### Request
```json
{
  "email": "user@example.com",
  "code": "12345"  // 5자리 인증 코드
}
```

#### Response
**Success (200)**
```json
{
  "success": true,
  "message": "이메일 인증이 완료되었습니다."
}
```

**Error (400)**
```json
{
  "success": false,
  "message": "인증 코드가 올바르지 않거나 만료되었습니다."
}
```

---

### 3. 회원가입
**POST** `/api/auth/signup`

새로운 사용자를 등록합니다.

#### Request
```json
{
  "email": "user@example.com",
  "password": "Password123!",  // 최소 8자, 영문자, 숫자, 특수문자 포함
  "passwordConfirm": "Password123!",
  "name": "홍길동",
  "userType": "STUDENT",  // STUDENT 또는 PROFESSOR
  "collegeId": 1,
  "departmentId": 1,
  "grade": 3,  // 학생인 경우만 필수 (1~4)
  "phoneNumber": "010-1234-5678"
}
```

#### Response
**Success (201)**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "userId": "20240001"  // 생성된 학번 또는 교번
}
```

**Error (400)**
```json
{
  "success": false,
  "message": "에러 메시지"
}
```

#### 유효성 검증 규칙
- **email**: 필수, 이메일 형식
- **password**: 필수, 최소 8자, 영문자/숫자/특수문자 포함
- **passwordConfirm**: 필수, password와 일치
- **name**: 필수, 2~50자
- **userType**: 필수, STUDENT 또는 PROFESSOR
- **collegeId**: 필수, 유효한 대학 ID
- **departmentId**: 필수, 유효한 학과 ID
- **grade**: 학생인 경우 필수 (1~4)
- **phoneNumber**: 필수, 한국 휴대폰 번호 형식

---

### 4. 로그인
**POST** `/api/auth/login`

사용자 인증 후 토큰을 발급합니다.

#### Request
```json
{
  "username": "user@example.com",  // 이메일 또는 학번/교번
  "password": "Password123!",
  "deviceInfo": "Chrome/Windows"  // 선택사항
}
```

#### Response
**Success (200)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR...",
  "userType": "STUDENT",
  "userNumber": "20240001",
  "name": "홍길동",
  "email": "user@example.com",
  "userId": "20240001"
}
```

**Error (400)**
```json
{
  "success": false,
  "message": "아이디 또는 비밀번호가 올바르지 않습니다."
}
```

---

### 5. 토큰 갱신
**POST** `/api/auth/refresh`

리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.

#### Request
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR..."
}
```

#### Response
**Success (200)**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR..."
}
```

**Error (401)**
```json
{
  "success": false,
  "message": "유효하지 않은 토큰입니다."
}
```

---

### 6. 로그아웃
**POST** `/api/auth/logout`

리프레시 토큰을 무효화하여 로그아웃 처리합니다.

#### Request Headers
```
Refresh-Token: eyJhbGciOiJIUzI1NiIsInR...
```

#### Response
**Success (200)**
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

> **Note**: 로그아웃은 실패하더라도 항상 성공 응답을 반환합니다.

---

### 7. 이메일 중복 확인
**GET** `/api/auth/check-email`

회원가입 전 이메일 사용 가능 여부를 확인합니다.

#### Query Parameters
- `email`: 확인할 이메일 주소

#### Example Request
```
GET /api/auth/check-email?email=user@example.com
```

#### Response
**Success (200)**
```json
{
  "available": true,
  "message": "사용 가능한 이메일입니다."
}
```

또는

```json
{
  "available": false,
  "message": "이미 사용 중인 이메일입니다."
}
```

---

## 공통 에러 응답

### 500 Internal Server Error
서버 내부 오류가 발생한 경우

```json
{
  "success": false,
  "message": "처리 중 오류가 발생했습니다."
}
```

---

## 인증 토큰 사용 방법

### Request Header
보호된 리소스에 접근할 때는 다음과 같이 Authorization 헤더에 토큰을 포함해야 합니다:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR...
```

### 토큰 만료 시 처리
1. Access Token 만료 시: 401 응답을 받게 됩니다.
2. Refresh Token을 사용하여 `/api/auth/refresh` 엔드포인트로 새 토큰을 요청합니다.
3. 새로 발급받은 Access Token으로 다시 요청합니다.

---

## 보안 고려사항

1. **비밀번호 정책**
   - 최소 8자 이상
   - 영문자, 숫자, 특수문자 포함 필수

2. **토큰 관리**
   - Access Token: 짧은 유효기간 (예: 15분)
   - Refresh Token: 긴 유효기간 (예: 7일)
   - 로그아웃 시 Refresh Token 무효화

3. **IP 주소 추적**
   - 로그인 시 클라이언트 IP 주소 자동 기록
   - 프록시 헤더 지원 (X-Forwarded-For, X-Real-IP 등)

4. **이메일 인증**
   - 회원가입 전 이메일 인증 필수
   - 5자리 인증 코드 사용
   - 인증 코드 유효시간 제한