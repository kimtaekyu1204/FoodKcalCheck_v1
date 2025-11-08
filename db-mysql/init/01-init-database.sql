-- CheckFood Database Initialization Script
-- This script will be executed when MySQL container starts for the first time

USE checkfood;

-- Users Table (유저 고유 코드 추가)
CREATE TABLE IF NOT EXISTS users (
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

-- Meals Table (음식1~3 컬럼 구조)
CREATE TABLE IF NOT EXISTS meals (
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

-- Admins Table (관리자 계정)
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Training Data Log Table (AI 모델 재학습용 데이터 수집)
CREATE TABLE IF NOT EXISTS training_data_log (
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

-- Insert default admin account
-- 관리자 계정은 AdminInitializer에서 자동 생성됩니다
-- 환경변수 ADMIN_USERNAME, ADMIN_PASSWORD로 설정 가능

-- Foods Table 삭제 (더 이상 사용 안 함)
DROP TABLE IF EXISTS foods;

-- Verify installation
SELECT 'CheckFood Database Initialized Successfully' AS Status;
SELECT COUNT(*) AS TotalUsers FROM users;
SELECT COUNT(*) AS TotalMeals FROM meals;
SELECT COUNT(*) AS TotalAdmins FROM admins;
