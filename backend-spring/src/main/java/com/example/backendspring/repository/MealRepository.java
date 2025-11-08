package com.example.backendspring.repository;

import com.example.backendspring.entity.Meal;
import com.example.backendspring.entity.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    
    // 특정 사용자의 특정 날짜 식사 조회
    List<Meal> findByUserUniqueCodeAndMealDate(String userUniqueCode, LocalDate mealDate);
    
    // 특정 사용자의 특정 기간 식사 조회
    List<Meal> findByUserUniqueCodeAndMealDateBetween(String userUniqueCode, LocalDate startDate, LocalDate endDate);
    
    // 특정 사용자의 특정 날짜, 특정 타입 식사 조회
    List<Meal> findByUserUniqueCodeAndMealDateAndMealType(String userUniqueCode, LocalDate mealDate, MealType mealType);
    
    // 특정 날짜의 총 칼로리 계산
    @Query("SELECT SUM(m.totalCalories) FROM Meal m WHERE m.userUniqueCode = :userUniqueCode AND m.mealDate = :mealDate")
    Integer getTotalCaloriesByUserCodeAndDate(@Param("userUniqueCode") String userUniqueCode, @Param("mealDate") LocalDate mealDate);
    
    // 특정 월의 날짜별 칼로리 합계
    @Query("SELECT m.mealDate, SUM(m.totalCalories) FROM Meal m " +
           "WHERE m.userUniqueCode = :userUniqueCode AND m.mealDate BETWEEN :startDate AND :endDate " +
           "GROUP BY m.mealDate ORDER BY m.mealDate")
    List<Object[]> getDailyCaloriesSummary(@Param("userUniqueCode") String userUniqueCode, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);
}

