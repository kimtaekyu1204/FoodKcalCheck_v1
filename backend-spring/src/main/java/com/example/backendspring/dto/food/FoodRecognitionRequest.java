package com.example.backendspring.dto.food;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRecognitionRequest {
    
    // 이미지 파일은 MultipartFile로 받음
    private String userUniqueCode;
}
