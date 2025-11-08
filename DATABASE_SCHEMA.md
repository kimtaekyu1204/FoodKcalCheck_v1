# CheckFood 데이터베이스 스키마

**데이터베이스**: MySQL 8.0  
**인코딩**: UTF-8 (utf8mb4_unicode_ci)  
**엔진**: InnoDB

---

## 📊 테이블 구조

### 1. users 테이블 (사용자 정보)

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unique_code VARCHAR(10) NOT NULL UNIQUE COMMENT '유저 고유 코드 (a-z, A-Z, 0-9)',
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    daily_calorie_goal INT DEFAULT 2000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_unique_code (unique_code),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**설명**:
- `unique_code`: 10자리 랜덤 코드 (회원가입 시 자동 생성)
- `daily_calorie_goal`: 일일 목표 칼로리 (기본값: 2000kcal)
- `password`: BCrypt 암호화된 비밀번호

---

### 2. meals 테이블 (식사 기록)

```sql
CREATE TABLE meals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_unique_code VARCHAR(10) NOT NULL COMMENT '유저 고유 코드',
    meal_date DATE NOT NULL COMMENT '식사 날짜',
    meal_time TIME NOT NULL COMMENT '식사 시간',
    meal_type VARCHAR(20) NOT NULL COMMENT '식사 타입 (BREAKFAST, LUNCH, DINNER, SNACK)',
    food_count INT NOT NULL COMMENT '음식 개수 (1~3)',
    food1_name VARCHAR(255) COMMENT '음식1 이름',
    food1_calories INT COMMENT '음식1 칼로리',
    food2_name VARCHAR(255) COMMENT '음식2 이름',
    food2_calories INT COMMENT '음식2 칼로리',
    food3_name VARCHAR(255) COMMENT '음식3 이름',
    food3_calories INT COMMENT '음식3 칼로리',
    total_calories INT DEFAULT 0 COMMENT '총 칼로리',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_code_date (user_unique_code, meal_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**설명**:
- 최대 3개 음식까지 저장 가능 (food1, food2, food3)
- `total_calories`: 자동 계산 (food1_calories + food2_calories + food3_calories)
- `meal_type`: BREAKFAST(아침), LUNCH(점심), DINNER(저녁), SNACK(간식)

---

### 3. admins 테이블 (관리자 계정)

```sql
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**설명**:
- 관리자 로그인 정보 저장
- 기본 계정: `admin` / `admin1234` (BCrypt 암호화)

---

### 4. training_data_log 테이블 (AI 학습 데이터)

```sql
CREATE TABLE training_data_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_unique_code VARCHAR(10) NOT NULL COMMENT '유저 고유 코드',
    meal_id BIGINT COMMENT '연관된 meal ID (외래키)',
    image_path VARCHAR(512) NOT NULL COMMENT '저장된 이미지 파일 경로',
    ai_prediction JSON COMMENT 'AI 모델의 원본 예측 결과 (음식명, 칼로리)',
    user_corrected_json JSON COMMENT '사용자가 수정한 최종 정보 (Ground Truth)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_code (user_unique_code),
    INDEX idx_meal_id (meal_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**설명**:
- AI 모델 재학습용 데이터 수집
- `ai_prediction`: AI 모델의 원본 예측 (JSON 형식)
- `user_corrected_json`: 사용자가 수정한 정답 데이터 (JSON 형식)
- `image_path`: 학습용 이미지 파일 경로

---

## 🔗 테이블 관계도

```
users (1) ────< (N) meals
                    │
                    │ (1:N)
                    │
                    └───< (N) training_data_log
                    
admins (독립 테이블)
```

---

## 📝 인덱스 정보

### users 테이블
- `idx_unique_code`: unique_code 조회 최적화
- `idx_email`: email 조회 최적화

### meals 테이블
- `idx_user_code_date`: 사용자별 날짜별 조회 최적화 (복합 인덱스)

### training_data_log 테이블
- `idx_user_code`: 사용자별 조회 최적화
- `idx_meal_id`: 식사별 조회 최적화
- `idx_created_at`: 날짜별 조회 최적화

---

## 🔐 기본 데이터

### 관리자 계정
애플리케이션 시작 시 AdminInitializer에서 자동으로 생성됩니다.
초기 관리자 정보는 `AdminService.java`에서 설정할 수 있습니다.

### 사용자 계정
회원가입 API(`/api/auth/signup`)를 통해 생성합니다.
- uniqueCode는 자동으로 10자리 랜덤 코드로 생성됩니다.
- 비밀번호는 BCrypt로 암호화되어 저장됩니다.
- 기본 목표 칼로리는 2000 kcal로 설정됩니다.

---

## 📂 초기화 스크립트

데이터베이스 초기화 스크립트 위치:
```
db-mysql/init/01-init-database.sql
```

Docker 컨테이너 시작 시 자동 실행됩니다.

---

## 🔄 데이터베이스 연결 정보

### Docker 환경
- **Host**: `db-mysql` (컨테이너 이름)
- **Port**: `3306`
- **Database**: `checkfood`
- **Username**: `checkfood_user`
- **Password**: `checkfood_pass`

### 로컬 환경
- **Host**: `localhost`
- **Port**: `3306`
- **Database**: `checkfood`
- **Username**: `checkfood_user`
- **Password**: `checkfood_pass`

---

## 📊 주요 쿼리 예시

### 일일 식사 조회
```sql
SELECT * FROM meals 
WHERE user_unique_code = 'TESTUSER01' 
  AND meal_date = '2025-11-08'
ORDER BY meal_time;
```

### 월별 칼로리 집계
```sql
SELECT meal_date, SUM(total_calories) as daily_total
FROM meals
WHERE user_unique_code = 'TESTUSER01'
  AND YEAR(meal_date) = 2025
  AND MONTH(meal_date) = 11
GROUP BY meal_date
ORDER BY meal_date;
```

### 학습 데이터 조회
```sql
SELECT tdl.*, m.meal_date, m.meal_type
FROM training_data_log tdl
LEFT JOIN meals m ON tdl.meal_id = m.id
WHERE tdl.user_unique_code = 'TESTUSER01'
ORDER BY tdl.created_at DESC;
```

---

**마지막 업데이트**: 2025-11-08  
**문서 버전**: 1.0

