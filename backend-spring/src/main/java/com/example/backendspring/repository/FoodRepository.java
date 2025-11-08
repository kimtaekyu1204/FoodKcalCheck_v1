package com.example.backendspring.repository;

import com.example.backendspring.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    
    // 특정 식사의 음식 조회
    List<Food> findByMealId(Long mealId);
}

