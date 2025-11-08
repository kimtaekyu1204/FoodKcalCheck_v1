package com.example.android.data.model

import com.google.gson.annotations.SerializedName

// 음식 인식 관련

data class FoodRecognitionResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("food_count") val foodCount: Int,  // 음식 개수 (1~3)
    @SerializedName("food1_name") val food1Name: String?,
    @SerializedName("food1_calories") val food1Calories: Int?,
    @SerializedName("food2_name") val food2Name: String?,
    @SerializedName("food2_calories") val food2Calories: Int?,
    @SerializedName("food3_name") val food3Name: String?,
    @SerializedName("food3_calories") val food3Calories: Int?,
    @SerializedName("total_calories") val totalCalories: Int
) {
    // UI 표시용 음식 리스트 변환
    fun toFoodList(): List<RecognizedFoodItem> {
        val foods = mutableListOf<RecognizedFoodItem>()
        
        if (food1Name != null && food1Calories != null) {
            foods.add(RecognizedFoodItem(food1Name, food1Calories, 0.95))
        }
        if (food2Name != null && food2Calories != null) {
            foods.add(RecognizedFoodItem(food2Name, food2Calories, 0.90))
        }
        if (food3Name != null && food3Calories != null) {
            foods.add(RecognizedFoodItem(food3Name, food3Calories, 0.85))
        }
        
        return foods
    }
}

// UI 표시용 (하위 호환성)
data class RecognizedFoodItem(
    val name: String,
    val calories: Int,
    val confidence: Double
)

// 음식 검색 응답
data class FoodSearchResponse(
    val foodName: String,
    val calories: Int
)
