package com.example.backendspring.service;

import com.example.backendspring.dto.meal.FoodResponse;
import com.example.backendspring.dto.meal.MealRequest;
import com.example.backendspring.dto.meal.MealResponse;
import com.example.backendspring.entity.Meal;
import com.example.backendspring.entity.User;
import com.example.backendspring.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealService {
    
    private final MealRepository mealRepository;
    private final AuthService authService;
    
    @Transactional
    public MealResponse createMeal(MealRequest request) {
        // 유저 존재 확인
        User user = authService.getUserByUniqueCode(request.getUserUniqueCode());
        
        // Meal 생성
        Meal meal = Meal.builder()
                .userUniqueCode(user.getUniqueCode())
                .mealDate(request.getMealDate())
                .mealTime(request.getMealTime())
                .mealType(request.getMealType())
                .foodCount(request.getFoodCount())
                .food1Name(request.getFood1Name())
                .food1Calories(request.getFood1Calories())
                .food2Name(request.getFood2Name())
                .food2Calories(request.getFood2Calories())
                .food3Name(request.getFood3Name())
                .food3Calories(request.getFood3Calories())
                .build();
        
        meal.calculateTotalCalories();
        
        Meal savedMeal = mealRepository.save(meal);
        return convertToMealResponse(savedMeal);
    }
    
    @Transactional
    public MealResponse updateMeal(Long mealId, MealRequest request) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("식사를 찾을 수 없습니다"));
        
        // 기본 정보 업데이트
        meal.setMealDate(request.getMealDate());
        meal.setMealTime(request.getMealTime());
        meal.setMealType(request.getMealType());
        meal.setFoodCount(request.getFoodCount());
        
        // 음식 정보 업데이트
        meal.setFood1Name(request.getFood1Name());
        meal.setFood1Calories(request.getFood1Calories());
        meal.setFood2Name(request.getFood2Name());
        meal.setFood2Calories(request.getFood2Calories());
        meal.setFood3Name(request.getFood3Name());
        meal.setFood3Calories(request.getFood3Calories());
        
        meal.calculateTotalCalories();
        
        Meal updatedMeal = mealRepository.save(meal);
        return convertToMealResponse(updatedMeal);
    }
    
    @Transactional
    public void deleteMeal(Long mealId) {
        if (!mealRepository.existsById(mealId)) {
            throw new IllegalArgumentException("식사를 찾을 수 없습니다");
        }
        mealRepository.deleteById(mealId);
    }
    
    public MealResponse getMealById(Long mealId) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("식사를 찾을 수 없습니다"));
        return convertToMealResponse(meal);
    }
    
    public List<MealResponse> getMealsByUserCodeAndDate(String userUniqueCode, LocalDate date) {
        List<Meal> meals = mealRepository.findByUserUniqueCodeAndMealDate(userUniqueCode, date);
        return meals.stream()
                .map(this::convertToMealResponse)
                .collect(Collectors.toList());
    }
    
    public List<MealResponse> getMealsByUserCodeAndDateRange(String userUniqueCode, LocalDate startDate, LocalDate endDate) {
        List<Meal> meals = mealRepository.findByUserUniqueCodeAndMealDateBetween(userUniqueCode, startDate, endDate);
        return meals.stream()
                .map(this::convertToMealResponse)
                .collect(Collectors.toList());
    }
    
    private MealResponse convertToMealResponse(Meal meal) {
        return MealResponse.builder()
                .id(meal.getId())
                .mealDate(meal.getMealDate())
                .mealTime(meal.getMealTime())
                .mealType(meal.getMealType())
                .mealTypeKorean(meal.getMealType().getKoreanName())
                .totalCalories(meal.getTotalCalories())
                .foodCount(meal.getFoodCount())
                .food1Name(meal.getFood1Name())
                .food1Calories(meal.getFood1Calories())
                .food2Name(meal.getFood2Name())
                .food2Calories(meal.getFood2Calories())
                .food3Name(meal.getFood3Name())
                .food3Calories(meal.getFood3Calories())
                .build();
    }
}
