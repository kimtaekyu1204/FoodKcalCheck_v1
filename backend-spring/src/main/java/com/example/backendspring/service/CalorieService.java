package com.example.backendspring.service;

import com.example.backendspring.dto.calorie.DailyCalorieResponse;
import com.example.backendspring.dto.calorie.MonthlyCalorieResponse;
import com.example.backendspring.dto.meal.MealResponse;
import com.example.backendspring.entity.User;
import com.example.backendspring.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalorieService {
    
    private final MealRepository mealRepository;
    private final MealService mealService;
    private final AuthService authService;
    
    public DailyCalorieResponse getDailyCalories(String userUniqueCode, LocalDate date) {
        User user = authService.getUserByUniqueCode(userUniqueCode);
        
        // 해당 날짜의 모든 식사 조회
        List<MealResponse> meals = mealService.getMealsByUserCodeAndDate(userUniqueCode, date);
        
        // 총 칼로리 계산
        int actualCalories = meals.stream()
                .mapToInt(MealResponse::getTotalCalories)
                .sum();
        
        int targetCalories = user.getDailyCalorieGoal() != null ? 
                user.getDailyCalorieGoal() : 2000;
        
        int exceededCalories = actualCalories - targetCalories;
        
        return DailyCalorieResponse.builder()
                .date(date)
                .targetCalories(targetCalories)
                .actualCalories(actualCalories)
                .exceededCalories(exceededCalories)
                .meals(meals)
                .build();
    }
    
    public MonthlyCalorieResponse getMonthlyCalories(String userUniqueCode, Integer year, Integer month) {
        User user = authService.getUserByUniqueCode(userUniqueCode);
        
        // 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 날짜별 칼로리 조회
        List<Object[]> dailySummary = mealRepository.getDailyCaloriesSummary(userUniqueCode, startDate, endDate);
        
        Map<LocalDate, Integer> dailyCalories = new HashMap<>();
        for (Object[] row : dailySummary) {
            LocalDate date = (LocalDate) row[0];
            Long calories = (Long) row[1];
            dailyCalories.put(date, calories.intValue());
        }
        
        int targetCalories = user.getDailyCalorieGoal() != null ? 
                user.getDailyCalorieGoal() : 2000;
        
        return MonthlyCalorieResponse.builder()
                .year(year)
                .month(month)
                .targetCalories(targetCalories)
                .dailyCalories(dailyCalories)
                .build();
    }
}
