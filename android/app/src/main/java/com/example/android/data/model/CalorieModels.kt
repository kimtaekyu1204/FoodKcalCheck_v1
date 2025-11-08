package com.example.android.data.model

// 칼로리 조회 관련

data class MonthlyCalorieResponse(
    val year: Int,
    val month: Int,
    val targetCalories: Int,
    val dailyCalories: Map<String, Int>  // "2025-11-07" -> 1650
)

data class DailyCalorieResponse(
    val date: String,
    val targetCalories: Int,
    val actualCalories: Int,
    val exceededCalories: Int,
    val meals: List<MealResponse>
)

data class MealResponse(
    val id: Long,
    val mealDate: String,
    val mealTime: String,
    val mealType: String,  // BREAKFAST, LUNCH, DINNER, SNACK
    val mealTypeKorean: String,  // 아침, 점심, 저녁, 간식
    val totalCalories: Int,
    val foods: List<FoodResponse>
)

data class FoodResponse(
    val id: Long,
    val name: String,
    val calories: Int,
    val imageUrl: String?
)
