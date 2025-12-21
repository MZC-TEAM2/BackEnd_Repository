# Auth API

> 인증/인가 관련 API

## 목차
- [1. 이메일 인증 코드 발송](#1-이메일-인증-코드-발송)
- [2. 이메일 인증 코드 확인](#2-이메일-인증-코드-확인)
- [3. 회원가입](#3-회원가입)
- [4. 로그인](#4-로그인)
- [5. 토큰 갱신](#5-토큰-갱신)
- [6. 로그아웃](#6-로그아웃)
- [7. 이메일 중복 확인](#7-이메일-중복-확인)

---

## 1. 이메일 인증 코드 발송

회원가입을 위한 이메일 인증 코드를 발송합니다.

### Request
```
POST /api/auth/signup/email-verification
```

### Request Body
```json
{
  "email": "user@example.com"
}
```

### Response
```json
{
  "success": true,
  "message": "인증 코드가 발송되었습니다."
}
```

### Error Response
| 상태 코드 | 메시지 |
|-----------|--------|
| 400 | 이미 가입된 이메일입니다. |
| 500 | 인증 코드 발송에 실패했습니다. |

---

## 2. 이메일 인증 코드 확인

발송된 인증 코드를 확인합니다.

### Request
```
POST /api/auth/signup/verify-code
```

### Request Body
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

### Response
```json
{
  "success": true,
  "message": "이메일 인증이 완료되었습니다."
}
```

### Error Response
| 상태 코드 | 메시지 |
|-----------|--------|
| 400 | 인증 코드가 올바르지 않거나 만료되었습니다. |

---

## 3. 회원가입

새로운 사용자를 등록합니다.

### Request
```
POST /api/auth/signup
```

### Request Body
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "userType": "STUDENT",
  "departmentId": 1,
  "phoneNumber": "010-1234-5678"
}
```

### Response
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "userId": "2025010001"
}
```

### Error Response
| 상태 코드 | 메시지 |
|-----------|--------|
| 400 | 이메일 인증이 필요합니다. |
| 400 | 이미 가입된 이메일입니다. |

---

## 4. 로그인

사용자 인증 후 JWT 토큰을 발급합니다.

### Request
```
POST /api/auth/login
```

### Request Body
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userType": "STUDENT",
  "userId": 2025010001,
  "userName": "홍길동"
}
```

### Error Response
| 상태 코드 | 메시지 |
|-----------|--------|
| 400 | 이메일 또는 비밀번호가 올바르지 않습니다. |

---

## 5. 토큰 갱신

Refresh Token을 사용하여 새로운 Access Token을 발급합니다.

### Request
```
POST /api/auth/refresh
```

### Request Body
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Error Response
| 상태 코드 | 메시지 |
|-----------|--------|
| 401 | 유효하지 않은 Refresh Token입니다. |
| 401 | 만료된 Refresh Token입니다. |

---

## 6. 로그아웃

사용자 로그아웃 처리 (Refresh Token 무효화)

### Request
```
POST /api/auth/logout
```

### Headers
```
Refresh-Token: {refreshToken}
```

### Response
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

---

## 7. 이메일 중복 확인

이메일 사용 가능 여부를 확인합니다.

### Request
```
GET /api/auth/check-email?email={email}
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| email | string | O | 확인할 이메일 |

### Response
```json
{
  "available": true,
  "message": "사용 가능한 이메일입니다."
}
```

```json
{
  "available": false,
  "message": "이미 사용 중인 이메일입니다."
}
```
