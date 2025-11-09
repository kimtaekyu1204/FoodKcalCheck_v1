# CheckFood EC2 배포 가이드

## 📌 개요

Docker Compose를 사용해 **EC2 1대**에 MySQL, Spring Boot, FastAPI를 모두 배포합니다.

### 현재 구조
```
CheckFood/
├── docker-compose.yml          # 모든 서비스 연결 설정
├── db-mysql/
│   ├── Dockerfile              # MySQL 이미지
│   └── init/
│       └── 01-init-database.sql
├── backend-spring/
│   ├── Dockerfile              # Spring Boot 이미지
│   └── src/
├── backend-fastapi/
│   ├── Dockerfile              # FastAPI 이미지
│   └── main.py
└── android/                    # 배포 X (APK만)
```

### 서비스 연결 구조
```
┌─────────────────────────────────────────┐
│          EC2 Instance (t3.medium)       │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  Docker Network: checkfood-network │
│  │                                  │   │
│  │  ┌──────────────┐                │   │
│  │  │   MySQL      │                │   │
│  │  │   :3306      │◄───────────┐   │   │
│  │  └──────────────┘            │   │   │
│  │         ▲                    │   │   │
│  │         │                    │   │   │
│  │  ┌──────┴───────┐    ┌───────┴──┐ │  │
│  │  │ Spring Boot  │◄──►│ FastAPI  │ │  │
│  │  │   :8080      │    │  :8000   │ │  │
│  │  └──────────────┘    └──────────┘ │  │
│  └─────────────────────────────────┘   │
│         │                  │            │
└─────────┼──────────────────┼────────────┘
          │                  │
       Port 8080          Port 8000
          │                  │
          ▼                  ▼
    Android App         (내부 통신)
```

---

## 🚀 배포 단계 (5단계로 완료!)

---

## 1️⃣ EC2 인스턴스 생성

### AWS Console 설정

1. **EC2 → Launch Instance** 클릭

2. **기본 설정**
   ```
   이름: checkfood-server
   OS: Ubuntu Server 22.04 LTS (프리티어 가능)
   인스턴스 타입: t3.medium (또는 t2.micro - 테스트용)
   ```

3. **키 페어 생성** (중요!)
   - 새 키 페어 생성 클릭
   - 이름: `checkfood-key`
   - 타입: RSA
   - 형식: .pem
   - **다운로드 후 안전하게 보관!**

4. **보안 그룹 설정**

   "보안 그룹 생성" 클릭:

   | 유형 | 포트 | 소스 | 설명 |
   |------|------|------|------|
   | SSH | 22 | 내 IP | SSH 접속 |
   | HTTP | 80 | 0.0.0.0/0 | 웹 (나중에 Nginx) |
   | 사용자 지정 TCP | 8080 | 0.0.0.0/0 | Spring Boot API |
   | 사용자 지정 TCP | 8000 | 0.0.0.0/0 | FastAPI (선택) |

5. **스토리지 설정**
   ```
   크기: 30GB
   유형: gp3
   ```

6. **Launch Instance** 클릭!

7. **인스턴스 시작 후**
   - 퍼블릭 IPv4 주소 복사 (예: `3.35.123.45`)
   - 이 주소를 메모장에 저장!

---

## 2️⃣ EC2 접속 및 환경 설정

### SSH 접속

**Mac/Linux:**
```bash
# 키 파일 권한 설정 (최초 1회)
chmod 400 ~/Downloads/checkfood-key.pem

# EC2 접속
ssh -i ~/Downloads/checkfood-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

**Windows (PowerShell):**
```powershell
ssh -i C:\Users\YourName\Downloads\checkfood-key.pem ubuntu@YOUR_EC2_PUBLIC_IP
```

### Docker 설치

EC2에 접속한 상태에서 실행:

```bash
# 시스템 업데이트
sudo apt-get update && sudo apt-get upgrade -y

# Docker 설치 (자동 설치 스크립트)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose 플러그인 설치
sudo apt-get install docker-compose-plugin -y

# 현재 사용자를 docker 그룹에 추가 (sudo 없이 사용)
sudo usermod -aG docker ubuntu

# Git 설치 (프로젝트 다운로드용)
sudo apt-get install git -y
```

### 재로그인 (권한 적용)

```bash
# 로그아웃
exit

# 다시 접속
ssh -i ~/Downloads/checkfood-key.pem ubuntu@YOUR_EC2_PUBLIC_IP

# Docker 동작 확인
docker --version
docker compose version
```

출력 예시:
```
Docker version 24.0.7
Docker Compose version v2.23.0
```

---

## 3️⃣ 프로젝트 파일 업로드

### 방법 A: Git Clone (권장)

**1. GitHub에 프로젝트 푸시** (로컬에서)
```bash
cd /Users/kimtaekyu/Documents/Develop_Fold/CheckFood

# Git 저장소 초기화 (아직 안했다면)
git add .
git commit -m "Ready for EC2 deployment"
git push origin main
```

**2. EC2에서 Clone**
```bash
cd ~
git clone https://github.com/YOUR_USERNAME/CheckFood.git
cd CheckFood
```

---

### 방법 B: SCP로 직접 전송 (Git 없이)

**로컬 PC에서 실행:**

```bash
# CheckFood 폴더로 이동
cd /Users/kimtaekyu/Documents/Develop_Fold

# EC2로 전송 (몇 분 소요)
scp -i ~/Downloads/checkfood-key.pem -r CheckFood ubuntu@YOUR_EC2_PUBLIC_IP:~/
```

**EC2에서 확인:**
```bash
cd ~/CheckFood
ls -la
```

출력:
```
docker-compose.yml
backend-spring/
backend-fastapi/
db-mysql/
android/
...
```

---

## 4️⃣ 환경변수 설정

### .env 파일 생성

**EC2에서 실행:**

```bash
cd ~/CheckFood

# .env 파일 생성
nano .env
```

**내용 입력:**

```env
# MySQL 설정
MYSQL_ROOT_PASSWORD=MySecureRootPass123!
MYSQL_DATABASE=checkfood
MYSQL_USER=checkfood_user
MYSQL_PASSWORD=MySecureUserPass456!

# Spring Boot 설정
SPRING_DATASOURCE_URL=jdbc:mysql://db-mysql:3306/checkfood?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=checkfood_user
SPRING_DATASOURCE_PASSWORD=MySecureUserPass456!
FASTAPI_SERVICE_URL=http://backend-fastapi:8000
ADMIN_USERNAME=admin
ADMIN_PASSWORD=AdminPass789!

# FastAPI 설정
DATABASE_URL=mysql+pymysql://checkfood_user:MySecureUserPass456!@db-mysql:3306/checkfood
SPRING_SERVICE_URL=http://backend-spring:8080

# 타임존
TZ=Asia/Seoul
```

**중요:**
- `MySecureRootPass123!` → 실제 강력한 비밀번호로 변경
- `MySecureUserPass456!` → 실제 강력한 비밀번호로 변경
- `AdminPass789!` → 실제 강력한 비밀번호로 변경
- **세 군데 `MySecureUserPass456!`가 모두 같아야 함!**

**저장:**
- `Ctrl + O` → Enter (저장)
- `Ctrl + X` (종료)

**파일 권한 설정 (보안):**
```bash
chmod 600 .env
```

---

## 5️⃣ Docker Compose 실행

### 서비스 시작

```bash
cd ~/CheckFood

# 백그라운드로 빌드 및 실행
docker compose up -d --build
```

출력 예시:
```
[+] Building 245.3s (45/45) FINISHED
[+] Running 4/4
 ✔ Network checkfood-network      Created
 ✔ Container checkfood-mysql       Started
 ✔ Container checkfood-fastapi     Started
 ✔ Container checkfood-spring      Started
```

이 명령어가 하는 일:
1. ✅ MySQL Dockerfile 빌드 → 컨테이너 실행 (포트 3306)
2. ✅ FastAPI Dockerfile 빌드 → 컨테이너 실행 (포트 8000)
3. ✅ Spring Boot Dockerfile 빌드 → 컨테이너 실행 (포트 8080)
4. ✅ Docker 네트워크 생성 (서비스 간 통신)
5. ✅ Volume 생성 (MySQL 데이터 영구 저장)

**첫 실행은 5-10분 소요됩니다** (Spring Boot 빌드 시간)

---

## 6️⃣ 배포 확인

### 컨테이너 상태 확인

```bash
docker compose ps
```

**정상 출력:**
```
NAME                  IMAGE                           STATUS          PORTS
checkfood-mysql       checkfood/db-mysql:latest       Up (healthy)    0.0.0.0:3306->3306/tcp
checkfood-spring      checkfood/backend-spring:latest Up              0.0.0.0:8080->8080/tcp
checkfood-fastapi     checkfood/backend-fastapi:latest Up             0.0.0.0:8000->8000/tcp
```

모두 `Up` 또는 `Up (healthy)` 상태여야 합니다!

---

### 로그 확인

```bash
# 전체 로그 보기 (실시간)
docker compose logs -f

# Spring Boot 로그만 보기
docker compose logs -f backend-spring

# FastAPI 로그만 보기
docker compose logs -f backend-fastapi

# MySQL 로그만 보기
docker compose logs -f db-mysql
```

**종료:** `Ctrl + C`

---

### API 테스트 (EC2 내부에서)

```bash
# Spring Boot Health Check
curl http://localhost:8080/actuator/health

# 성공 응답:
# {"status":"UP"}

# FastAPI Health Check
curl http://localhost:8000/health

# 성공 응답:
# {"status":"healthy"}
```

---

### API 테스트 (외부에서)

**로컬 PC에서 실행:**

```bash
# Spring Boot 테스트
curl http://YOUR_EC2_PUBLIC_IP:8080/actuator/health

# FastAPI 테스트
curl http://YOUR_EC2_PUBLIC_IP:8000/health
```

**브라우저에서 접속:**
```
http://YOUR_EC2_PUBLIC_IP:8080/actuator/health
http://YOUR_EC2_PUBLIC_IP:8000/docs (FastAPI 문서)
```

---

## 7️⃣ MySQL 데이터베이스 확인

```bash
# MySQL 컨테이너 내부 접속
docker exec -it checkfood-mysql mysql -u checkfood_user -p

# 비밀번호 입력: MySecureUserPass456! (또는 .env에 설정한 값)
```

**MySQL 쉘에서 실행:**
```sql
-- 데이터베이스 선택
USE checkfood;

-- 테이블 확인
SHOW TABLES;

-- 초기 데이터 확인
SELECT * FROM food_calorie LIMIT 10;
SELECT * FROM admin_users;

-- 종료
EXIT;
```

---

## 8️⃣ Android 앱 연결

### API 엔드포인트 변경

**파일:** `android/app/src/main/java/com/example/android/data/api/RetrofitInstance.kt`

**변경 전:**
```kotlin
private val BASE_URL = System.getenv("API_BASE_URL") ?: "http://10.0.2.2:8080/api/"
```

**변경 후:**
```kotlin
private val BASE_URL = System.getenv("API_BASE_URL") ?: "http://YOUR_EC2_PUBLIC_IP:8080/api/"
```

**예시:**
```kotlin
private val BASE_URL = System.getenv("API_BASE_URL") ?: "http://3.35.123.45:8080/api/"
```

### APK 빌드

```bash
cd android
./gradlew assembleRelease
```

빌드된 APK:
```
android/app/build/outputs/apk/release/app-release.apk
```

---

## 🔧 Docker 관리 명령어

### 서비스 관리

```bash
# 서비스 중지
docker compose down

# 서비스 재시작
docker compose restart

# 특정 서비스만 재시작
docker compose restart backend-spring

# 코드 수정 후 다시 빌드
docker compose up -d --build

# 모든 것 삭제 (데이터 포함 - 주의!)
docker compose down -v
```

### 로그 및 모니터링

```bash
# 실시간 로그
docker compose logs -f

# 최근 100줄
docker compose logs --tail=100

# 특정 서비스 로그
docker compose logs backend-spring

# 컨테이너 리소스 사용량
docker stats
```

### 컨테이너 내부 접속

```bash
# Spring Boot 컨테이너
docker exec -it checkfood-spring bash

# FastAPI 컨테이너
docker exec -it checkfood-fastapi bash

# MySQL 컨테이너
docker exec -it checkfood-mysql bash
```

---

## 🔄 EC2 재부팅 시 자동 재시작 설정

### Systemd 서비스 생성

```bash
sudo nano /etc/systemd/system/checkfood.service
```

**내용:**
```ini
[Unit]
Description=CheckFood Docker Compose Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ubuntu/CheckFood
ExecStart=/usr/bin/docker compose up -d
ExecStop=/usr/bin/docker compose down
TimeoutStartSec=300

[Install]
WantedBy=multi-user.target
```

**활성화:**
```bash
# 서비스 활성화
sudo systemctl enable checkfood.service

# 서비스 시작
sudo systemctl start checkfood.service

# 상태 확인
sudo systemctl status checkfood.service
```

**테스트:**
```bash
# EC2 재부팅
sudo reboot

# 재접속 후 확인 (1-2분 후)
docker compose ps
```

---

## 🔒 보안 강화 (선택사항)

### 1. FastAPI 외부 접근 차단

**보안 그룹에서:**
- 포트 8000 규칙 **삭제**
- Spring Boot만 외부 접근 허용 (8080)

**이유:** FastAPI는 Spring Boot를 통해서만 접근해야 함

---

### 2. MySQL 외부 접근 차단

**보안 그룹에서:**
- 포트 3306 규칙 **삭제**
- Docker 네트워크 내부에서만 접근

---

### 3. SSH 접근 제한

**보안 그룹에서:**
- SSH (22) 소스를 "내 IP"로 제한
- 고정 IP 사용 권장

---

## 📊 배포 확인 체크리스트

완료 여부를 확인하세요:

- [ ] EC2 인스턴스 생성 완료
- [ ] SSH 접속 성공
- [ ] Docker 설치 완료 (`docker --version` 확인)
- [ ] 프로젝트 파일 업로드 완료
- [ ] `.env` 파일 생성 및 비밀번호 설정
- [ ] `docker compose up -d --build` 실행 완료
- [ ] 3개 컨테이너 모두 `Up` 상태 (`docker compose ps`)
- [ ] Spring Boot Health Check 성공 (8080)
- [ ] FastAPI Health Check 성공 (8000)
- [ ] MySQL 접속 및 테이블 확인
- [ ] 외부에서 API 접근 테스트
- [ ] Android 앱 API 엔드포인트 변경
- [ ] 자동 재시작 설정 (systemd)

---

## ⚠️ 문제 해결

### 컨테이너가 계속 재시작됨

```bash
# 로그 확인
docker compose logs backend-spring
docker compose logs backend-fastapi

# 환경변수 확인
docker compose config
```

**원인:**
- MySQL 연결 실패 (비밀번호 불일치)
- 포트 충돌
- 메모리 부족

---

### MySQL 연결 오류

**증상:**
```
Access denied for user 'checkfood_user'@'%'
```

**해결:**
1. `.env` 파일 확인 (비밀번호 일치 여부)
2. 컨테이너 재시작
```bash
docker compose down
docker compose up -d
```

---

### Spring Boot 빌드 실패

**증상:**
```
BUILD FAILED in 3m 45s
```

**해결:**
```bash
# 로컬에서 빌드 테스트
cd backend-spring
./gradlew build

# 문제 확인 후 수정
```

---

### 메모리 부족

**증상:**
```
Killed
```

**해결:**
- EC2 인스턴스 타입 업그레이드 (t2.micro → t3.medium)
- 또는 Swap 메모리 추가:
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

---

## 💰 예상 비용

| 항목 | 사양 | 월 비용 |
|------|------|---------|
| EC2 (t3.medium) | 2 vCPU, 4GB RAM | $30-35 |
| EBS 스토리지 | 30GB gp3 | $3 |
| 데이터 전송 | ~100GB | $5-10 |
| **총계** | | **약 $40-50** |

**프리티어 (1년):**
- t2.micro (750시간/월) 무료
- 30GB EBS 무료
- 비용: 데이터 전송 $5-10만 발생

---

## 📞 다음 단계

배포 완료 후:

1. **도메인 연결** (선택)
   - Route 53에서 도메인 구매
   - A 레코드로 EC2 연결

2. **HTTPS 설정**
   - Nginx + Let's Encrypt
   - 무료 SSL 인증서

3. **모니터링**
   - CloudWatch Logs
   - Docker stats

4. **백업 설정**
   - MySQL 자동 백업
   - EBS 스냅샷

---

## 🎉 완료!

이제 Android 앱에서 EC2 서버에 배포된 API를 사용할 수 있습니다!

**Android 앱 연결 주소:**
```
http://YOUR_EC2_PUBLIC_IP:8080/api/
```

**API 테스트:**
```
http://YOUR_EC2_PUBLIC_IP:8080/actuator/health
http://YOUR_EC2_PUBLIC_IP:8000/docs
```
