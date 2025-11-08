package com.example.backendspring.service;

import com.example.backendspring.entity.TrainingDataLog;
import com.example.backendspring.repository.TrainingDataLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingDataService {

    private final TrainingDataLogRepository trainingDataLogRepository;
    private final ImageStorageService imageStorageService;
    private final ObjectMapper objectMapper;

    /**
     * 학습 데이터 저장
     *
     * @param imageFile 원본 이미지 파일
     * @param userUniqueCode 유저 고유 코드
     * @param mealId 연관된 Meal ID
     * @param aiPrediction AI 모델의 예측 결과
     * @param userCorrectedData 사용자가 수정한 최종 데이터
     * @return 저장된 TrainingDataLog ID
     */
    @Transactional
    public Long saveTrainingData(
        MultipartFile imageFile,
        String userUniqueCode,
        Long mealId,
        Map<String, Object> aiPrediction,
        Map<String, Object> userCorrectedData
    ) {
        try {
            log.info("학습 데이터 저장 시작 - 유저: {}, Meal ID: {}", userUniqueCode, mealId);

            // 1. 이미지 파일 저장
            String imagePath = imageStorageService.saveImage(imageFile, userUniqueCode);

            // 2. AI 예측 결과를 JSON 문자열로 변환
            String aiPredictionJson = objectMapper.writeValueAsString(aiPrediction);

            // 3. 사용자 수정 데이터를 JSON 문자열로 변환
            String userCorrectedJson = objectMapper.writeValueAsString(userCorrectedData);

            // 4. TrainingDataLog 엔티티 생성 및 저장
            TrainingDataLog trainingDataLog = TrainingDataLog.builder()
                .userUniqueCode(userUniqueCode)
                .mealId(mealId)
                .imagePath(imagePath)
                .aiPrediction(aiPredictionJson)
                .userCorrectedJson(userCorrectedJson)
                .build();

            TrainingDataLog saved = trainingDataLogRepository.save(trainingDataLog);

            log.info("학습 데이터 저장 완료 - Log ID: {}, Image Path: {}", saved.getLogId(), imagePath);

            return saved.getLogId();

        } catch (IOException e) {
            log.error("학습 데이터 저장 실패 - 유저: {}, Meal ID: {}", userUniqueCode, mealId, e);
            throw new RuntimeException("학습 데이터 저장 중 오류 발생", e);
        }
    }

    /**
     * Meal 저장 시 자동으로 학습 데이터 수집
     *
     * @param imageFile 원본 이미지 파일
     * @param userUniqueCode 유저 고유 코드
     * @param mealId 저장된 Meal ID
     * @param aiPredictionResponse FastAPI에서 받은 AI 예측 응답
     * @param foodCount 음식 개수
     * @param food1Name 음식1 이름
     * @param food1Calories 음식1 칼로리
     * @param food2Name 음식2 이름
     * @param food2Calories 음식2 칼로리
     * @param food3Name 음식3 이름
     * @param food3Calories 음식3 칼로리
     */
    @Transactional
    public void collectTrainingDataFromMeal(
        MultipartFile imageFile,
        String userUniqueCode,
        Long mealId,
        Map<String, Object> aiPredictionResponse,
        Integer foodCount,
        String food1Name,
        Integer food1Calories,
        String food2Name,
        Integer food2Calories,
        String food3Name,
        Integer food3Calories
    ) {
        try {
            log.info("Meal 저장 시 학습 데이터 자동 수집 - Meal ID: {}", mealId);

            // AI 예측 결과 (원본)
            Map<String, Object> aiPrediction = new HashMap<>(aiPredictionResponse);

            // 사용자가 수정한 최종 데이터 (Ground Truth)
            Map<String, Object> userCorrectedData = new HashMap<>();
            userCorrectedData.put("food_count", foodCount);

            if (food1Name != null) {
                Map<String, Object> food1 = new HashMap<>();
                food1.put("name", food1Name);
                food1.put("calories", food1Calories);
                userCorrectedData.put("food1", food1);
            }

            if (food2Name != null) {
                Map<String, Object> food2 = new HashMap<>();
                food2.put("name", food2Name);
                food2.put("calories", food2Calories);
                userCorrectedData.put("food2", food2);
            }

            if (food3Name != null) {
                Map<String, Object> food3 = new HashMap<>();
                food3.put("name", food3Name);
                food3.put("calories", food3Calories);
                userCorrectedData.put("food3", food3);
            }

            // 학습 데이터 저장
            saveTrainingData(imageFile, userUniqueCode, mealId, aiPrediction, userCorrectedData);

            log.info("Meal 학습 데이터 자동 수집 완료 - Meal ID: {}", mealId);

        } catch (Exception e) {
            // 학습 데이터 수집 실패는 전체 트랜잭션을 롤백하지 않음 (로그만 남김)
            log.error("Meal 학습 데이터 자동 수집 실패 - Meal ID: {}", mealId, e);
        }
    }
}
