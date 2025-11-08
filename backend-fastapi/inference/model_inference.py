"""
음식 인식 모델 추론 모듈

이 파일은 학습된 AI 모델을 로드하고 추론을 수행합니다.
"""

import os
import json
import numpy as np
from typing import Dict, List, Optional, Tuple
from pathlib import Path
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 설정 파일 경로
BASE_DIR = Path(__file__).parent.parent
MODEL_DIR = BASE_DIR / "models"
CONFIG_DIR = BASE_DIR / "config"

# 모델 파일 경로 (v2 모델 우선)
MODEL_PATH_V2 = MODEL_DIR / "food_model_v2.onnx"  # AI 모델팀 v2 스펙
MODEL_PATH = MODEL_DIR / "food_recognition_model.onnx"  # fallback
PYTORCH_MODEL_PATH = MODEL_DIR / "food_recognition_model.pt"  # fallback

# 설정 파일 (idx_to_class.json은 models/ 또는 config/ 폴더에 있을 수 있음)
IDX_TO_CLASS_PATH_MODELS = MODEL_DIR / "idx_to_class.json"
IDX_TO_CLASS_PATH_CONFIG = CONFIG_DIR / "idx_to_class.json"
CLASS_LABELS_PATH = CONFIG_DIR / "class_labels.json"  # fallback


class FoodRecognitionModel:
    """음식 인식 모델 클래스"""

    def __init__(self):
        """모델 초기화"""
        self.session = None
        self.idx_to_class = {}  # idx_to_class.json 매핑
        self.model_loaded = False

        # 설정 파일 로드
        self._load_config()

        # 모델 로드 시도
        self._load_model()

    def _load_config(self):
        """설정 파일 로드 (idx_to_class.json)"""
        try:
            # idx_to_class.json 파일 찾기 (models/ 우선, 없으면 config/)
            idx_to_class_path = None
            if IDX_TO_CLASS_PATH_MODELS.exists():
                idx_to_class_path = IDX_TO_CLASS_PATH_MODELS
            elif IDX_TO_CLASS_PATH_CONFIG.exists():
                idx_to_class_path = IDX_TO_CLASS_PATH_CONFIG
            elif CLASS_LABELS_PATH.exists():
                # fallback: 기존 class_labels.json 사용
                idx_to_class_path = CLASS_LABELS_PATH
                logger.info("idx_to_class.json을 찾을 수 없어 class_labels.json을 사용합니다.")

            if idx_to_class_path:
                with open(idx_to_class_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    # '_'로 시작하는 메타데이터 제외
                    self.idx_to_class = {
                        int(k): v for k, v in data.items()
                        if not k.startswith('_')
                    }
                logger.info(f"idx_to_class 매핑 로드 완료: {len(self.idx_to_class)}개 (경로: {idx_to_class_path})")
            else:
                logger.warning(f"idx_to_class.json 파일을 찾을 수 없습니다. models/ 또는 config/ 폴더를 확인하세요.")
                logger.warning(f"확인 경로: {IDX_TO_CLASS_PATH_MODELS}, {IDX_TO_CLASS_PATH_CONFIG}")

        except Exception as e:
            logger.error(f"설정 파일 로드 중 오류: {e}")

    def _load_model(self):
        """모델 파일 로드 (v2 모델 우선)"""
        try:
            # v2 ONNX 모델 우선 시도
            if MODEL_PATH_V2.exists():
                logger.info(f"v2 ONNX 모델 로드 중: {MODEL_PATH_V2}")
                self._load_onnx_model(MODEL_PATH_V2)
                return

            # 기존 ONNX 모델 시도
            if MODEL_PATH.exists():
                logger.info(f"ONNX 모델 로드 중: {MODEL_PATH}")
                self._load_onnx_model(MODEL_PATH)
                return

            # PyTorch 모델 시도
            if PYTORCH_MODEL_PATH.exists():
                logger.info(f"PyTorch 모델 로드 중: {PYTORCH_MODEL_PATH}")
                self._load_pytorch_model()
                return

            logger.warning("모델 파일을 찾을 수 없습니다. 더미 모드로 실행됩니다.")
            logger.warning(f"모델 경로를 확인하세요: {MODEL_DIR}")
            logger.warning(f"v2 모델 경로: {MODEL_PATH_V2}")

        except Exception as e:
            logger.error(f"모델 로드 중 오류: {e}")
            logger.warning("더미 모드로 실행됩니다.")

    def _load_onnx_model(self, model_path: Path):
        """ONNX 모델 로드"""
        try:
            import onnxruntime as ort

            # ONNX 런타임 세션 생성
            self.session = ort.InferenceSession(
                str(model_path),
                providers=['CPUExecutionProvider']  # GPU: ['CUDAExecutionProvider', 'CPUExecutionProvider']
            )

            # 입력/출력 정보
            input_name = self.session.get_inputs()[0].name
            input_shape = self.session.get_inputs()[0].shape

            logger.info(f"ONNX 모델 로드 완료: {model_path.name}")
            logger.info(f"입력 이름: {input_name}, 입력 크기: {input_shape}")

            self.model_loaded = True
            self.model_type = "onnx"

        except ImportError:
            logger.error("onnxruntime 패키지가 설치되지 않았습니다: pip install onnxruntime")
            raise
        except Exception as e:
            logger.error(f"ONNX 모델 로드 실패: {e}")
            raise

    def _load_pytorch_model(self):
        """PyTorch 모델 로드"""
        try:
            import torch

            # PyTorch 모델 로드
            self.model = torch.load(str(PYTORCH_MODEL_PATH), map_location='cpu')
            self.model.eval()

            logger.info(f"PyTorch 모델 로드 완료")

            self.model_loaded = True
            self.model_type = "pytorch"

        except ImportError:
            logger.error("torch 패키지가 설치되지 않았습니다: pip install torch")
            raise
        except Exception as e:
            logger.error(f"PyTorch 모델 로드 실패: {e}")
            raise

    def preprocess_image(self, image_bytes: bytes) -> np.ndarray:
        """
        이미지 전처리

        Args:
            image_bytes: 이미지 바이트 데이터

        Returns:
            전처리된 이미지 텐서 [1, 3, 384, 384]
        """
        try:
            from PIL import Image
            import io

            # 이미지 로드
            image = Image.open(io.BytesIO(image_bytes)).convert('RGB')

            # 리사이즈 (모델 입력 크기에 맞게 조정)
            image = image.resize((384, 384))

            # numpy 배열로 변환
            image_array = np.array(image).astype(np.float32)

            # 정규화 (ImageNet 통계)
            mean = np.array([0.485, 0.456, 0.406], dtype=np.float32) * 255
            std = np.array([0.229, 0.224, 0.225], dtype=np.float32) * 255
            image_array = (image_array - mean) / std

            # 채널 순서 변경: HWC -> CHW
            image_array = np.transpose(image_array, (2, 0, 1))

            # 배치 차원 추가: CHW -> NCHW
            image_array = np.expand_dims(image_array, axis=0).astype(np.float32)

            return image_array

        except Exception as e:
            logger.error(f"이미지 전처리 중 오류: {e}")
            raise

    def predict(self, image_bytes: bytes, top_k: int = 3) -> Dict:
        """
        음식 인식 추론 (v2: 음식 이름 + 칼로리 반환)

        Args:
            image_bytes: 이미지 바이트 데이터
            top_k: 상위 k개 결과 반환 (최대 3)

        Returns:
            {
                "food_count": int,
                "food1_name": str,
                "food1_calories": int,
                "food2_name": str | None,
                "food2_calories": int | None,
                "food3_name": str | None,
                "food3_calories": int | None,
                "confidence_scores": List[float]
            }
        """
        # 모델이 로드되지 않았으면 더미 데이터 반환
        if not self.model_loaded:
            return self._dummy_prediction()

        try:
            # 이미지 전처리
            input_tensor = self.preprocess_image(image_bytes)

            # 모델 추론
            if self.model_type == "onnx":
                predictions = self._predict_onnx(input_tensor)
            elif self.model_type == "pytorch":
                predictions = self._predict_pytorch(input_tensor)
            else:
                return self._dummy_prediction()

            # v2 모델: 멀티태스크 출력 (분류 + 회귀)
            # predictions[0]: 음식 이름 확률 (classification)
            # predictions[1]: 칼로리 예측값 (regression) - 모델이 있을 경우

            # 상위 k개 결과 추출
            classification_output = predictions[0]
            top_k_indices = np.argsort(classification_output)[::-1][:top_k]
            top_k_scores = classification_output[top_k_indices]

            # 칼로리 예측값 (v2 모델이면 있을 수 있음, 없으면 None)
            calorie_predictions = None
            if len(predictions) > 1:
                calorie_predictions = predictions[1]

            # 결과 포맷팅
            result = self._format_prediction(top_k_indices, top_k_scores, calorie_predictions)

            return result

        except Exception as e:
            logger.error(f"추론 중 오류 발생: {e}")
            return self._dummy_prediction()

    def _predict_onnx(self, input_tensor: np.ndarray) -> np.ndarray:
        """ONNX 모델 추론"""
        input_name = self.session.get_inputs()[0].name
        outputs = self.session.run(None, {input_name: input_tensor})
        return outputs[0]

    def _predict_pytorch(self, input_tensor: np.ndarray) -> np.ndarray:
        """PyTorch 모델 추론"""
        import torch

        input_tensor = torch.from_numpy(input_tensor)
        with torch.no_grad():
            outputs = self.model(input_tensor)
        return outputs.numpy()

    def _format_prediction(self, indices: np.ndarray, scores: np.ndarray, calorie_predictions: Optional[np.ndarray] = None) -> Dict:
        """
        예측 결과 포맷팅 (v2: 음식 이름 + 칼로리 반환)

        Args:
            indices: 클래스 인덱스 배열
            scores: 신뢰도 점수 배열
            calorie_predictions: 칼로리 예측값 배열 (v2 모델의 경우)

        Returns:
            포맷팅된 결과 딕셔너리 (음식 이름 + 칼로리 포함)
        """
        foods = []
        confidence_scores = []

        for i, (idx, score) in enumerate(zip(indices, scores)):
            # idx_to_class 매핑으로 음식 이름 가져오기
            food_name = self.idx_to_class.get(int(idx), f"Unknown_{idx}")

            # 칼로리 예측값 가져오기 (v2 모델)
            predicted_calories = None
            if calorie_predictions is not None and i < len(calorie_predictions):
                # 칼로리 예측값을 정수로 변환
                predicted_calories = int(calorie_predictions[i])

            foods.append({
                "name": food_name,
                "calories": predicted_calories,
                "confidence": float(score)
            })

            confidence_scores.append(float(score))

        # 최대 3개까지만
        foods = foods[:3]

        # 결과 딕셔너리 생성 (칼로리 포함)
        result = {
            "food_count": len(foods),
            "food1_name": foods[0]["name"] if len(foods) > 0 else None,
            "food1_calories": foods[0]["calories"] if len(foods) > 0 else None,
            "food2_name": foods[1]["name"] if len(foods) > 1 else None,
            "food2_calories": foods[1]["calories"] if len(foods) > 1 else None,
            "food3_name": foods[2]["name"] if len(foods) > 2 else None,
            "food3_calories": foods[2]["calories"] if len(foods) > 2 else None,
            "confidence_scores": confidence_scores
        }

        return result

    def _dummy_prediction(self) -> Dict:
        """더미 예측 결과 (모델 로드 실패 시) - v2: 음식 이름 + 칼로리"""
        logger.warning("모델이 로드되지 않아 더미 데이터를 반환합니다.")

        return {
            "food_count": 2,
            "food1_name": "쌀밥",
            "food1_calories": 310,
            "food2_name": "된장찌개",
            "food2_calories": 140,
            "food3_name": None,
            "food3_calories": None,
            "confidence_scores": [0.85, 0.72]
        }


# 싱글톤 모델 인스턴스
_model_instance: Optional[FoodRecognitionModel] = None


def get_model() -> FoodRecognitionModel:
    """모델 인스턴스 가져오기 (싱글톤 패턴)"""
    global _model_instance

    if _model_instance is None:
        logger.info("모델 인스턴스 생성 중...")
        _model_instance = FoodRecognitionModel()

    return _model_instance


def predict_food(image_bytes: bytes) -> Dict:
    """
    음식 인식 추론 (편의 함수)

    Args:
        image_bytes: 이미지 바이트 데이터

    Returns:
        예측 결과 딕셔너리
    """
    model = get_model()
    return model.predict(image_bytes)
