package com.example.backendspring.service;

import com.example.backendspring.dto.food.FoodRecognitionResponse;
import com.example.backendspring.dto.food.FoodSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodRecognitionService {
    
    @Value("${fastapi.service.url}")
    private String fastapiServiceUrl;
    
    private final WebClient.Builder webClientBuilder;
    
    /**
     * FastAPI 서비스와 연동하여 음식 인식
     * 
     * FastAPI 응답 형식:
     * {
     *   "success": true,
     *   "message": "음식 인식 완료",
     *   "food_count": 2,
     *   "food1_name": "김치찌개",
     *   "food1_calories": 200,
     *   "food2_name": "쌀밥",
     *   "food2_calories": 300,
     *   "food3_name": null,
     *   "food3_calories": null,
     *   "total_calories": 500
     * }
     */
    public FoodRecognitionResponse recognizeFood(MultipartFile imageFile) {
        try {
            log.info("음식 인식 시작 - 파일명: {}, 크기: {} bytes",
                    imageFile.getOriginalFilename(), imageFile.getSize());

            // ============================================================
            // FastAPI 서버와 통신하여 실제 음식 인식
            // ============================================================
            WebClient webClient = webClientBuilder
                    .baseUrl(fastapiServiceUrl)
                    .build();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("image", new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            });

            FoodRecognitionResponse response = webClient
                    .post()
                    .uri("/api/v1/food/recognize")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(FoodRecognitionResponse.class)
                    .block();

            log.info("FastAPI 응답 성공 - 음식 개수: {}, 총 칼로리: {}kcal",
                    response.getFoodCount(), response.getTotalCalories());

            return response;

        } catch (Exception e) {
            log.error("음식 인식 중 오류 발생", e);
            return FoodRecognitionResponse.builder()
                    .success(false)
                    .message("음식 인식 중 오류가 발생했습니다: " + e.getMessage())
                    .foodCount(0)
                    .totalCalories(0)
                    .build();
        }
    }

    /**
     * FastAPI 서비스와 연동하여 음식 칼로리 검색
     *
     * @param foodName 검색할 음식 이름
     * @return 음식 이름과 칼로리 정보
     */
    public FoodSearchResponse searchFood(String foodName) {
        try {
            log.info("음식 검색 시작 - 음식명: {}", foodName);

            WebClient webClient = webClientBuilder
                    .baseUrl(fastapiServiceUrl)
                    .build();

            Map<String, Object> response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/food/search")
                            .queryParam("foodName", foodName)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("foodName") && response.containsKey("calories")) {
                String foundName = (String) response.get("foodName");
                Integer foundCalories = ((Number) response.get("calories")).intValue();

                log.info("음식 검색 성공 - {}: {}kcal", foundName, foundCalories);
                return new FoodSearchResponse(foundName, foundCalories);
            } else {
                throw new RuntimeException("음식을 찾을 수 없습니다");
            }

        } catch (Exception e) {
            log.error("음식 검색 중 오류 발생 - 음식명: {}", foodName, e);
            throw new RuntimeException("음식을 찾을 수 없습니다: " + foodName);
        }
    }
}
