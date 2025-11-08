"""
영양 정보 DB 로더 모듈

foodKcalList.csv 파일을 로드하여 음식 이름으로 영양 정보를 조회합니다.
"""

import csv
import logging
from pathlib import Path
from typing import Dict, Optional

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 설정 파일 경로
BASE_DIR = Path(__file__).parent.parent
NUTRITION_DB_PATH = BASE_DIR / "foodKcalList.csv"


class NutritionDB:
    """영양 정보 DB 클래스"""

    def __init__(self):
        """영양 정보 DB 초기화"""
        self.nutrition_data: Dict[str, float] = {}
        self.loaded = False
        self._load_nutrition_data()

    def _load_nutrition_data(self):
        """foodKcalList.csv 파일 로드"""
        try:
            if not NUTRITION_DB_PATH.exists():
                logger.warning(f"영양 정보 파일을 찾을 수 없습니다: {NUTRITION_DB_PATH}")
                return

            with open(NUTRITION_DB_PATH, 'r', encoding='utf-8-sig') as f:  # BOM 처리를 위해 utf-8-sig 사용
                reader = csv.DictReader(f)
                for row in reader:
                    food_name = row.get('음 식 명', '').strip()
                    calories_str = row.get('에너지(kcal)', '').strip()

                    if food_name and calories_str:
                        try:
                            calories = float(calories_str)
                            # 공백 제거된 음식 이름으로 저장
                            self.nutrition_data[food_name] = calories
                        except ValueError:
                            logger.warning(f"칼로리 파싱 실패: {food_name} -> {calories_str}")

            self.loaded = True
            logger.info(f"영양 정보 로드 완료: {len(self.nutrition_data)}개 음식")

        except Exception as e:
            logger.error(f"영양 정보 로드 중 오류: {e}")
            self.loaded = False

    def get_calories(self, food_name: str) -> Optional[float]:
        """
        음식 이름으로 칼로리 조회

        Args:
            food_name: 음식 이름

        Returns:
            칼로리 값 (kcal), 없으면 None
        """
        if not self.loaded:
            logger.warning("영양 정보 DB가 로드되지 않았습니다.")
            return None

        # 정확한 매칭 시도
        if food_name in self.nutrition_data:
            return self.nutrition_data[food_name]

        # 공백 제거 후 매칭 시도
        food_name_stripped = food_name.strip()
        if food_name_stripped in self.nutrition_data:
            return self.nutrition_data[food_name_stripped]

        # 부분 매칭 시도 (공백 포함/제외)
        for key, value in self.nutrition_data.items():
            if key.strip() == food_name_stripped or key == food_name_stripped:
                return value

        logger.warning(f"음식 이름을 찾을 수 없습니다: {food_name}")
        return None

    def get_nutrition_info(self, food_name: str) -> Optional[Dict[str, float]]:
        """
        음식 이름으로 영양 정보 조회 (현재는 칼로리만)

        Args:
            food_name: 음식 이름

        Returns:
            영양 정보 딕셔너리 {'calories': float}, 없으면 None
        """
        calories = self.get_calories(food_name)
        if calories is None:
            return None

        return {
            'calories': calories
        }

    def get_multiple_nutrition_info(self, food_names: list) -> Dict[str, Optional[Dict[str, float]]]:
        """
        여러 음식 이름으로 영양 정보 일괄 조회

        Args:
            food_names: 음식 이름 리스트

        Returns:
            {음식이름: 영양정보} 딕셔너리
        """
        result = {}
        for food_name in food_names:
            if food_name:
                result[food_name] = self.get_nutrition_info(food_name)
        return result


# 싱글톤 인스턴스
_nutrition_db_instance: Optional[NutritionDB] = None


def get_nutrition_db() -> NutritionDB:
    """영양 정보 DB 인스턴스 가져오기 (싱글톤 패턴)"""
    global _nutrition_db_instance

    if _nutrition_db_instance is None:
        logger.info("영양 정보 DB 인스턴스 생성 중...")
        _nutrition_db_instance = NutritionDB()

    return _nutrition_db_instance

