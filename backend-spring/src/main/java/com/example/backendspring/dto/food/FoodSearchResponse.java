package com.example.backendspring.dto.food;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 음식 검색 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodSearchResponse {
    private String foodName;  // 음식 이름
    private Integer calories;  // 칼로리 (kcal)
}
