# CheckFood AWS 배포 계획

**배포 전략**: Android APK + AWS 클라우드 백엔드

---

## 📱 1. Android 앱 (APK 배포)

### 포함 요소
- ✅ Android 앱 소스 코드 전체 (`android/` 폴더)
- ✅ Jetpack Compose UI
- ✅ CameraX (카메라 기능)
- ✅ Retrofit (API 통신)
- ✅ Navigation Component

### 배포 파일
- **APK 파일**: `app-release.apk` (서명 필요)
- **또는 AAB 파일**: `app-release.aab` (Google Play Store용)

### 설정 변경 필요
**API 엔드포인트 변경** (`RetrofitInstance.kt`):
```kotlin
// 현재 (로컬)
private val BASE_URL = "http://10.0.2.2:8080/api/"

// 변경 (AWS)
private val BASE_URL = "https://your-domain.com/api/"
// 또는
private val BASE_URL = "http://ec2-xx-xx-xx-xx.compute.amazonaws.com:8080/api/"
```

### 빌드 방법
```bash
cd android
./gradlew assembleRelease
# 또는 Google Play Store용
./gradlew bundleRelease
```

### 배포 방법
1. **직접 배포**: APK 파일을 사용자에게 직접 전달
2. **Google Play Store**: AAB 파일 업로드 (앱 서명 필요)
3. **Firebase App Distribution**: 테스트 배포

---

## 🗄️ 2. MySQL 데이터베이스

### AWS 배포 옵션

#### 옵션 A: Amazon RDS (권장) ⭐
**포함 요소**:
- ✅ MySQL 8.0 데이터베이스
- ✅ 자동 백업
- ✅ 자동 패치
- ✅ 고가용성 (Multi-AZ)

**필요한 설정**:
- DB 인스턴스 크기: `db.t3.micro` (프리티어) 또는 `db.t3.small`
- 스토리지: 20GB SSD (확장 가능)
- 보안 그룹: Spring Boot와 FastAPI에서만 접근 허용 (포트 3306)

**초기화 스크립트**:
- `db-mysql/init/01-init-database.sql` 파일을 RDS 연결 후 실행

**장점**:
- ✅ 관리 부담 최소화
- ✅ 자동 백업
- ✅ 확장성 좋음
- ✅ 보안 강화

**비용**: 프리티어 1년, 이후 월 $15~30

---

#### 옵션 B: EC2 + Docker MySQL
**포함 요소**:
- ✅ EC2 인스턴스
- ✅ Docker + MySQL 컨테이너
- ✅ 수동 백업 설정 필요

**장점**:
- ✅ 비용 저렴 (다른 서비스와 공유 가능)

**단점**:
- ⚠️ 백업 직접 관리
- ⚠️ 유지보수 부담

---

## 🍃 3. Spring Boot 백엔드

### AWS 배포 옵션

#### 옵션 A: AWS Elastic Beanstalk (권장) ⭐
**포함 요소**:
- ✅ Spring Boot JAR 파일
- ✅ `application.properties` (환경변수 설정)
- ✅ Dockerfile (선택)

**배포 파일**:
```
backend-spring/build/libs/backend-spring-0.0.1-SNAPSHOT.jar
```

**환경변수 설정** (Elastic Beanstalk 콘솔):
```properties
SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-endpoint:3306/checkfood
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=your_password
FASTAPI_SERVICE_URL=http://fastapi-instance-url:8000
SERVER_PORT=8080
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD=your_admin_password
```

**장점**:
- ✅ 자동 스케일링
- ✅ 로드 밸런싱
- ✅ 모니터링 내장
- ✅ 쉬운 배포 (ZIP 업로드)

**비용**: 프리티어 1년, 이후 월 $10~50

---

#### 옵션 B: EC2 + Docker
**포함 요소**:
- ✅ EC2 인스턴스 (t2.micro ~ t3.small)
- ✅ Docker
- ✅ Spring Boot 컨테이너

**배포 파일**:
- `backend-spring/Dockerfile`
- `backend-spring/` 전체 폴더

**배포 방법**:
```bash
# EC2에서 실행
cd backend-spring
docker build -t checkfood-spring .
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://rds-endpoint:3306/checkfood \
  -e FASTAPI_SERVICE_URL=http://fastapi-ip:8000 \
  checkfood-spring
```

---

#### 옵션 C: AWS ECS/Fargate (컨테이너)
**포함 요소**:
- ✅ Spring Boot Docker 이미지
- ✅ Task Definition
- ✅ Service 설정

**장점**:
- ✅ 서버리스 컨테이너
- ✅ 자동 스케일링
- ✅ 관리 부담 적음

**비용**: 사용량 기반, 월 $20~80

---

## 🐍 4. FastAPI AI 서버

### AWS 배포 옵션

#### 옵션 A: EC2 + Docker (권장) ⭐
**포함 요소**:
- ✅ FastAPI 소스 코드 (`backend-fastapi/`)
- ✅ ONNX 모델 (70MB) - `food_recognition_model.onnx`
- ✅ idx_to_class.json (164개 클래스)
- ✅ foodKcalList.csv (400개 음식)
- ✅ requirements.txt
- ✅ Dockerfile

**EC2 인스턴스 요구사항**:
- 타입: `t3.medium` 이상 (AI 모델 추론용)
- vCPU: 2개 이상
- 메모리: 4GB 이상 (ONNX Runtime)
- 스토리지: 20GB

**배포 방법**:
```bash
# EC2에서 실행
cd backend-fastapi
docker build -t checkfood-fastapi .
docker run -d -p 8000:8000 checkfood-fastapi
```

**이유**: AI 모델 추론에는 충분한 CPU/메모리 필요

---

#### 옵션 B: AWS Lambda + API Gateway
**포함 요소**:
- ✅ FastAPI 코드
- ✅ Mangum (Lambda 어댑터)
- ⚠️ 모델 파일 (Lambda 제한 250MB)

**장점**:
- ✅ 서버리스
- ✅ 사용량 기반 과금

**단점**:
- ⚠️ Cold start (첫 요청 느림)
- ⚠️ 실행 시간 제한 (15분)
- ⚠️ AI 모델 추론에 비효율적

**권장하지 않음**: AI 모델 추론은 EC2가 더 적합

---

## 🏗️ 권장 AWS 아키텍처

```
┌──────────────────────────────────────────────────────────┐
│                     Android 앱 (APK)                      │
│                                                           │
└────────────────────┬─────────────────────────────────────┘
                     │ HTTPS
                     ↓
┌──────────────────────────────────────────────────────────┐
│          AWS Application Load Balancer (ALB)             │
│                  (HTTPS 인증서 설정)                      │
└────────────┬──────────────────────────────┬──────────────┘
             │                                │
             │ HTTP:8080                      │ HTTP:8000
             ↓                                ↓
┌─────────────────────────┐      ┌──────────────────────────┐
│  Spring Boot 백엔드      │      │   FastAPI AI 서버         │
│  (Elastic Beanstalk)    │      │   (EC2 + Docker)         │
│  - Port 8080            │◄────►│   - Port 8000            │
│  - JAR 배포             │      │   - ONNX Runtime         │
│  - Auto Scaling         │      │   - 70MB 모델 파일       │
└────────────┬────────────┘      └──────────────────────────┘
             │
             │ Port 3306
             ↓
┌─────────────────────────┐
│   Amazon RDS MySQL      │
│   - 자동 백업           │
│   - Multi-AZ (선택)     │
│   - 20GB 스토리지       │
└─────────────────────────┘

추가 서비스:
- Amazon S3: 학습 이미지 저장 (training_images)
- CloudWatch: 로그 및 모니터링
- Route 53: 도메인 관리 (선택)
```

---

## 📦 각 서비스별 배포 파일

### 1. Android APK
**필요 파일**:
- `android/` 폴더 전체
- 빌드 후 생성: `app-release.apk`

**설정 파일**:
- `RetrofitInstance.kt` - AWS 엔드포인트로 변경

---

### 2. Spring Boot (Elastic Beanstalk)
**필요 파일**:
- `backend-spring-0.0.1-SNAPSHOT.jar` (빌드 필요)
- 또는 ZIP:
  ```
  backend-spring/
  ├── src/
  ├── build.gradle
  ├── settings.gradle
  └── gradlew
  ```

**환경변수** (Elastic Beanstalk):
- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- FASTAPI_SERVICE_URL
- ADMIN_USERNAME
- ADMIN_PASSWORD

---

### 3. FastAPI (EC2)
**필요 파일**:
```
backend-fastapi/
├── main.py
├── requirements.txt
├── Dockerfile
├── inference/
│   ├── model_inference.py
│   └── nutrition_db.py
├── models/
│   ├── food_recognition_model.onnx (70MB)
│   └── idx_to_class.json
└── foodKcalList.csv
```

**전체 폴더 업로드** (EC2로 SCP 또는 Git Clone)

---

### 4. MySQL (RDS)
**필요 작업**:
- RDS 인스턴스 생성
- 초기화 스크립트 실행: `db-mysql/init/01-init-database.sql`

**설정**:
- MySQL 8.0
- 20GB 스토리지
- 보안 그룹: Spring Boot와 FastAPI만 접근 허용

---

## 💰 예상 비용 (월별)

### 프리티어 사용 시 (1년)
- **RDS MySQL** (db.t3.micro): 무료
- **Elastic Beanstalk** (t2.micro): 무료
- **EC2 FastAPI** (t3.medium): $30~40
- **S3** (이미지 저장): $1~5
- **데이터 전송**: $5~10
- **총계**: 약 $36~55/월

### 프리티어 종료 후
- **RDS MySQL** (db.t3.micro): $15
- **Elastic Beanstalk** (t3.small): $15~30
- **EC2 FastAPI** (t3.medium): $30~40
- **기타**: $10
- **총계**: 약 $70~95/월

---

## 🚀 배포 순서

### 1단계: RDS MySQL 구축
1. RDS MySQL 8.0 인스턴스 생성
2. 보안 그룹 설정 (3306 포트)
3. 데이터베이스 초기화 스크립트 실행

### 2단계: FastAPI 배포 (EC2)
1. EC2 t3.medium 인스턴스 생성
2. Docker 설치
3. backend-fastapi 폴더 업로드
4. Docker 빌드 및 실행
5. 포트 8000 오픈

### 3단계: Spring Boot 배포 (Elastic Beanstalk)
1. JAR 파일 빌드
2. Elastic Beanstalk 애플리케이션 생성
3. 환경변수 설정 (RDS, FastAPI 엔드포인트)
4. JAR 파일 업로드

### 4단계: Android 앱 빌드
1. RetrofitInstance.kt에서 AWS 엔드포인트 설정
2. Release APK 빌드
3. 서명 (keystore)
4. 배포

---

## 📋 각 구성요소별 체크리스트

### Android APK
- [ ] API 엔드포인트를 AWS 주소로 변경
- [ ] Release 빌드 설정
- [ ] Keystore 생성 (서명용)
- [ ] ProGuard/R8 설정 (코드 난독화)
- [ ] APK 빌드 및 서명
- [ ] 테스트

### MySQL (RDS)
- [ ] RDS 인스턴스 생성
- [ ] 보안 그룹 설정
- [ ] 파라미터 그룹 설정 (timezone 등)
- [ ] 초기화 스크립트 실행
- [ ] 백업 설정
- [ ] 모니터링 설정

### Spring Boot (Elastic Beanstalk)
- [ ] JAR 파일 빌드
- [ ] Elastic Beanstalk 애플리케이션 생성
- [ ] 환경변수 설정
- [ ] 배포
- [ ] Health Check 설정
- [ ] Auto Scaling 설정 (선택)

### FastAPI (EC2)
- [ ] EC2 인스턴스 생성 (t3.medium)
- [ ] 보안 그룹 설정 (8000 포트)
- [ ] Docker 설치
- [ ] backend-fastapi 폴더 업로드
- [ ] Docker 이미지 빌드
- [ ] 컨테이너 실행
- [ ] 자동 재시작 설정 (systemd)

---

## 🔒 보안 설정

### 1. 환경변수 분리
- ❌ 하드코딩된 비밀번호 제거 (완료)
- ✅ AWS Systems Manager Parameter Store 사용
- ✅ AWS Secrets Manager 사용 (권장)

### 2. 보안 그룹 설정
```
RDS MySQL:
- 3306 포트: Spring Boot, FastAPI에서만 접근

Spring Boot:
- 8080 포트: ALB에서만 접근

FastAPI:
- 8000 포트: Spring Boot에서만 접근

ALB:
- 80 포트: 전체 오픈 (HTTP)
- 443 포트: 전체 오픈 (HTTPS)
```

### 3. HTTPS 설정
- AWS Certificate Manager (ACM)에서 무료 SSL 인증서 발급
- ALB에 HTTPS 리스너 추가
- HTTP → HTTPS 리디렉션 설정

---

## 🔧 필요한 수정 사항

### 1. Android - API 엔드포인트
**파일**: `android/app/src/main/java/com/example/android/data/api/RetrofitInstance.kt`
```kotlin
// 수정 필요
private val BASE_URL = System.getenv("API_BASE_URL") 
    ?: "https://api.checkfood.com/api/"  // AWS 도메인으로 변경
```

### 2. Spring Boot - FastAPI URL
**파일**: `backend-spring/src/main/resources/application.properties`
```properties
# 환경변수로 설정 (이미 완료)
fastapi.service.url=${FASTAPI_SERVICE_URL:http://localhost:8000}
```

### 3. Docker Compose 사용 안 함
AWS 배포 시에는 각 서비스가 독립적으로 실행되므로 `docker-compose.yml`은 **로컬 개발용**으로만 사용됩니다.

---

## 📊 배포 전략 요약

| 구성요소 | AWS 서비스 | 인스턴스 | 비용 (월) |
|---------|-----------|---------|----------|
| MySQL | RDS | db.t3.micro | $15 (프리티어 후) |
| Spring Boot | Elastic Beanstalk | t2.micro/small | $15-30 |
| FastAPI | EC2 + Docker | t3.medium | $30-40 |
| 이미지 저장 | S3 | - | $1-5 |
| 로드 밸런서 | ALB | - | $16 |
| **총계** | - | - | **$77-106** |

**프리티어 1년**: RDS + EB 무료, FastAPI EC2만 $30-40

---

## 🎯 추천 배포 방법

### 초기 배포 (비용 최소화)
1. **RDS MySQL** (db.t3.micro) - 프리티어
2. **하나의 EC2** (t3.medium) - Spring Boot + FastAPI 함께 실행
3. **Docker Compose** 사용
4. **비용**: 월 $30-40

### 프로덕션 배포 (안정성 우선)
1. **RDS MySQL** (db.t3.small + Multi-AZ)
2. **Elastic Beanstalk** - Spring Boot
3. **EC2** (t3.medium) - FastAPI
4. **ALB** - 로드 밸런싱 + HTTPS
5. **S3** - 이미지 저장
6. **비용**: 월 $100-150

---

## 📝 다음 단계

어떤 배포 방법을 선택하시겠습니까?

1. **비용 최소화**: EC2 1대에 모두 (Docker Compose)
2. **권장 방법**: RDS + Elastic Beanstalk + EC2
3. **프로덕션**: 위 + ALB + S3 + 모니터링

선택하시면 상세한 배포 가이드를 작성해드리겠습니다!

