package com.example.backendspring.dto.meal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRequest {
    
    @NotBlank(message = "음식명은 필수입니다")
    private String name;
    
    @NotNull(message = "칼로리는 필수입니다")
    @Positive(message = "칼로리는 양수여야 합니다")
    private Integer calories;
    
    private String imageUrl; // 선택 사항
}

