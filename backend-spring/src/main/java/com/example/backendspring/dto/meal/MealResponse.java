package com.example.backendspring.dto.meal;

import com.example.backendspring.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealResponse {
    
    private Long id;
    private LocalDate mealDate;
    private LocalTime mealTime;
    private MealType mealType;
    private String mealTypeKorean; // 아침, 점심, 저녁, 간식
    private Integer totalCalories;
    private Integer foodCount;
    
    // 음식 1
    private String food1Name;
    private Integer food1Calories;
    
    // 음식 2
    private String food2Name;
    private Integer food2Calories;
    
    // 음식 3
    private String food3Name;
    private Integer food3Calories;
    
    // UI 표시용 음식 리스트 (하위 호환성)
    private List<FoodResponse> foods;
    
    // foods 리스트 자동 생성
    public List<FoodResponse> getFoods() {
        if (foods == null) {
            foods = new ArrayList<>();
            if (food1Name != null && food1Calories != null) {
                foods.add(FoodResponse.builder()
                        .id(1L)
                        .name(food1Name)
                        .calories(food1Calories)
                        .imageUrl(null)
                        .build());
            }
            if (food2Name != null && food2Calories != null) {
                foods.add(FoodResponse.builder()
                        .id(2L)
                        .name(food2Name)
                        .calories(food2Calories)
                        .imageUrl(null)
                        .build());
            }
            if (food3Name != null && food3Calories != null) {
                foods.add(FoodResponse.builder()
                        .id(3L)
                        .name(food3Name)
                        .calories(food3Calories)
                        .imageUrl(null)
                        .build());
            }
        }
        return foods;
    }
}
