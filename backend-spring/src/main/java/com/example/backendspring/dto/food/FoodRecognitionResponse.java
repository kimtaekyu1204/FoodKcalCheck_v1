package com.example.backendspring.dto.food;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRecognitionResponse {
    
    private boolean success;
    private String message;
    
    @JsonProperty("food_count")
    private Integer foodCount; // 음식 개수 (1~3)
    
    @JsonProperty("food1_name")
    private String food1Name;
    
    @JsonProperty("food1_calories")
    private Integer food1Calories;
    
    @JsonProperty("food2_name")
    private String food2Name;
    
    @JsonProperty("food2_calories")
    private Integer food2Calories;
    
    @JsonProperty("food3_name")
    private String food3Name;
    
    @JsonProperty("food3_calories")
    private Integer food3Calories;
    
    @JsonProperty("total_calories")
    private Integer totalCalories;
}
