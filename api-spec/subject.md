# Subject API

> 과목 관리 API

## 목차
- [1. 과목 목록 조회](#1-과목-목록-조회)
- [2. 과목 상세 조회](#2-과목-상세-조회)
- [3. 과목 검색](#3-과목-검색)

---

## 1. 과목 목록 조회

과목 목록을 조회합니다.

### Request
```
GET /api/v1/subjects
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| page | int | X | 0 | 페이지 번호 |
| size | int | X | 20 | 페이지 크기 |
| keyword | string | X | - | 검색어 (과목명, 과목코드) |
| departmentId | long | X | - | 학과 ID |
| showAllDepartments | boolean | X | false | 전체 학과 표시 |
| courseType | string | X | - | 강의 유형 |
| credits | int | X | - | 학점 |
| isActive | boolean | X | true | 활성 과목만 조회 |

### Response
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "subjectId": 1,
        "subjectCode": "CS101",
        "subjectName": "자료구조",
        "departmentName": "컴퓨터공학과",
        "credits": 3,
        "courseType": "전공필수",
        "description": "자료구조의 기본 개념"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "currentPage": 0,
    "size": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

## 2. 과목 상세 조회

과목 상세 정보를 조회합니다.

### Request
```
GET /api/v1/subjects/{subjectId}
```

### Path Parameters
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| subjectId | long | 과목 ID |

### Response
```json
{
  "success": true,
  "data": {
    "subjectId": 1,
    "subjectCode": "CS101",
    "subjectName": "자료구조",
    "departmentId": 1,
    "departmentName": "컴퓨터공학과",
    "credits": 3,
    "courseType": "전공필수",
    "description": "자료구조의 기본 개념과 알고리즘을 학습합니다.",
    "prerequisiteSubjects": [
      {
        "subjectId": 2,
        "subjectName": "프로그래밍 기초"
      }
    ],
    "isActive": true
  }
}
```

---

## 3. 과목 검색

과목을 검색합니다. (페이징 지원)

### Request
```
GET /api/v1/subjects/search
```

### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| q | string | O | - | 검색어 |
| page | int | X | 0 | 페이지 번호 |
| size | int | X | 20 | 페이지 크기 (최대 50) |

### Response
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "subjectId": 1,
        "subjectCode": "CS101",
        "subjectName": "자료구조",
        "departmentName": "컴퓨터공학과",
        "credits": 3
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "currentPage": 0,
    "size": 20,
    "hasNext": false,
    "hasPrevious": false
  }
}
```
