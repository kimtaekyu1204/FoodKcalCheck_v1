package com.example.backendspring.controller;

import com.example.backendspring.dto.common.ApiResponse;
import com.example.backendspring.dto.food.FoodRecognitionResponse;
import com.example.backendspring.dto.food.FoodSearchResponse;
import com.example.backendspring.service.FoodRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/food")
@RequiredArgsConstructor
public class FoodRecognitionController {
    
    private final FoodRecognitionService foodRecognitionService;
    
    /**
     * 음식 인식 API
     * POST /api/food/recognize
     * 
     * Android 카메라에서 촬영한 이미지를 받아서
     * FastAPI 서버로 전달하여 음식을 인식합니다.
     * 
     * 사진 1장당 최대 3개의 음식까지 인식합니다.
     * 
     * @param image 촬영한 음식 이미지 파일
     * @param uniqueCode 사용자 고유 코드
     * @return 인식된 음식 정보 (음식 개수, 음식1~3 이름/칼로리)
     */
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FoodRecognitionResponse>> recognizeFood(
            @RequestParam("image") MultipartFile image,
            @RequestParam("userUniqueCode") String uniqueCode) {
        
        try {
            // 파일 검증
            if (image.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("이미지 파일이 비어있습니다"));
            }
            
            // 파일 크기 검증 (10MB 제한)
            if (image.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("이미지 파일 크기는 10MB를 초과할 수 없습니다"));
            }
            
            // 파일 타입 검증
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("이미지 파일만 업로드 가능합니다"));
            }
            
            log.info("음식 인식 요청 - 사용자 코드: {}, 파일명: {}, 크기: {} bytes", 
                    uniqueCode, image.getOriginalFilename(), image.getSize());
            
            // FastAPI 서버와 통신하여 음식 인식
            FoodRecognitionResponse response = foodRecognitionService.recognizeFood(image);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("음식 인식 완료", response));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(response.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("음식 인식 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("음식 인식 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 음식 칼로리 검색 API
     * GET /api/food/search?foodName={음식이름}
     *
     * 음식 이름으로 foodKcalList.csv에서 칼로리를 검색합니다.
     *
     * @param foodName 검색할 음식 이름
     * @return 음식 이름과 칼로리 정보
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<FoodSearchResponse>> searchFood(@RequestParam("foodName") String foodName) {
        try {
            log.info("음식 검색 요청 - 음식명: {}", foodName);

            FoodSearchResponse response = foodRecognitionService.searchFood(foodName);

            return ResponseEntity.ok(ApiResponse.success("검색 완료", response));

        } catch (RuntimeException e) {
            log.warn("음식 검색 실패 - 음식명: {}, 사유: {}", foodName, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("음식 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("음식 검색 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
