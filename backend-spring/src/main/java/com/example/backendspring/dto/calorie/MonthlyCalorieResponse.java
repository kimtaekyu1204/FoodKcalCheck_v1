package com.example.backendspring.dto.calorie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyCalorieResponse {
    
    private Integer year;
    private Integer month;
    private Integer targetCalories;
    private Map<LocalDate, Integer> dailyCalories; // 날짜별 칼로리 맵
}

