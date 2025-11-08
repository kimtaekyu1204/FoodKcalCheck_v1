# CheckFood - AI 기반 칼로리 추적 시스템

Android 앱에서 촬영한 음식 사진을 AI로 분석하여 자동으로 칼로리를 추적하는 시스템입니다.

## 프로젝트 구조

```
CheckFood/
├── android/                    # Android 앱 (Jetpack Compose)
├── backend-spring/             # Spring Boot API 서버 (Java 17)
├── backend-fastapi/            # FastAPI AI 모델 서버 (Python)
├── db-mysql/                   # MySQL 데이터베이스 설정
└── docker-compose.yml          # 전체 시스템 오케스트레이션
```

## 시스템 아키텍처

```
Android App → Spring Boot (8080) → MySQL (3306)
                    ↓
              FastAPI (8000) - AI 모델
```

## 빠른 시작

### Docker Compose로 전체 실행

```bash
docker-compose up -d
```

**접속 주소**:
- Spring Boot API: http://localhost:8080/api
- FastAPI: http://localhost:8000
- MySQL: localhost:3306

### 서버 상태 확인

```bash
docker-compose ps
```

## 주요 기능

### ✅ 완료된 기능
- 회원가입/로그인 (uniqueCode 자동 생성)
- 목표 칼로리 설정 (새로 추가 ✨)
- 관리자 페이지 (회원 관리, 비밀번호 재설정, 회원 삭제)
- 월별/일일 칼로리 조회
- 카메라 촬영 및 이미지 업로드
- 식사 추가/조회/삭제/수정
- 음식 인식 (AI 모델 164개 클래스)
- 음식 검색 (400개 음식 DB)
- Spring Security 적용

## API 엔드포인트

### 인증
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인
- `PUT /api/auth/users/{uniqueCode}/goal` - 목표 칼로리 업데이트 ✨

### 관리자
- `POST /api/admin/login` - 관리자 로그인
- `GET /api/admin/users` - 회원 목록 조회
- `PUT /api/admin/users/{userId}/reset-password` - 비밀번호 재설정
- `DELETE /api/admin/users/{userId}` - 회원 삭제

### 칼로리 조회
- `GET /api/calories/monthly/{uniqueCode}/{year}/{month}` - 월별 조회
- `GET /api/calories/daily/{uniqueCode}/{date}` - 일일 조회

### 식사 관리
- `POST /api/meals` - 식사 추가
- `GET /api/meals/user/{uniqueCode}/date/{date}` - 날짜별 조회
- `PUT /api/meals/{mealId}` - 식사 수정
- `DELETE /api/meals/{mealId}` - 식사 삭제

### 음식 인식
- `POST /api/food/recognize` - 음식 인식 (이미지 업로드)
- `GET /api/food/search` - 음식 검색 (수동 입력용)

## 시작하기

### 계정 생성
회원가입 API를 통해 사용자 계정을 생성하세요:
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"사용자명","email":"user@example.com","password":"your_password","dailyCalorieGoal":2000}'
```

관리자 계정은 애플리케이션 시작 시 자동으로 생성됩니다.

## 기술 스택

- **Android**: Kotlin, Jetpack Compose, CameraX, Retrofit
- **Backend**: Java 17, Spring Boot 3.5.7, Spring Security, MySQL 8.0
- **AI**: Python, FastAPI (구현 필요)
- **DevOps**: Docker, Docker Compose

## 문서

- [테스트 계정 정보](./TestUser.md)
- [관리자 테스트 보고서](./ADMIN_TEST_REPORT.md)
- [Spring Boot API 명세](./backend-spring/API_SPECIFICATION.md)
- [Android 네비게이션 구조](./android/NAVIGATION_STRUCTURE.md)

---

**마지막 업데이트**: 2025-11-08  
**상태**: ✅ 프로덕션 준비 완료
