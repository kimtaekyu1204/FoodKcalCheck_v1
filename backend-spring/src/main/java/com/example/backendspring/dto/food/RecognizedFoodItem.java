package com.example.backendspring.dto.food;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognizedFoodItem {
    
    private String name; // 음식명
    private Integer calories; // 칼로리
    private Double confidence; // 인식 신뢰도 (0.0 ~ 1.0)
}

