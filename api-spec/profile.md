# Profile API

> 프로필 관리 API

## 목차
- [1. 내 프로필 조회](#1-내-프로필-조회)
- [2. 프로필 수정](#2-프로필-수정)
- [3. 프로필 이미지 업로드](#3-프로필-이미지-업로드)
- [4. 프로필 이미지 삭제](#4-프로필-이미지-삭제)

---

## 1. 내 프로필 조회

로그인한 사용자의 프로필 정보를 조회합니다.

### Request
```
GET /api/v1/profile/me
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```json
{
  "userId": 2025010001,
  "userNumber": "2025010001",
  "name": "홍길동",
  "email": "hong@example.com",
  "userType": "STUDENT",
  "phoneNumber": "010-1234-5678",
  "departmentName": "컴퓨터공학과",
  "collegeName": "공과대학",
  "profileImageUrl": "https://storage.example.com/profiles/123.jpg",
  "createdAt": "2025-01-01T00:00:00"
}
```

---

## 2. 프로필 수정

프로필 정보를 수정합니다.

### Request
```
PATCH /api/v1/profile/me
```

### Headers
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### Request Body
```json
{
  "phoneNumber": "010-9876-5432",
  "introduce": "안녕하세요. 컴퓨터공학과 학생입니다."
}
```

### Response
```json
{
  "userId": 2025010001,
  "userNumber": "2025010001",
  "name": "홍길동",
  "email": "hong@example.com",
  "userType": "STUDENT",
  "phoneNumber": "010-9876-5432",
  "introduce": "안녕하세요. 컴퓨터공학과 학생입니다.",
  "departmentName": "컴퓨터공학과",
  "collegeName": "공과대학",
  "profileImageUrl": "https://storage.example.com/profiles/123.jpg"
}
```

---

## 3. 프로필 이미지 업로드

프로필 이미지를 업로드합니다.

### Request
```
POST /api/v1/profile/me/image
```

### Headers
```
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

### Request Body (Form Data)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| image | file | O | 이미지 파일 (jpg, png, gif) |

### Response
```
HTTP/1.1 202 Accepted
```

> 이미지는 비동기로 처리되며, 처리 완료 후 프로필 조회 시 반영됩니다.

---

## 4. 프로필 이미지 삭제

프로필 이미지를 삭제합니다.

### Request
```
DELETE /api/v1/profile/me/image
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```
HTTP/1.1 200 OK
```
