package com.example.android.data.model

import com.google.gson.annotations.SerializedName

// 식사 추가/수정 관련

data class MealRequest(
    val userUniqueCode: String,  // 유저 고유 코드
    val mealDate: String,  // "2025-11-07"
    val mealTime: String,  // "12:30:00"
    val mealType: String,  // "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    val foodCount: Int,  // 음식 개수 (1~3)
    val food1Name: String?,
    val food1Calories: Int?,
    val food2Name: String? = null,
    val food2Calories: Int? = null,
    val food3Name: String? = null,
    val food3Calories: Int? = null
)

data class FoodRequest(
    val name: String,
    val calories: Int,
    val imageUrl: String? = null
)

// 식사 타입 Enum
enum class MealType(val value: String, val korean: String) {
    BREAKFAST("BREAKFAST", "아침"),
    LUNCH("LUNCH", "점심"),
    DINNER("DINNER", "저녁"),
    SNACK("SNACK", "간식")
}
