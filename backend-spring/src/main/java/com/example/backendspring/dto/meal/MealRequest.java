package com.example.backendspring.dto.meal;

import com.example.backendspring.entity.MealType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealRequest {
    
    @NotNull(message = "사용자 고유 코드는 필수입니다")
    private String userUniqueCode;
    
    @NotNull(message = "식사 날짜는 필수입니다")
    private LocalDate mealDate;
    
    @NotNull(message = "식사 시간은 필수입니다")
    private LocalTime mealTime;
    
    @NotNull(message = "식사 타입은 필수입니다")
    private MealType mealType;
    
    @NotNull(message = "음식 개수는 필수입니다")
    @Min(value = 1, message = "음식 개수는 최소 1개입니다")
    @Max(value = 3, message = "음식 개수는 최대 3개입니다")
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
}
