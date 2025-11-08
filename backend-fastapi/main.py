from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import uvicorn
import logging

# 모델 추론 모듈 임포트 (모델 파일이 있을 경우 사용)
try:
    from inference.model_inference import predict_food
    MODEL_AVAILABLE = True
except ImportError:
    MODEL_AVAILABLE = False
    logging.warning("모델 추론 모듈을 로드할 수 없습니다. 더미 모드로 실행됩니다.")

# 영양 정보 DB 모듈 임포트
try:
    from inference.nutrition_db import get_nutrition_db
    NUTRITION_DB_AVAILABLE = True
except ImportError:
    NUTRITION_DB_AVAILABLE = False
    logging.warning("영양 정보 DB 모듈을 로드할 수 없습니다.")

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="CheckFood AI Service")

# CORS 설정 (Spring Boot와 통신)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class FoodRecognitionResponse(BaseModel):
    success: bool
    message: str
    food_count: int  # 음식 개수 (1~3)
    food1_name: Optional[str] = None
    food1_calories: Optional[int] = None
    food2_name: Optional[str] = None
    food2_calories: Optional[int] = None
    food3_name: Optional[str] = None
    food3_calories: Optional[int] = None
    total_calories: int

@app.get("/")
async def root():
    return {"message": "CheckFood AI Service", "status": "running"}

@app.get("/health")
async def health():
    return {"status": "healthy"}

@app.get("/api/v1/food/search")
async def search_food(foodName: str):
    """
    음식 이름으로 칼로리 검색

    Args:
        foodName: 음식 이름

    Returns:
        음식 이름과 칼로리 정보
    """
    try:
        if not NUTRITION_DB_AVAILABLE:
            raise HTTPException(status_code=503, detail="영양 정보 DB를 사용할 수 없습니다")

        nutrition_db = get_nutrition_db()
        nutrition_info = nutrition_db.get_nutrition_info(foodName)

        if nutrition_info is None:
            raise HTTPException(status_code=404, detail=f"음식을 찾을 수 없습니다: {foodName}")

        return {
            "foodName": foodName,
            "calories": int(nutrition_info['calories'])
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"음식 검색 중 오류: {e}")
        raise HTTPException(status_code=500, detail=f"음식 검색 중 오류 발생: {str(e)}")

@app.post("/api/v1/food/recognize", response_model=FoodRecognitionResponse)
async def recognize_food(image: UploadFile = File(...)):
    """
    음식 이미지 인식 API (v2: AI는 음식 이름 + 칼로리 예측, 하이브리드 방식으로 처리)

    요청:
    - image: 음식 이미지 파일

    응답:
    - success: 성공 여부
    - message: 응답 메시지
    - food_count: 음식 개수 (1~3)
    - food1_name, food1_calories: 첫 번째 음식
    - food2_name, food2_calories: 두 번째 음식 (있을 경우)
    - food3_name, food3_calories: 세 번째 음식 (있을 경우)
    - total_calories: 총 칼로리

    하이브리드 방식:
    - CSV에 음식 이름이 있으면 → CSV 칼로리 사용 (더 정확)
    - CSV에 없으면 → 모델 예측 칼로리 사용
    """
    try:
        logger.info(f"음식 인식 요청 수신: {image.filename}")

        # 이미지 읽기
        contents = await image.read()

        # ============================================================
        # AI 모델 사용 (v2: 음식 이름 + 칼로리 반환)
        # ============================================================
        food_data = []  # [(name, model_calories), ...]

        if MODEL_AVAILABLE:
            try:
                logger.info("AI 모델로 음식 인식 중...")
                prediction = predict_food(contents)

                # 음식 데이터 추출 (이름 + 모델 예측 칼로리)
                if prediction.get("food1_name"):
                    food_data.append({
                        "name": prediction["food1_name"],
                        "model_calories": prediction.get("food1_calories")
                    })
                if prediction.get("food2_name"):
                    food_data.append({
                        "name": prediction["food2_name"],
                        "model_calories": prediction.get("food2_calories")
                    })
                if prediction.get("food3_name"):
                    food_data.append({
                        "name": prediction["food3_name"],
                        "model_calories": prediction.get("food3_calories")
                    })

                logger.info(f"AI 모델 예측 결과: {food_data}")

            except Exception as model_error:
                logger.error(f"모델 추론 중 오류: {model_error}")
                logger.warning("더미 데이터로 폴백합니다.")
                food_data = [
                    {"name": "쌀밥", "model_calories": 310},
                    {"name": "된장찌개", "model_calories": 140}
                ]

        # 더미 데이터 (모델 파일이 없거나 오류 발생 시)
        if not food_data:
            logger.info("더미 데이터 사용")
            food_data = [
                {"name": "쌀밥", "model_calories": 310},
                {"name": "된장찌개", "model_calories": 140}
            ]

        # ============================================================
        # 하이브리드 칼로리 처리 (CSV 우선, 없으면 모델 예측값)
        # ============================================================
        food_count = len(food_data)
        final_foods = []
        total_calories = 0

        if NUTRITION_DB_AVAILABLE:
            try:
                nutrition_db = get_nutrition_db()

                for food in food_data:
                    food_name = food["name"]
                    model_calories = food["model_calories"]

                    # CSV에서 칼로리 조회 시도
                    nutrition_info = nutrition_db.get_nutrition_info(food_name)

                    if nutrition_info:
                        # CSV에 있음 → CSV 칼로리 사용 (더 정확)
                        final_calories = int(nutrition_info['calories'])
                        logger.info(f"✅ CSV 매칭 성공: {food_name} = {final_calories}kcal (CSV)")
                    elif model_calories is not None:
                        # CSV에 없음 → 모델 예측 칼로리 사용
                        final_calories = model_calories
                        logger.info(f"⚠️ CSV 매칭 실패: {food_name} = {final_calories}kcal (모델 예측값 사용)")
                    else:
                        # 모델 칼로리도 없음 → null
                        final_calories = None
                        logger.warning(f"❌ 칼로리 정보 없음: {food_name}")

                    final_foods.append({
                        "name": food_name,
                        "calories": final_calories
                    })

                    if final_calories:
                        total_calories += final_calories

                logger.info(f"영양 정보 처리 완료: 총 칼로리 {total_calories}kcal")

            except Exception as db_error:
                logger.error(f"영양 정보 DB 처리 중 오류: {db_error}")
                logger.warning("모델 예측 칼로리를 사용합니다.")
                # DB 오류 시 모델 예측값 사용
                for food in food_data:
                    final_foods.append({
                        "name": food["name"],
                        "calories": food["model_calories"]
                    })
                    if food["model_calories"]:
                        total_calories += food["model_calories"]
        else:
            logger.warning("영양 정보 DB를 사용할 수 없습니다. 모델 예측 칼로리를 사용합니다.")
            # DB 없을 시 모델 예측값 사용
            for food in food_data:
                final_foods.append({
                    "name": food["name"],
                    "calories": food["model_calories"]
                })
                if food["model_calories"]:
                    total_calories += food["model_calories"]

        # 최대 3개까지
        final_foods = final_foods[:3]

        return FoodRecognitionResponse(
            success=True,
            message="음식 인식 완료",
            food_count=food_count,
            food1_name=final_foods[0]["name"] if len(final_foods) > 0 else None,
            food1_calories=final_foods[0]["calories"] if len(final_foods) > 0 else None,
            food2_name=final_foods[1]["name"] if len(final_foods) > 1 else None,
            food2_calories=final_foods[1]["calories"] if len(final_foods) > 1 else None,
            food3_name=final_foods[2]["name"] if len(final_foods) > 2 else None,
            food3_calories=final_foods[2]["calories"] if len(final_foods) > 2 else None,
            total_calories=total_calories
        )

    except Exception as e:
        logger.error(f"음식 인식 API 오류: {e}")
        raise HTTPException(status_code=500, detail=f"음식 인식 중 오류 발생: {str(e)}")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
