package com.example.backendspring.dto.calorie;

import com.example.backendspring.dto.meal.MealResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCalorieResponse {
    
    private LocalDate date;
    private Integer targetCalories;
    private Integer actualCalories;
    private Integer exceededCalories;
    private List<MealResponse> meals;
}

