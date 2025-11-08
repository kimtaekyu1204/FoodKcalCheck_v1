package com.example.backendspring.controller;

import com.example.backendspring.dto.common.ApiResponse;
import com.example.backendspring.service.TrainingDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/training")
@RequiredArgsConstructor
public class TrainingDataController {

    private final TrainingDataService trainingDataService;
    private final ObjectMapper objectMapper;

    /**
     * 학습 데이터 수집 API
     * POST /api/training/collect
     *
     * Android에서 Meal 저장 시 자동으로 호출되어 학습 데이터를 수집합니다.
     *
     * @param image 원본 이미지 파일
     * @param userUniqueCode 유저 고유 코드
     * @param mealId 저장된 Meal ID
     * @param aiPredictionJson AI 모델의 예측 결과 (JSON)
     * @param userCorrectedJson 사용자가 수정한 최종 데이터 (JSON)
     * @return 저장된 Training Data Log ID
     */
    @PostMapping(value = "/collect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> collectTrainingData(
        @RequestParam("image") MultipartFile image,
        @RequestParam("userUniqueCode") String userUniqueCode,
        @RequestParam("mealId") Long mealId,
        @RequestParam("aiPrediction") String aiPredictionJson,
        @RequestParam("userCorrected") String userCorrectedJson
    ) {
        try {
            log.info("학습 데이터 수집 요청 - 유저: {}, Meal ID: {}", userUniqueCode, mealId);

            // 파일 검증
            if (image.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("이미지 파일이 비어있습니다"));
            }

            // JSON 문자열을 Map으로 변환
            Map<String, Object> aiPrediction = objectMapper.readValue(aiPredictionJson, Map.class);
            Map<String, Object> userCorrected = objectMapper.readValue(userCorrectedJson, Map.class);

            // 학습 데이터 저장
            Long logId = trainingDataService.saveTrainingData(
                image,
                userUniqueCode,
                mealId,
                aiPrediction,
                userCorrected
            );

            log.info("학습 데이터 수집 완료 - Log ID: {}", logId);

            return ResponseEntity.ok(ApiResponse.success("학습 데이터 수집 완료", logId));

        } catch (Exception e) {
            log.error("학습 데이터 수집 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("학습 데이터 수집 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
