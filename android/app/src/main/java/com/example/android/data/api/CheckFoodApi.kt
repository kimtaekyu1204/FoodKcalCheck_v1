package com.example.android.data.api

import com.example.android.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface CheckFoodApi {

    // ============= 인증 관련 =============

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): ApiResponse<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    @PUT("auth/users/{uniqueCode}/goal")
    suspend fun updateDailyCalorieGoal(
        @Path("uniqueCode") uniqueCode: String,
        @Query("dailyCalorieGoal") dailyCalorieGoal: Int
    ): ApiResponse<String?>

    // ============= 칼로리 조회 =============

    @GET("calories/monthly/{uniqueCode}/{year}/{month}")
    suspend fun getMonthlyCalories(
        @Path("uniqueCode") uniqueCode: String,
        @Path("year") year: Int,
        @Path("month") month: Int
    ): ApiResponse<MonthlyCalorieResponse>

    @GET("calories/daily/{uniqueCode}/{date}")
    suspend fun getDailyCalories(
        @Path("uniqueCode") uniqueCode: String,
        @Path("date") date: String  // "2025-11-07"
    ): ApiResponse<DailyCalorieResponse>

    // ============= 음식 인식 =============

    @Multipart
    @POST("food/recognize")
    suspend fun recognizeFood(
        @Part image: MultipartBody.Part,
        @Part("userUniqueCode") uniqueCode: RequestBody
    ): ApiResponse<FoodRecognitionResponse>

    @GET("food/search")
    suspend fun searchFoodCalories(
        @Query("foodName") foodName: String
    ): ApiResponse<FoodSearchResponse>

    // ============= 식사 관리 =============

    @POST("meals")
    suspend fun createMeal(@Body request: MealRequest): ApiResponse<MealResponse>

    @PUT("meals/{mealId}")
    suspend fun updateMeal(
        @Path("mealId") mealId: Long,
        @Body request: MealRequest
    ): ApiResponse<MealResponse>

    @DELETE("meals/{mealId}")
    suspend fun deleteMeal(@Path("mealId") mealId: Long): ApiResponse<String>

    @GET("meals/{mealId}")
    suspend fun getMeal(@Path("mealId") mealId: Long): ApiResponse<MealResponse>

    @GET("meals/user/{uniqueCode}/date/{date}")
    suspend fun getMealsByDate(
        @Path("uniqueCode") uniqueCode: String,
        @Path("date") date: String
    ): ApiResponse<List<MealResponse>>

    // ============= 관리자 =============

    @POST("admin/login")
    suspend fun adminLogin(@Body request: AdminLoginRequest): ApiResponse<AdminLoginResponse>

    @GET("admin/users")
    suspend fun getAllUsers(): ApiResponse<List<UserManagementResponse>>

    @PUT("admin/users/{userId}/reset-password")
    suspend fun resetUserPassword(
        @Path("userId") userId: Long,
        @Body request: ResetPasswordRequest
    ): ApiResponse<String>

    @DELETE("admin/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Long): ApiResponse<String>
}
