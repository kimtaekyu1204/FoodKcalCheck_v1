package com.example.backendspring.controller;

import com.example.backendspring.dto.common.ApiResponse;
import com.example.backendspring.dto.meal.MealRequest;
import com.example.backendspring.dto.meal.MealResponse;
import com.example.backendspring.service.MealService;
import com.example.backendspring.service.TrainingDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final TrainingDataService trainingDataService;
    private final ObjectMapper objectMapper;
    
    /**
     * 식사 추가
     * POST /api/meals
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MealResponse>> createMeal(@Valid @RequestBody MealRequest request) {
        try {
            MealResponse response = mealService.createMeal(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("식사가 추가되었습니다", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("식사 추가 중 오류가 발생했습니다"));
        }
    }

    /**
     * 식사 추가 + 학습 데이터 수집 (카메라 사용 시)
     * POST /api/meals/with-training-data
     *
     * 카메라로 음식을 촬영하여 Meal을 저장할 때 자동으로 학습 데이터를 수집합니다.
     *
     * @param image 원본 이미지 파일
     * @param aiPredictionJson AI 모델의 예측 결과 (JSON 문자열)
     * @param mealRequestJson Meal 생성 요청 데이터 (JSON 문자열)
     * @return 생성된 Meal 정보
     */
    @PostMapping(value = "/with-training-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MealResponse>> createMealWithTrainingData(
        @RequestParam("image") MultipartFile image,
        @RequestParam("aiPrediction") String aiPredictionJson,
        @RequestParam("mealRequest") String mealRequestJson
    ) {
        try {
            log.info("학습 데이터 포함 식사 추가 요청 - 이미지: {}", image.getOriginalFilename());

            // JSON 파싱
            MealRequest mealRequest = objectMapper.readValue(mealRequestJson, MealRequest.class);
            Map<String, Object> aiPrediction = objectMapper.readValue(aiPredictionJson, Map.class);

            // 1. Meal 생성
            MealResponse mealResponse = mealService.createMeal(mealRequest);
            log.info("Meal 생성 완료 - ID: {}", mealResponse.getId());

            // 2. 사용자가 수정한 최종 데이터 (Ground Truth)
            Map<String, Object> userCorrectedData = new HashMap<>();
            userCorrectedData.put("food_count", mealRequest.getFoodCount());

            if (mealRequest.getFood1Name() != null) {
                Map<String, Object> food1 = new HashMap<>();
                food1.put("name", mealRequest.getFood1Name());
                food1.put("calories", mealRequest.getFood1Calories());
                userCorrectedData.put("food1", food1);
            }

            if (mealRequest.getFood2Name() != null) {
                Map<String, Object> food2 = new HashMap<>();
                food2.put("name", mealRequest.getFood2Name());
                food2.put("calories", mealRequest.getFood2Calories());
                userCorrectedData.put("food2", food2);
            }

            if (mealRequest.getFood3Name() != null) {
                Map<String, Object> food3 = new HashMap<>();
                food3.put("name", mealRequest.getFood3Name());
                food3.put("calories", mealRequest.getFood3Calories());
                userCorrectedData.put("food3", food3);
            }

            // 3. 학습 데이터 저장 (비동기 처리로 실패해도 Meal은 저장됨)
            try {
                Long logId = trainingDataService.saveTrainingData(
                    image,
                    mealRequest.getUserUniqueCode(),
                    mealResponse.getId(),
                    aiPrediction,
                    userCorrectedData
                );
                log.info("학습 데이터 수집 완료 - Log ID: {}", logId);
            } catch (Exception e) {
                log.error("학습 데이터 수집 실패 (Meal은 저장됨) - Meal ID: {}", mealResponse.getId(), e);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("식사가 추가되었습니다", mealResponse));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("식사 추가 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("식사 추가 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 식사 수정
     * PUT /api/meals/{mealId}
     */
    @PutMapping("/{mealId}")
    public ResponseEntity<ApiResponse<MealResponse>> updateMeal(
            @PathVariable Long mealId,
            @Valid @RequestBody MealRequest request) {
        try {
            MealResponse response = mealService.updateMeal(mealId, request);
            return ResponseEntity.ok(ApiResponse.success("식사가 수정되었습니다", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("식사 수정 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 식사 삭제
     * DELETE /api/meals/{mealId}
     */
    @DeleteMapping("/{mealId}")
    public ResponseEntity<ApiResponse<String>> deleteMeal(@PathVariable Long mealId) {
        try {
            mealService.deleteMeal(mealId);
            return ResponseEntity.ok(ApiResponse.success("식사가 삭제되었습니다", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("식사 삭제 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 식사 단건 조회
     * GET /api/meals/{mealId}
     */
    @GetMapping("/{mealId}")
    public ResponseEntity<ApiResponse<MealResponse>> getMeal(@PathVariable Long mealId) {
        try {
            MealResponse response = mealService.getMealById(mealId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("식사 조회 중 오류가 발생했습니다"));
        }
    }
    
    /**
     * 특정 날짜의 식사 조회
     * GET /api/meals/user/{uniqueCode}/date/{date}
     */
    @GetMapping("/user/{uniqueCode}/date/{date}")
    public ResponseEntity<ApiResponse<List<MealResponse>>> getMealsByDate(
            @PathVariable String uniqueCode,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<MealResponse> response = mealService.getMealsByUserCodeAndDate(uniqueCode, date);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("식사 조회 중 오류가 발생했습니다"));
        }
    }
}
