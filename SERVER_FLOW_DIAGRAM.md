# CheckFood 서버 흐름도

**작성일:** 2025-11-08
**버전:** 2.0
**시스템:** Android + Spring Boot + FastAPI + MySQL

---

## 📋 목차

1. [전체 시스템 아키텍처](#전체-시스템-아키텍처)
2. [서비스 구성](#서비스-구성)
3. [음식 인식 플로우](#음식-인식-플로우)
4. [식사 저장 플로우](#식사-저장-플로우)
5. [학습 데이터 수집 플로우](#학습-데이터-수집-플로우)
6. [수동 입력 플로우](#수동-입력-플로우)
7. [캘린더 조회 플로우](#캘린더-조회-플로우)
8. [관리자 기능 플로우](#관리자-기능-플로우)
9. [데이터베이스 구조](#데이터베이스-구조)
10. [에러 처리 플로우](#에러-처리-플로우)

---

## 전체 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                        Android Application                       │
│  - Jetpack Compose UI                                           │
│  - Retrofit2 (HTTP Client)                                      │
│  - CameraX (카메라)                                              │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ HTTP/REST API
             │ Port: 8080
             ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                           │
│  - REST API Controller                                          │
│  - Business Logic Service                                       │
│  - JPA/Hibernate (ORM)                                          │
│  - WebClient (FastAPI 통신)                                      │
└────┬───────────────────────────────────────────────────┬────────┘
     │                                                     │
     │ JDBC                                                │ HTTP
     │ Port: 3306                                         │ Port: 8000
     ↓                                                     ↓
┌────────────────────┐                      ┌──────────────────────┐
│   MySQL Database   │                      │   FastAPI Backend    │
│  - users           │                      │  - AI 모델 추론      │
│  - meals           │                      │  - ONNX Runtime      │
│  - admins          │                      │  - CSV 영양 DB       │
│  - training_data   │                      │  - 이미지 전처리     │
└────────────────────┘                      └──────────────────────┘
     ↑                                                     ↑
     │                                                     │
     └─────────────────────────────────────────────────────┘
                   Docker Network: checkfood-network
```

---

## 서비스 구성

### 1. Android Application
- **역할:** 사용자 인터페이스 및 클라이언트
- **기술 스택:** Kotlin, Jetpack Compose, Retrofit2, CameraX
- **주요 기능:**
  - 카메라로 음식 촬영
  - 음식 인식 결과 표시
  - 수동 음식 입력
  - 캘린더 식사 기록 조회

### 2. Spring Boot Backend
- **역할:** 메인 백엔드 서버
- **포트:** 8080
- **기술 스택:** Spring Boot 3.x, JPA/Hibernate, WebClient
- **주요 기능:**
  - 사용자 인증/관리
  - 식사 기록 CRUD
  - FastAPI와 통신 (AI 모델 호출)
  - 학습 데이터 수집
  - 이미지 저장

### 3. FastAPI Backend
- **역할:** AI 모델 추론 서버
- **포트:** 8000
- **기술 스택:** Python, FastAPI, ONNX Runtime
- **주요 기능:**
  - 음식 이미지 인식 (AI 모델)
  - 음식 칼로리 검색 (CSV)
  - 하이브리드 칼로리 처리

### 4. MySQL Database
- **역할:** 데이터 영구 저장
- **포트:** 3306
- **주요 테이블:**
  - `users` - 사용자 정보
  - `meals` - 식사 기록
  - `admins` - 관리자 계정
  - `training_data_log` - AI 학습 데이터

---

## 음식 인식 플로우

### 1. 카메라 촬영 → AI 인식

```
┌──────────┐
│ Android  │
│  Camera  │
└────┬─────┘
     │ 1. 사진 촬영
     │    촬영된 이미지 저장
     ↓
┌──────────────────────────────────────────┐
│ CameraDetectionPage.kt                   │
│  - imageUri 전달받음                      │
│  - LaunchedEffect로 자동 API 호출        │
└────┬─────────────────────────────────────┘
     │
     │ 2. POST /api/food/recognize
     │    - image: MultipartFile
     │    - userUniqueCode: String
     ↓
┌──────────────────────────────────────────┐
│ Spring Boot                              │
│ FoodRecognitionController.recognizeFood()│
└────┬─────────────────────────────────────┘
     │
     │ 3. 이미지 파일 검증
     │    - 파일 비어있는지 확인
     │    - 크기 검증 (10MB 제한)
     │    - 파일 타입 검증 (image/*)
     ↓
┌──────────────────────────────────────────┐
│ FoodRecognitionService.recognizeFood()  │
└────┬─────────────────────────────────────┘
     │
     │ 4. WebClient로 FastAPI 호출
     │    POST http://backend-fastapi:8000/api/v1/food/recognize
     │    - image: MultipartFile
     ↓
┌──────────────────────────────────────────┐
│ FastAPI                                  │
│ main.py - recognize_food()               │
└────┬─────────────────────────────────────┘
     │
     │ 5. 이미지 전처리
     │    - PIL로 이미지 로드
     │    - 384x384 리사이즈
     │    - ImageNet 정규화
     │    - float32 변환
     ↓
┌──────────────────────────────────────────┐
│ FoodRecognitionModel                     │
│  - ONNX Runtime                          │
│  - food_recognition_model.onnx (70MB)    │
│  - 164개 음식 클래스                      │
└────┬─────────────────────────────────────┘
     │
     │ 6. AI 모델 추론
     │    Input: [1, 3, 384, 384]
     │    Output: Top-3 음식 예측
     │    - food1_name, food1_calories
     │    - food2_name, food2_calories
     │    - food3_name, food3_calories
     ↓
┌──────────────────────────────────────────┐
│ 하이브리드 칼로리 처리                    │
│  1. CSV 조회 (우선)                      │
│     - foodKcalList.csv (400개 음식)      │
│  2. 모델 예측 (fallback)                 │
│     - CSV에 없으면 모델 칼로리 사용       │
└────┬─────────────────────────────────────┘
     │
     │ 7. 응답 생성
     │    {
     │      "success": true,
     │      "food_count": 2,
     │      "food1_name": "쌀밥",
     │      "food1_calories": 334,
     │      "food2_name": "된장찌개",
     │      "food2_calories": 147,
     │      "total_calories": 481
     │    }
     ↓
Spring Boot → Android
     ↓
┌──────────────────────────────────────────┐
│ CameraDetectionPage.kt                   │
│  - 인식 결과 표시                         │
│  - 총 칼로리 표시                         │
│  - "확인 및 저장" 버튼 활성화             │
└──────────────────────────────────────────┘
```

---

## 식사 저장 플로우

### 2. 인식 결과 확인 → 식사 저장

```
┌──────────────────────────────────────────┐
│ Android - CameraDetectionPage.kt         │
│  사용자가 "확인 및 저장" 버튼 클릭        │
└────┬─────────────────────────────────────┘
     │
     │ 1. MealRequest 생성
     │    - userUniqueCode
     │    - mealDate (현재 날짜)
     │    - mealTime (현재 시간)
     │    - mealType (시간대별 자동 결정)
     │    - foodCount, food1_name, food1_calories, ...
     ↓
     │ 2-A. 기존 방식 (학습 데이터 수집 안 됨)
     │    POST /api/meals
     │    Body: JSON (MealRequest)
     │
     │ 2-B. 새로운 방식 (학습 데이터 수집 포함)
     │    POST /api/meals/with-training-data
     │    - image: MultipartFile
     │    - aiPrediction: JSON (AI 예측 결과)
     │    - mealRequest: JSON
     ↓
┌──────────────────────────────────────────┐
│ Spring Boot - MealController             │
└────┬─────────────────────────────────────┘
     │
     ├─ 2-A 경로 (기존)
     │  └→ createMeal(@RequestBody MealRequest)
     │     ↓
     │     MealService.createMeal()
     │     ↓
     │     Meal 엔티티 생성 → DB 저장
     │     ↓
     │     완료
     │
     └─ 2-B 경로 (학습 데이터 포함)
        └→ createMealWithTrainingData(image, aiPrediction, mealRequest)
           ↓
        ┌──────────────────────────────────────────┐
        │ 3. JSON 파싱                             │
        │    - mealRequest → MealRequest 객체      │
        │    - aiPrediction → Map<String, Object>  │
        └────┬─────────────────────────────────────┘
             │
             │ 4. Meal 생성
             ↓
        ┌──────────────────────────────────────────┐
        │ MealService.createMeal()                 │
        │  - User 존재 확인                        │
        │  - Meal 엔티티 생성                      │
        │  - totalCalories 계산                    │
        │  - DB 저장                               │
        └────┬─────────────────────────────────────┘
             │
             │ 5. MealResponse 반환 (ID 포함)
             │    Meal ID: 4
             ↓
        ┌──────────────────────────────────────────┐
        │ 6. 사용자 수정 데이터 생성 (Ground Truth)│
        │    {                                     │
        │      "food_count": 2,                    │
        │      "food1": {                          │
        │        "name": "쌀밥",                    │
        │        "calories": 334                   │
        │      },                                  │
        │      "food2": {                          │
        │        "name": "된장찌개",                │
        │        "calories": 147                   │
        │      }                                   │
        │    }                                     │
        └────┬─────────────────────────────────────┘
             │
             │ 7. 학습 데이터 저장 (비동기)
             ↓
        ┌──────────────────────────────────────────┐
        │ TrainingDataService.saveTrainingData()   │
        └────┬─────────────────────────────────────┘
             │
             │ 8. 이미지 저장
             ↓
        ┌──────────────────────────────────────────┐
        │ ImageStorageService.saveImage()          │
        │  - 디렉토리 생성                         │
        │    /app/training_images/{user}/{yyyy}/{MM}/{dd}/│
        │  - 파일명 생성                           │
        │    {uuid}_{timestamp}.{ext}              │
        │  - 파일 저장 (Docker Volume)             │
        └────┬─────────────────────────────────────┘
             │
             │ 9. TrainingDataLog 저장
             ↓
        ┌──────────────────────────────────────────┐
        │ MySQL - training_data_log 테이블         │
        │  - log_id (AUTO_INCREMENT)               │
        │  - user_unique_code                      │
        │  - meal_id (FK → meals.id)               │
        │  - image_path                            │
        │  - ai_prediction (JSON)                  │
        │  - user_corrected_json (JSON)            │
        │  - created_at                            │
        └────┬─────────────────────────────────────┘
             │
             │ 10. 성공 응답 반환
             ↓
        Android
             ↓
        ┌──────────────────────────────────────────┐
        │ 식사 저장 완료                            │
        │  - Calendar 페이지로 이동                 │
        └──────────────────────────────────────────┘
```

---

## 학습 데이터 수집 플로우

### AI 모델 재학습용 데이터 수집

```
┌──────────────────────────────────────────────────────────┐
│              학습 데이터 수집 플로우                       │
└──────────────────────────────────────────────────────────┘

1. 사용자가 카메라로 음식 촬영
   ↓
2. AI 모델이 음식 인식 (예측)
   {
     "food1_name": "쌀밥",
     "food1_calories": 310,    ← AI 예측 (부정확할 수 있음)
     "food2_name": "된장찌개",
     "food2_calories": 140     ← AI 예측 (부정확할 수 있음)
   }
   ↓
3. 사용자가 결과 확인 및 수정
   - 음식 이름 수정 가능
   - 칼로리 수정 가능 (CSV 검색 기능 제공)
   ↓
4. 사용자가 "저장" 버튼 클릭
   ↓
5. Spring Boot가 3가지 데이터 수집
   ┌────────────────────────────────────────┐
   │ A. 원본 이미지 파일                     │
   │    → Docker Volume 영구 저장            │
   │    → /app/training_images/...           │
   └────────────────────────────────────────┘
   ┌────────────────────────────────────────┐
   │ B. AI 예측 결과 (Before)                │
   │    → MySQL JSON 저장                    │
   │    → ai_prediction 컬럼                 │
   │    {                                   │
   │      "food1_calories": 310,  ← 오답    │
   │      "food2_calories": 140   ← 오답    │
   │    }                                   │
   └────────────────────────────────────────┘
   ┌────────────────────────────────────────┐
   │ C. 사용자 수정 정보 (After - 정답)      │
   │    → MySQL JSON 저장                    │
   │    → user_corrected_json 컬럼           │
   │    {                                   │
   │      "food1": {"calories": 334}, ← 정답│
   │      "food2": {"calories": 147}  ← 정답│
   │    }                                   │
   └────────────────────────────────────────┘
   ↓
6. AI 모델팀이 데이터 활용
   - 이미지 + Ground Truth로 재학습
   - AI 예측 vs 실제 정답 비교 분석
   - 오답률이 높은 음식 카테고리 파악
   - 모델 성능 개선
```

---

## 수동 입력 플로우

### 사용자가 직접 음식 입력

```
┌──────────────────────────────────────────┐
│ Android - CameraPage.kt                  │
│  사용자가 "수동" 버튼 클릭                │
└────┬─────────────────────────────────────┘
     │
     │ navigate("manual_input")
     ↓
┌──────────────────────────────────────────┐
│ ManualInputPage.kt                       │
│  - 음식명 입력 TextField                 │
│  - 칼로리 입력 TextField                 │
│  - 검색 버튼 (돋보기 아이콘)             │
└────┬─────────────────────────────────────┘
     │
     │ 사용자가 음식명 입력 후 검색
     │ (예: "쌀밥")
     ↓
     │ GET /api/food/search?foodName=쌀밥
     ↓
┌──────────────────────────────────────────┐
│ Spring Boot                              │
│ FoodRecognitionController.searchFood()   │
└────┬─────────────────────────────────────┘
     │
     │ FoodRecognitionService.searchFood()
     ↓
     │ POST http://backend-fastapi:8000/api/v1/food/search
     │    ?foodName=쌀밥
     ↓
┌──────────────────────────────────────────┐
│ FastAPI                                  │
│ main.py - search_food()                  │
└────┬─────────────────────────────────────┘
     │
     │ NutritionDatabase.get_nutrition_info()
     ↓
┌──────────────────────────────────────────┐
│ foodKcalList.csv 조회                    │
│  - 400개 음식 중 검색                     │
│  - 음식명 일치 여부 확인                  │
└────┬─────────────────────────────────────┘
     │
     │ 쌀밥 → 334kcal (발견)
     ↓
     │ 응답
     │ {
     │   "foodName": "쌀밥",
     │   "calories": 334
     │ }
     ↓
Spring Boot → Android
     ↓
┌──────────────────────────────────────────┐
│ ManualInputPage.kt                       │
│  - 검색 결과 다이얼로그 표시              │
│  - "사용하기" 버튼                        │
│    → 칼로리 자동 입력                     │
│  - "닫기" 버튼                            │
└────┬─────────────────────────────────────┘
     │
     │ 사용자가 "저장" 버튼 클릭
     ↓
     │ POST /api/meals
     │ Body: {
     │   "userUniqueCode": "...",
     │   "mealDate": "2025-11-08",
     │   "mealTime": "12:30:00",
     │   "mealType": "LUNCH",
     │   "foodCount": 1,
     │   "food1Name": "쌀밥",
     │   "food1Calories": 334
     │ }
     ↓
Spring Boot → MealService.createMeal()
     ↓
MySQL - meals 테이블 저장
     ↓
완료 → Calendar 페이지로 이동
```

---

## 캘린더 조회 플로우

### 일별 식사 기록 조회

```
┌──────────────────────────────────────────┐
│ Android - CalendarPage.kt                │
│  사용자가 특정 날짜 클릭                  │
│  (예: 2025년 11월 8일)                   │
└────┬─────────────────────────────────────┘
     │
     │ GET /api/meals/user/{uniqueCode}/date/{date}
     │ GET /api/meals/user/TESTUSER01/date/2025-11-08
     ↓
┌──────────────────────────────────────────┐
│ Spring Boot                              │
│ MealController.getMealsByDate()          │
└────┬─────────────────────────────────────┘
     │
     │ MealService.getMealsByUserCodeAndDate()
     ↓
┌──────────────────────────────────────────┐
│ MealRepository                           │
│ findByUserUniqueCodeAndMealDate()        │
└────┬─────────────────────────────────────┘
     │
     │ SQL: SELECT * FROM meals
     │      WHERE user_unique_code = 'TESTUSER01'
     │        AND meal_date = '2025-11-08'
     │      ORDER BY meal_time;
     ↓
┌──────────────────────────────────────────┐
│ MySQL - meals 테이블                     │
│  결과:                                   │
│  - 아침: 쌀밥, 김치, ... (500kcal)       │
│  - 점심: 쌀밥, 된장찌개 (481kcal)        │
│  - 저녁: 삼겹살, ... (850kcal)           │
└────┬─────────────────────────────────────┘
     │
     │ List<Meal> → List<MealResponse> 변환
     ↓
     │ 응답
     │ [
     │   {
     │     "id": 1,
     │     "mealType": "BREAKFAST",
     │     "mealTypeKorean": "아침",
     │     "totalCalories": 500,
     │     "food1Name": "쌀밥",
     │     ...
     │   },
     │   {
     │     "id": 4,
     │     "mealType": "LUNCH",
     │     "mealTypeKorean": "점심",
     │     "totalCalories": 481,
     │     "food1Name": "쌀밥",
     │     "food1Calories": 334,
     │     "food2Name": "된장찌개",
     │     "food2Calories": 147
     │   },
     │   ...
     │ ]
     ↓
Android - CalendarPage.kt
     ↓
┌──────────────────────────────────────────┐
│ 화면 표시                                 │
│  ┌────────────────────────────────────┐ │
│  │ 2025년 11월 8일                    │ │
│  │ 총 섭취 칼로리: 1831 kcal          │ │
│  ├────────────────────────────────────┤ │
│  │ [아침] 500 kcal                    │ │
│  │  - 쌀밥, 김치, ...                 │ │
│  ├────────────────────────────────────┤ │
│  │ [점심] 481 kcal                    │ │
│  │  - 쌀밥 (334kcal)                  │ │
│  │  - 된장찌개 (147kcal)              │ │
│  ├────────────────────────────────────┤ │
│  │ [저녁] 850 kcal                    │ │
│  │  - 삼겹살, ...                     │ │
│  └────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

---

## 관리자 기능 플로우

### 관리자 로그인 및 사용자 관리

```
┌──────────────────────────────────────────┐
│ Android - AdminLoginPage.kt              │
│  관리자 ID/PW 입력                        │
└────┬─────────────────────────────────────┘
     │
     │ POST /api/admin/login
     │ Body: {
     │   "username": "admin",
     │   "password": "admin1234"
     │ }
     ↓
┌──────────────────────────────────────────┐
│ Spring Boot - AdminController            │
│ login()                                  │
└────┬─────────────────────────────────────┘
     │
     │ AdminService.login()
     ↓
┌──────────────────────────────────────────┐
│ AdminRepository.findByUsername()         │
└────┬─────────────────────────────────────┘
     │
     │ MySQL - admins 테이블 조회
     ↓
     │ BCrypt 비밀번호 검증
     │ passwordEncoder.matches(inputPw, storedPw)
     ↓
     │ 성공 → AdminResponse 반환
     │ {
     │   "id": 1,
     │   "username": "admin",
     │   "message": "로그인 성공"
     │ }
     ↓
Android - AdminPage.kt로 이동
     ↓
┌──────────────────────────────────────────┐
│ 사용자 목록 조회                          │
│ GET /api/admin/users                     │
└────┬─────────────────────────────────────┘
     │
     │ AdminService.getAllUsers()
     ↓
     │ MySQL - users 테이블 전체 조회
     │ SELECT * FROM users ORDER BY created_at DESC;
     ↓
     │ 응답: List<User>
     ↓
Android에 사용자 목록 표시
     ↓
┌──────────────────────────────────────────┐
│ 사용자 삭제 (관리자 기능)                 │
│ DELETE /api/admin/users/{uniqueCode}     │
└────┬─────────────────────────────────────┘
     │
     │ AdminService.deleteUser()
     ↓
     │ 1. meals 테이블에서 해당 유저 식사 삭제
     │    DELETE FROM meals WHERE user_unique_code = ?
     │
     │ 2. users 테이블에서 유저 삭제
     │    DELETE FROM users WHERE unique_code = ?
     ↓
완료
```

---

## 데이터베이스 구조

### ER 다이어그램

```
┌─────────────────────┐
│      admins         │
├─────────────────────┤
│ id (PK)             │
│ username (UNIQUE)   │
│ password            │
│ created_at          │
└─────────────────────┘

┌─────────────────────────────────────────┐
│               users                     │
├─────────────────────────────────────────┤
│ id (PK)                                 │
│ unique_code (UNIQUE)  ← 10자리 랜덤코드 │
│ name                                    │
│ email (UNIQUE)                          │
│ password                                │
│ daily_calorie_goal                      │
│ created_at                              │
│ updated_at                              │
└─────────────┬───────────────────────────┘
              │
              │ 1:N
              ↓
┌─────────────────────────────────────────┐
│               meals                     │
├─────────────────────────────────────────┤
│ id (PK)                                 │
│ user_unique_code (FK) → users           │
│ meal_date                               │
│ meal_time                               │
│ meal_type (BREAKFAST, LUNCH, ...)       │
│ food_count (1~3)                        │
│ food1_name, food1_calories              │
│ food2_name, food2_calories              │
│ food3_name, food3_calories              │
│ total_calories                          │
│ created_at                              │
│ updated_at                              │
└─────────────┬───────────────────────────┘
              │
              │ 1:N
              ↓
┌─────────────────────────────────────────┐
│        training_data_log                │
├─────────────────────────────────────────┤
│ log_id (PK)                             │
│ user_unique_code                        │
│ meal_id (FK) → meals.id                 │
│ image_path                              │
│ ai_prediction (JSON)                    │
│   {                                     │
│     "food1_name": "쌀밥",               │
│     "food1_calories": 310,  ← AI 예측  │
│     ...                                 │
│   }                                     │
│ user_corrected_json (JSON)              │
│   {                                     │
│     "food1": {                          │
│       "name": "쌀밥",                   │
│       "calories": 334  ← 사용자 수정    │
│     },                                  │
│     ...                                 │
│   }                                     │
│ created_at                              │
└─────────────────────────────────────────┘
```

### 테이블 설명

#### 1. users 테이블
- **용도:** 사용자 정보 관리
- **인덱스:**
  - `unique_code` (UNIQUE)
  - `email` (UNIQUE)

#### 2. meals 테이블
- **용도:** 식사 기록 저장
- **인덱스:**
  - `user_unique_code, meal_date` (복합 인덱스)
- **최대 음식 개수:** 3개 (food1, food2, food3)

#### 3. training_data_log 테이블
- **용도:** AI 모델 재학습용 데이터 수집
- **인덱스:**
  - `user_unique_code`
  - `meal_id`
  - `created_at`
- **외래키:** `meal_id` → `meals.id` (ON DELETE SET NULL)

#### 4. admins 테이블
- **용도:** 관리자 계정 관리
- **기본 계정:**
  - username: `admin`
  - password: `admin1234` (BCrypt 암호화)

---

## 에러 처리 플로우

### 1. 이미지 업로드 실패

```
Android → Spring Boot
     ↓
파일 검증 실패
     ├─ 파일이 비어있음
     │  → HTTP 400 Bad Request
     │  → "이미지 파일이 비어있습니다"
     │
     ├─ 파일 크기 초과 (>10MB)
     │  → HTTP 400 Bad Request
     │  → "이미지 파일 크기는 10MB를 초과할 수 없습니다"
     │
     └─ 잘못된 파일 타입
        → HTTP 400 Bad Request
        → "이미지 파일만 업로드 가능합니다"
```

### 2. AI 모델 추론 실패

```
Spring Boot → FastAPI
     ↓
FastAPI 오류 발생
     ├─ 모델 로드 실패
     │  → HTTP 500 Internal Server Error
     │  → "AI 모델 로드 실패"
     │
     ├─ 이미지 전처리 실패
     │  → HTTP 400 Bad Request
     │  → "이미지 처리 중 오류"
     │
     └─ ONNX Runtime 오류
        → HTTP 500 Internal Server Error
        → "음식 인식 중 오류 발생"
     ↓
Spring Boot에 에러 전달
     ↓
Android에 에러 메시지 표시
```

### 3. 데이터베이스 오류

```
Spring Boot → MySQL
     ↓
DB 연결 실패
     ├─ MySQL 서버 다운
     │  → HTTP 503 Service Unavailable
     │  → "데이터베이스 연결 실패"
     │
     ├─ 유저가 존재하지 않음
     │  → HTTP 400 Bad Request
     │  → "사용자를 찾을 수 없습니다"
     │
     └─ Meal 저장 실패
        → HTTP 500 Internal Server Error
        → "식사 추가 중 오류가 발생했습니다"
```

### 4. 학습 데이터 수집 실패

```
Meal 저장 성공
     ↓
학습 데이터 저장 시도
     ↓
오류 발생 (예: 디스크 공간 부족)
     ↓
로그에 에러 기록
     ↓
⚠️ 중요: Meal은 이미 저장됨 (롤백 안 함)
     ↓
사용자에게는 "식사가 추가되었습니다" 응답
     ↓
관리자는 로그에서 학습 데이터 수집 실패 확인 가능
```

---

## API 엔드포인트 목록

### Spring Boot (Port 8080)

#### 음식 인식
- `POST /api/food/recognize`
  - 이미지 파일로 음식 인식
  - 파라미터: `image`, `userUniqueCode`

- `GET /api/food/search?foodName={name}`
  - 음식 이름으로 칼로리 검색
  - CSV 데이터베이스 조회

#### 식사 관리
- `POST /api/meals`
  - 식사 추가 (일반)

- `POST /api/meals/with-training-data`
  - 식사 추가 + 학습 데이터 수집
  - 파라미터: `image`, `aiPrediction`, `mealRequest`

- `PUT /api/meals/{mealId}`
  - 식사 수정

- `DELETE /api/meals/{mealId}`
  - 식사 삭제

- `GET /api/meals/{mealId}`
  - 식사 단건 조회

- `GET /api/meals/user/{uniqueCode}/date/{date}`
  - 특정 날짜의 식사 조회

#### 관리자
- `POST /api/admin/login`
  - 관리자 로그인

- `GET /api/admin/users`
  - 전체 사용자 조회

- `DELETE /api/admin/users/{uniqueCode}`
  - 사용자 삭제

#### 헬스 체크
- `GET /api/actuator/health`
  - 서버 상태 확인

### FastAPI (Port 8000)

#### 음식 인식
- `POST /api/v1/food/recognize`
  - AI 모델로 음식 인식
  - 파라미터: `image`

- `GET /api/v1/food/search?foodName={name}`
  - CSV에서 칼로리 검색

#### 헬스 체크
- `GET /health`
  - 서버 상태 확인

---

## Docker 네트워크 통신

### 컨테이너 간 통신

```
┌─────────────────────────────────────────┐
│  Docker Network: checkfood-network      │
│  (Bridge Mode)                          │
└─────────────────────────────────────────┘

컨테이너 이름으로 통신:

1. Spring Boot → MySQL
   jdbc:mysql://db-mysql:3306/checkfood

2. Spring Boot → FastAPI
   http://backend-fastapi:8000

3. FastAPI → MySQL (미사용)
   mysql+pymysql://checkfood_user:...@db-mysql:3306/checkfood

외부 접근 (Host → Container):
- localhost:8080 → Spring Boot
- localhost:8000 → FastAPI
- localhost:3306 → MySQL
```

### Volume 마운트

```
┌─────────────────────────────────────────┐
│  Docker Volumes                         │
└─────────────────────────────────────────┘

1. checkfood-mysql-data
   - MySQL 데이터 영구 저장
   - /var/lib/mysql

2. checkfood-training-images
   - 학습용 이미지 영구 저장
   - /app/training_images
   - 구조: /{userCode}/{yyyy}/{MM}/{dd}/{uuid}_{timestamp}.{ext}
```

---

## 환경변수 설정

### docker-compose.yml

```yaml
backend-spring:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://db-mysql:3306/checkfood...
    SPRING_DATASOURCE_USERNAME: checkfood_user
    SPRING_DATASOURCE_PASSWORD: checkfood_pass
    FASTAPI_SERVICE_URL: http://backend-fastapi:8000
    TRAINING_IMAGE_PATH: /app/training_images
    SERVER_PORT: 8080

backend-fastapi:
  environment:
    DATABASE_URL: mysql+pymysql://checkfood_user:...@db-mysql:3306/checkfood
    PORT: 8000
    PYTHONUNBUFFERED: "1"

db-mysql:
  environment:
    MYSQL_ROOT_PASSWORD: rootpassword
    MYSQL_DATABASE: checkfood
    MYSQL_USER: checkfood_user
    MYSQL_PASSWORD: checkfood_pass
    TZ: Asia/Seoul
```

---

## 시스템 시작 순서

```
1. MySQL 컨테이너 시작
   ↓
   Health Check 대기
   (mysqladmin ping)
   ↓
   ✅ Healthy

2. FastAPI 컨테이너 시작
   ↓
   - CSV 데이터 로드 (400개 음식)
   - ONNX 모델 로드 (164개 클래스)
   ↓
   Health Check 대기
   ↓
   ✅ Healthy

3. Spring Boot 컨테이너 시작
   ↓
   - MySQL 연결
   - JPA 스키마 업데이트
   - FastAPI 연결 테스트
   ↓
   Health Check 대기
   ↓
   ✅ Healthy

4. 모든 서비스 준비 완료
   ↓
   Android 앱 연결 가능
```

---

**문서 작성일:** 2025-11-08
**작성자:** CheckFood 개발팀
**버전:** 2.0 (학습 데이터 수집 기능 포함)
