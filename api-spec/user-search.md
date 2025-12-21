# User Search API

> 유저 검색 API (커서 기반 무한스크롤)

## 목차
- [1. 유저 검색](#1-유저-검색)
- [2. 단과대 목록 조회](#2-단과대-목록-조회)
- [3. 학과 목록 조회 (단과대별)](#3-학과-목록-조회-단과대별)
- [4. 전체 학과 목록 조회](#4-전체-학과-목록-조회)

---

## 1. 유저 검색

단과대, 학과, 이름, 사용자 타입으로 유저를 검색합니다.

### Request
```
GET /api/v1/users/search
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| keyword | string | X | 검색어 (이름) |
| collegeId | long | X | 단과대 ID |
| departmentId | long | X | 학과 ID |
| userType | string | X | 사용자 타입 (STUDENT, PROFESSOR) |
| cursor | long | X | 커서 (이전 응답의 nextCursor) |
| size | int | X | 페이지 크기 (기본: 20) |

### Response
```json
{
  "users": [
    {
      "userId": 2025010001,
      "userNumber": "2025010001",
      "name": "홍길동",
      "userType": "STUDENT",
      "departmentName": "컴퓨터공학과",
      "collegeName": "공과대학",
      "profileImageUrl": "https://storage.example.com/profiles/123.jpg"
    }
  ],
  "nextCursor": 2025010002,
  "hasNext": true,
  "totalCount": 150
}
```

---

## 2. 단과대 목록 조회

유저 검색 필터용 단과대 목록을 조회합니다.

### Request
```
GET /api/v1/users/colleges
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```json
[
  {
    "collegeId": 1,
    "collegeName": "공과대학"
  },
  {
    "collegeId": 2,
    "collegeName": "경영대학"
  }
]
```

---

## 3. 학과 목록 조회 (단과대별)

특정 단과대의 학과 목록을 조회합니다.

### Request
```
GET /api/v1/users/colleges/{collegeId}/departments
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| collegeId | long | 단과대 ID |

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```json
[
  {
    "departmentId": 1,
    "departmentName": "컴퓨터공학과"
  },
  {
    "departmentId": 2,
    "departmentName": "전자공학과"
  }
]
```

---

## 4. 전체 학과 목록 조회

모든 학과 목록을 조회합니다.

### Request
```
GET /api/v1/users/departments
```

### Headers
```
Authorization: Bearer {accessToken}
```

### Response
```json
[
  {
    "departmentId": 1,
    "departmentName": "컴퓨터공학과",
    "collegeName": "공과대학"
  },
  {
    "departmentId": 2,
    "departmentName": "전자공학과",
    "collegeName": "공과대학"
  }
]
```
