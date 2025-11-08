package com.example.backendspring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "meals", indexes = {
    @Index(name = "idx_user_code_date", columnList = "user_unique_code, meal_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_unique_code", nullable = false, length = 10)
    private String userUniqueCode; // 유저 고유 코드
    
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate; // 식사 날짜
    
    @Column(name = "meal_time", nullable = false)
    private LocalTime mealTime; // 식사 시간
    
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType; // 아침, 점심, 저녁, 간식
    
    @Column(name = "food_count", nullable = false)
    private Integer foodCount; // 음식 개수 (1~3)
    
    // 음식 1
    @Column(name = "food1_name", length = 255)
    private String food1Name;
    
    @Column(name = "food1_calories")
    private Integer food1Calories;
    
    // 음식 2
    @Column(name = "food2_name", length = 255)
    private String food2Name;
    
    @Column(name = "food2_calories")
    private Integer food2Calories;
    
    // 음식 3
    @Column(name = "food3_name", length = 255)
    private String food3Name;
    
    @Column(name = "food3_calories")
    private Integer food3Calories;
    
    @Column(name = "total_calories")
    private Integer totalCalories; // 총 칼로리
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 총 칼로리 계산 메서드
    public void calculateTotalCalories() {
        int total = 0;
        if (food1Calories != null) total += food1Calories;
        if (food2Calories != null) total += food2Calories;
        if (food3Calories != null) total += food3Calories;
        this.totalCalories = total;
    }
}
