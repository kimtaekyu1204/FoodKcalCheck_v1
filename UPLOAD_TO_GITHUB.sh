#!/bin/bash

# CheckFood GitHub 업로드 스크립트
# Repository: https://github.com/kimtaekyu1204/FoodKcalCheck_v1.git

echo "🚀 CheckFood GitHub 업로드 시작..."

# 현재 디렉토리 확인
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 오류: CheckFood 프로젝트 루트 디렉토리에서 실행하세요"
    exit 1
fi

echo ""
echo "📋 단계 1: Git 초기화"
git init
echo "✅ Git 초기화 완료"

echo ""
echo "📋 단계 2: 모든 파일 추가"
git add .
echo "✅ 파일 추가 완료"

echo ""
echo "📋 단계 3: 초기 커밋"
git commit -m "Initial commit: CheckFood AI 칼로리 추적 시스템

## 프로젝트 구성
- Android 앱 (Jetpack Compose, Kotlin)
- Spring Boot 백엔드 (Java 17)
- FastAPI AI 서버 (Python, ONNX Runtime)
- MySQL 데이터베이스
- Docker Compose 오케스트레이션

## 주요 기능
- AI 기반 음식 인식 (164개 클래스)
- 자동 칼로리 계산
- 일일/월별 칼로리 추적
- 목표 칼로리 설정
- 관리자 시스템
- 학습 데이터 수집

## 기술 스택
- Frontend: Kotlin, Jetpack Compose, CameraX, Retrofit
- Backend: Spring Boot 3.5.7, Spring Security, JPA
- AI: FastAPI, ONNX Runtime, PIL
- Database: MySQL 8.0
- DevOps: Docker, Docker Compose
"
echo "✅ 커밋 완료"

echo ""
echo "📋 단계 4: 원격 저장소 연결"
git remote add origin https://github.com/kimtaekyu1204/FoodKcalCheck_v1.git
echo "✅ 원격 저장소 연결 완료"

echo ""
echo "📋 단계 5: 브랜치 이름 설정"
git branch -M main
echo "✅ 브랜치 설정 완료 (main)"

echo ""
echo "📋 단계 6: GitHub에 푸시"
echo "⚠️  다음 명령어를 실행하여 GitHub에 푸시하세요:"
echo ""
echo "    git push -u origin main"
echo ""
echo "💡 GitHub 인증 정보를 입력해야 할 수 있습니다."
echo ""
echo "✅ 준비 완료! 위의 명령어를 실행하세요."

